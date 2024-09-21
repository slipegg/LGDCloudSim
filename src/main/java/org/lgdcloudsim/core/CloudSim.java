package org.lgdcloudsim.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.lgdcloudsim.core.events.*;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.datacenter.CollaborationManager;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.datacenter.DatacenterPowerOnRecord;
import org.lgdcloudsim.record.MemoryRecord;
import org.lgdcloudsim.record.SqlRecord;
import org.lgdcloudsim.record.SqlRecordNull;
import org.lgdcloudsim.record.SqlRecordSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An abstract class to manage Cloud Computing simulations,
 * providing all methods to start, pause and stop them.
 * It sends and processes all discrete events during the simulation time.
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Manoel Campos da Silva Filho
 * @author Anonymous
 * @since CloudSim Toolkit 1.0
 */
public class CloudSim implements Simulation {
    /**
     * The logger for this class.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(CloudSim.class.getSimpleName());
    /**
     * The version of CLGDCloudSim.
     */
    public static final String VERSION = "LGDCloudSim 1.0";
    /**
     * The simulation time.
     */
    double clock;
    /**
     * A flag to indicate if the simulation is running or not.
     */
    @Getter
    private boolean running;
    /**
     * The queue of events that will be sent in a future simulation time.
     */
    private final FutureQueue future;
    /**
     * The deferred event queue.
     */
    private final DeferredQueue deferred;
    /**
     * The list of entities in the simulation.
     */
    private final List<CloudSimEntity> entityList;
    /**
     * The Cloud Information Service (CIS) that provides information about the simulation entities.
     * It also acts as a cloud administrator.
     */
    @Getter
    private final CloudInformationService cis;
    /**
     * The network topology used in the simulation.
     */
    @Getter
    private NetworkTopology networkTopology;
    /**
     * The collaboration manager used in the simulation.
     */
    @Getter
    private CollaborationManager collaborationManager;
    /**
     * A flag to indicate if the simulation should record the simulation data in a SQL database.
     */
    @Getter
    @Setter
    private boolean isSqlRecord;
    /**
     * The SQL record used to store the simulation data in a SQL database.
     */
    @Getter
    @Setter
    private SqlRecord sqlRecord = new SqlRecordNull();
    /**
     * The simulation accuracy.
     */
    @Getter
    private int simulationAccuracy;
    /**
     * The termination time of the simulation.
     */
    private double terminationTime = -1;
    /**
     * A flag to indicate if the simulation system is simulating only one datacenter.
     */
    @Getter
    @Setter
    private boolean singleDatacenterFlag = false;
    /**
     * The name of the database used to store the simulation data.
     */
    @Getter
    @Setter
    private String dbName;

    /**
     * Creates a new CloudSim instance.
     */
    public CloudSim() {
        clock = 0;
        this.entityList = new ArrayList<>();
        this.future = new FutureQueue();
        this.deferred = new DeferredQueue();
        this.cis = new CloudInformationService(this);
        this.simulationAccuracy = 3;
        this.isSqlRecord = true;
    }

    @Override
    public double clock() {
        return clock;
    }

    @Override
    public String clockStr() {
        return "%.2f ms".formatted(clock);
    }

    @Override
    public Simulation setClock(double time) {
        this.clock = time;
        return this;
    }

    @Override
    public void addEntity(@NonNull final CloudSimEntity entity) {
        if (running) {
            final var evt = new CloudSimEvent(0, entity, SimEntity.NULL, CloudActionTags.NONE, entity);
            future.addEvent(evt);
        }

        if (entity.getId() == -1) { // Only add once!
            entity.setId(entityList.size());
            entityList.add(entity);
        }
    }

    @Override
    public SimEvent select(final SimEntity dest, final Predicate<SimEvent> predicate) {
        final SimEvent evt = findFirstDeferred(dest, predicate);
        if (evt != SimEvent.NULL) {
            deferred.remove(evt);
        }

        return evt;
    }

    @Override
    public SimEvent findFirstDeferred(final SimEntity dest, final Predicate<SimEvent> predicate) {
        return filterEventsToDestinationEntity(deferred, predicate, dest).findFirst().orElse(SimEvent.NULL);
    }

    @Override
    public void send(@NonNull final SimEvent evt) {
        future.addEvent(evt);
    }

    @Override
    public boolean terminateAt(double time) {
        if (time <= clock) {
            return false;
        }

        terminationTime = time;
        return true;
    }

    /**
     * Gets a stream of events inside a specific queue that match a given predicate
     * and are targeted to an specific entity.
     *
     * @param queue     the queue to get the events from
     * @param predicate the event selection predicate
     * @param dest      id of entity that the event has to be sent to
     * @return a Stream of events from the queue
     */
    private Stream<SimEvent> filterEventsToDestinationEntity(final EventQueue queue, final Predicate<SimEvent> predicate, final SimEntity dest) {
        return filterEvents(queue, predicate.and(evt -> evt.getDestination() == dest));
    }

    /**
     * Gets a stream of events inside a specific queue that match a given predicate.
     *
     * @param queue the queue to get the events from
     * @param predicate the event selection predicate
     * @return a Stream of events from the queue
     */
    private Stream<SimEvent> filterEvents(final EventQueue queue, final Predicate<SimEvent> predicate) {
        return queue.stream().filter(predicate);
    }

    @Override
    public double start() {
        if(this.sqlRecord==null) {
            if (isSqlRecord) {
                if(getDbName()==null || getDbName().isEmpty()){
                    this.sqlRecord = new SqlRecordSimple("LGDCloudSim"+collaborationManager.getDatacenterById(1).getArchitecture()+".db");
                }else{
                    this.sqlRecord = new SqlRecordSimple(getDbName());
                }
            } else {
                this.sqlRecord = new SqlRecordNull();
            }
        }
        startSync();
        MemoryRecord.recordMemory();

        while (processEvents(Double.MAX_VALUE)) {
            MemoryRecord.recordMemory();
        }
        finish();
        MemoryRecord.recordMemory();
        getSqlRecord().close();
        return clock;
    }

    /**
     * Process all the events happening up to a given time are processed.
     *
     * @param until The interval for which the events should be processed (in seconds)
     * @return true if some event was processed, false if no event was processed
     *         or a termination time was set and the clock reached that time
     */
    protected boolean processEvents(final double until) {
        if (!runClockTickAndProcessFutureEvents(until)) {
            return false;
        }
        LOGGER.debug(this.deferred.toString());
        /* If it's time to terminate the simulation, sets a new termination time
         * so that events to finish Cloudlets with a negative length are received.
         * Cloudlets with a negative length must keep running
         * until a CLOUDLET_FINISH event is sent to the broker or the termination time is reached.
         */
        if (isTimeToTerminateSimulationUnderRequest()) {
            return false;
        }
        return true;
    }


    /**
     * Finishes execution of running entities before terminating the simulation,
     * then cleans up internal state.
     *
     * <b>Note:</b> Should be used only in the <b>synchronous</b> mode
     * (after starting the simulation with {@link #startSync()}).
     *
     * Note that it prints some simulation details at the end.
     */
    private void finish() {
        LOGGER.info("Simulation finished at {}.", clockStr());
        double allCost = 0;
        for (Datacenter datacenter : getCis().getDatacenterList()) {
            double dcCost = datacenter.getAllCost();
            System.out.printf("%s's TCO = %f\n", datacenter.getName(), dcCost);
            allCost += dcCost;
            Map<Integer, Integer> partitionConflicts = datacenter.getConflictHandler().getPartitionConflicts();
            int conflictSum = 0;
            for (Map.Entry<Integer, Integer> entry : partitionConflicts.entrySet()) {
                System.out.printf("%s's Partition%d has %d conflicts.\n", datacenter.getName(), entry.getKey(), entry.getValue());
                conflictSum += entry.getValue();
            }
            System.out.printf("%s all has %d conflicts.\n", datacenter.getName(), conflictSum);
            DatacenterPowerOnRecord record = datacenter.getStatesManager().getDatacenterPowerOnRecord();
            System.out.printf("%s has a maximum of %d hosts powered on, with a total usage time of %f ms for all hosts\n", datacenter.getName(), record.getMaxHostNum(), record.getAllPowerOnTime());

            sqlRecord.recordDatacentersInfo(datacenter);
        }
        sqlRecord.recordDcNetworkInfo(networkTopology);

        System.out.printf("All TCO = %f\n", allCost);
        System.out.printf("Database to save simulation results: %s\n", getSqlRecord().getDbPath());
    }

    @Override
    public boolean isTimeToTerminateSimulationUnderRequest() {
        return isTerminationTimeSet() && clock >= terminationTime;
    }

    @Override
    public boolean getIsSqlRecord() {
        return isSqlRecord;
    }

    @Override
    public void setIsSqlRecord(boolean isSqlRecord) {
        this.isSqlRecord = isSqlRecord;
    }

    /**
     * Run one tick of the simulation, processing and removing the
     * events in the {@link #future future event queue} that happen
     * up to a given time.
     * @param until The interval for which the events should be processed (in seconds)
     * @return true if some event was processed, false otherwise
     */
    private boolean runClockTickAndProcessFutureEvents(final double until) {
        executeRunnableEntities(until);
        if (future.isEmpty() || isOnlySyn()) {
            return false;
        }

        final SimEvent first = future.first();
        if (first.getTime() <= until) {
            processFutureEventsHappeningAtSameTimeOfTheFirstOne(first);
            return true;
        }

        return false;
    }

    /**
     * Get whether there are only continuous looping events.
     * @return true if there are only continuous looping events, false otherwise
     */
    private boolean isOnlySyn() {
        //TODO We can consider making a judgment in advance here.
//        if (!cis.getDatacenterList().isEmpty() && future.size() > cis.getDatacenterList().size() + 1) {
//            return false;
//        }
        for (SimEvent simEvent : future.stream().toList()) {
            if (!CloudActionTags.LOOP_TAG.contains(simEvent.getTag())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the list of entities that are in {@link SimEntity.State#RUNNABLE}
     * and execute them.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void executeRunnableEntities(final double until) {
        /* Uses an indexed loop instead of anything else to avoid
        ConcurrencyModificationException when a HostFaultInjection is created inside a DC. */
        for (int i = 0; i < entityList.size(); i++) {
            final CloudSimEntity ent = entityList.get(i);
            if (ent.getState() == SimEntity.State.RUNNABLE) {
                ent.run(until);
            }
        }
    }

    /**
     * Process all the future events happening at the same time of the first one.
     * @param firstEvent the first event to process
     */
    private void processFutureEventsHappeningAtSameTimeOfTheFirstOne(final SimEvent firstEvent) {
        processEvent(firstEvent);
        future.remove(firstEvent);

        while (!future.isEmpty()) {
            final SimEvent evt = future.first();
            if (evt.getTime() != firstEvent.getTime())
                break;
            processEvent(evt);
            future.remove(evt);
        }
    }

    /**
     * Processes an event.
     *
     * @param evt the event to be processed
     */
    protected void processEvent(final SimEvent evt) {
        if (evt.getTime() < clock) {
            final var msg = "Past event detected. Event time: %.2f Simulation clock: %.2f";
            throw new IllegalArgumentException(msg.formatted(evt.getTime(), clock));
        }

        setClock(evt.getTime());
        if (CloudActionTags.UNIQUE_TAG.contains(evt.getTag())) {
            if (deferred.isExistSameEvent(evt.getDestination(), evt.getTag(), evt.getData())) {
                return;
            }
        }
        deferred.addEvent(evt);
    }


    @Override
    public void startSync() {
        LOGGER.info("{}================== Starting {} =================={}", System.lineSeparator(), VERSION, System.lineSeparator());
        startEntitiesIfNotRunning();
    }

    @Override
    public int getNumEntities() {
        return entityList.size();
    }

    /**
     * Starts all entities if they are not running yet.
     */
    private void startEntitiesIfNotRunning() {
        if (running) {
            return;
        }

        running = true;
        entityList.forEach(SimEntity::start);
        LOGGER.info("{}: All entities started.", clockStr());
    }

    @Override
    public void setNetworkTopology(NetworkTopology networkTopology) {
        this.networkTopology = networkTopology;
    }

    @Override
    public void setCollaborationManager(CollaborationManager collaborationManager) {
        this.collaborationManager = collaborationManager;
    }

    @Override
    public void setSimulationAccuracy(int simulationAccuracy) {
        this.simulationAccuracy = simulationAccuracy;
    }

    @Override
    public SqlRecord getSqlRecord() {
        return sqlRecord;
    }

    @Override
    public void setSqlRecord(SqlRecord sqlRecord) {
        this.sqlRecord = sqlRecord;
    }

    @Override
    public boolean isTerminationTimeSet() {
        return terminationTime > 0.0;
    }
}
