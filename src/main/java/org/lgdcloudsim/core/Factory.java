package org.lgdcloudsim.core;

import org.lgdcloudsim.loadbalancer.LoadBalancer;
import org.lgdcloudsim.conflicthandler.ConflictHandler;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.record.SqlRecord;
import org.lgdcloudsim.interscheduler.InterScheduler;
import org.lgdcloudsim.statemanager.PredictionManager;

/**
 * An interface that implements a method factory.
 * Its main function is to realize that after the user initializes through the file,
 * it can initialize the components of the simulation system in different ways according to the component names provided by the file.
 * So when the user customizes a component with different methods,
 * it needs to be registered here. Then it can be initialized through the file.
 * Current components are:
 * {@link IntraScheduler}, {@link PredictionManager}, {@link InterScheduler}, {@link LoadBalancer}, {@link ConflictHandler}.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface Factory {
    /**
     * Get the {@link IntraScheduler} by the type name and some parameters.
     * @param type the type name of the intra-scheduler.
     * @param id the id of the intra-scheduler.
     * @param firstPartitionId the first synchronization partition id of the intra-scheduler.
     * @param partitionNum the number of partitions in the data center.
     * @return the intra-scheduler.
     */
    IntraScheduler getIntraScheduler(String type, int id, int firstPartitionId, int partitionNum);

    /**
     * Get the prediction manager by the type name.
     * @param type the type name of the prediction manager.
     * @return the prediction manager.
     */
    PredictionManager getPredictionManager(String type);

    /**
     * Get the {@link InterScheduler} by the type name and some parameters.
     * @param type  the type name of the inter-scheduler.
     * @param id the id of the inter-scheduler.
     * @param simulation the simulation object.
     * @param collaborationId the collaboration id of the inter-scheduler.
     * @param target the target id of the inter-scheduler.
     * @param isSupportForward whether the inter-scheduler supports forward.
     * @return the inter-scheduler.
     */
    InterScheduler getInterScheduler(String type, int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward);

    /**
     * Get the{@link LoadBalancer} by the type name.
     * @param type the type name of the load balance.
     * @return the load balance.
     */
    LoadBalancer getLoadBalance(String type);

    /**
     * Get the {@link ConflictHandler} by the type name.
     * @param type the type name of the conflict handler.
     * @return the conflict handler.
     */
    ConflictHandler getResourceAllocateSelector(String type);

    /**
     * Get the {@link SqlRecord} by the type name.
     * @param type the type name of the sql record.
     * @return the sql record.
     */
    SqlRecord getSqlRecord(String type);

    /**
     * Get the {@link SqlRecord} by the type name and the database name.
     * @param type the type name of the sql record.
     * @param dbName the database name.
     * @return the sql record.
     */
    SqlRecord getSqlRecord(String type, String dbName);
}
