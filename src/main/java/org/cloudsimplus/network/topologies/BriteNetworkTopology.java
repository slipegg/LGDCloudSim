/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudsimplus.network.topologies;

import lombok.Getter;
import org.cloudsimplus.core.SimEntity;
import org.cloudsimplus.network.DelayDynamicModel;
import org.cloudsimplus.network.DelayMatrix;
import org.cloudsimplus.network.topologies.readers.TopologyReaderBrite;
import org.cloudsimplus.util.ResourceLoader;
import org.cloudsimplus.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a network layer by reading the topology from a file in the
 * <a href="http://www.cs.bu.edu/brite/user_manual/node29.html">BRITE
 * format</a>, the <b>B</b>oston university
 * <b>R</b>epresentative <b>I</b>nternet <b>T</b>opology g<b>E</b>nerator,
 * and generates a topological network
 * from it. Information of this network is used to simulate latency in network
 * traffic of CloudSim.
 *
 * <p>The topology file may contain more nodes than the number of entities in the
 * simulation. It allows users to increase the scale of the simulation without
 * changing the topology file. Nevertheless, each CloudSim entity must be mapped
 * to one (and only one) BRITE node to allow proper work of the network
 * simulation. Each BRITE node can be mapped to only one entity at a time.</p>
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Manoel Campos da Silva Filho
 * @see #getInstance(String)
 * @see <a href="http://www.cs.bu.edu/brite/">Brite Oficial Website (shut down)</a>
 * @see <a href="https://web.archive.org/web/20200119144536/http://www.cs.bu.edu:80/brite/">Web archieve of Brite Oficial Website</a>
 * @since CloudSim Toolkit 1.0
 */
public class BriteNetworkTopology implements NetworkTopology {
    private static final Logger LOGGER = LoggerFactory.getLogger(BriteNetworkTopology.class.getSimpleName());

    /**
     * The BRITE id to use for the next node to be created in the network.
     */
    private int nextIdx;

    /**
     * Checks if the network simulation is working. If there were some problem
     * during creation of network (e.g., during parsing of BRITE file) that does
     * not allow a proper simulation of the network, this method returns false.
     */
    @Getter
    private boolean networkEnabled;

    /**
     * A matrix containing the delay (in seconds) between every pair of nodes in the network.
     */
    private DelayMatrix delayMatrix;

    /**
     * @see #getBwMatrix()
     */
    private double[][] bwMatrix;
    
    /**
     * the matrix containing the unit price of bandwidth (in Megabits/s)
     * between every pair of {@link SimEntity}s in the network.
     */
    private double[][] bwUnitPriceMatrix;

    /**
     * The Topological Graph of the network.
     */
    @Getter
    private TopologicalGraph graph;

    /**
     * The map between CloudSim entities and BRITE entities.
     * Each key is a CloudSim entity and each value the corresponding BRITE entity ID.
     */
    private Map<SimEntity, Integer> entitiesMap;

    private DelayDynamicModel delayDynamicModel;

    /**
     * The TCO of bandwidth between datacenters
     */
    @Getter
    private double TCONetwork;

    /**
     * Instantiates a Network Topology from a file inside the <b>application's resource directory</b>.
     *
     * @param fileName the <b>relative name</b> of the BRITE file
     * @return the BriteNetworkTopology instance.
     */
    public static BriteNetworkTopology getInstance(final String fileName) {
        final InputStreamReader reader = ResourceLoader.newInputStreamReader(fileName, BriteNetworkTopology.class);
        return new BriteNetworkTopology(reader);
    }

    public static BriteNetworkTopology getInstance(final String fileName, boolean directed) {
        final InputStreamReader reader = ResourceLoader.newInputStreamReader(fileName, BriteNetworkTopology.class);
        return new BriteNetworkTopology(reader, directed);
    }

    /**
     * Instantiates an empty Network Topology.
     *
     * @see #BriteNetworkTopology(String)
     * @see #BriteNetworkTopology(InputStreamReader)
     * @see #getInstance(String)
     */
    public BriteNetworkTopology() {
        entitiesMap = new HashMap<>();
        bwMatrix = new double[0][0];
        bwUnitPriceMatrix = new double[0][0];
        graph = new TopologicalGraph();
        delayMatrix = new DelayMatrix();
    }

    /**
     * Instantiates a Network Topology if a given file exists and can be successfully
     * parsed. File is written in the BRITE format and contains
     * topological information on simulation entities.
     *
     * @param filePath the path of the BRITE file
     * @see #BriteNetworkTopology()
     * @see #BriteNetworkTopology(InputStreamReader)
     * @see #getInstance(String)
     */
    public BriteNetworkTopology(final String filePath) {
        this(ResourceLoader.newInputStreamReader(filePath));
        LOGGER.info("Topology file: {}", filePath);
    }

    /**
     * Creates a network topology from a given input stream reader.
     * The file is written in the BRITE format and contains
     * topological information on simulation entities.
     *
     * @param reader the reader for the topology file
     * @see #BriteNetworkTopology()
     * @see #BriteNetworkTopology(InputStreamReader)
     * @see #getInstance(String)
     */
    private BriteNetworkTopology(final InputStreamReader reader) {
        this();
        final var instance = new TopologyReaderBrite();
        graph = instance.readGraphFile(reader);
        generateMatrices(false);//默认是无向图
    }

    /**
     * Creates a network topology from a given input stream reader
     * and of target topology direction type.
     * The file is written in the BRITE format and contains
     * topological information on simulation entities.
     *
     * @param reader the reader for the topology file
     * @param directed whether it is a digraph
     * @see #BriteNetworkTopology()
     * @see #BriteNetworkTopology(InputStreamReader)
     * @see #getInstance(String)
     */
    private BriteNetworkTopology(final InputStreamReader reader, boolean directed) {
        this();
        final var instance = new TopologyReaderBrite();
        graph = instance.readGraphFile(reader);
        generateMatrices(directed);
    }

    /**
     * Generates the matrices used internally to set latency and bandwidth
     * between elements.
     */
    private void generateMatrices(boolean directed) {
        //TODO 后期如果有需要可以改为在文件中自定义是无向图还是有向图,现在是需要在代码中指定
        delayMatrix = new DelayMatrix(graph, directed);
        bwMatrix = createBwMatrix(graph, directed);
        bwUnitPriceMatrix = createBwUnitPriceMatrix(graph);
        networkEnabled = true;
    }

    /**
     * Creates the matrix containing the available bandwidth between every pair
     * of nodes.
     *
     * @param graph    topological graph describing the topology
     * @param directed true if the graph is directed; false otherwise
     * @return the bandwidth graph
     */
    private double[][] createBwMatrix(final TopologicalGraph graph, final boolean directed) {
        final int nodes = graph.getNumberOfNodes();
        final double[][] matrix = Util.newSquareMatrix(nodes);

        for (final TopologicalLink edge : graph.getLinksList()) {
            matrix[edge.getSrcNodeID()][edge.getDestNodeID()] = edge.getLinkBw();
            if (!directed) {
                matrix[edge.getDestNodeID()][edge.getSrcNodeID()] = edge.getLinkBw();
            }
        }

        return matrix;
    }

    /**
     * Creates the matrix containing the unit price of bandwidth between
     * every pair of nodes.
     * Currently it's all initialized by 1, custom configuration will be 
     * added later.
     *
     * @param graph    topological graph describing the topology
     * @return the unit price of bandwidth graph
     */
    private double[][] createBwUnitPriceMatrix(final TopologicalGraph graph) {
        final int nodes = graph.getNumberOfNodes();
        final double[][] matrix = Util.newSquareMatrix(nodes);
        
        for (int i = 0; i < matrix.length; i = i + 1) {
            for (int j = 0; j < matrix[i].length; j = j + 1) {
                matrix[i][j] = 1;
            }
        }

        return matrix;
    }

    /**
     * Add new link in the network topology graph whatever it's present.
     * @param src  {@link SimEntity} that represents the link's source node
     * @param dest {@link SimEntity} that represents the link's destination node
     * @param bandwidth   link's bandwidth (in Megabits/s)
     * @param latency  link's latency (in seconds)
     */
    @Override
    public void addLink(final SimEntity src, final SimEntity dest, final double bandwidth, final double latency) {
        if (graph == null) {
            graph = new TopologicalGraph();
        }

        if (entitiesMap == null) {
            entitiesMap = new HashMap<>();
        }

        addNodeMapping(src);
        addNodeMapping(dest);

        final var link = new TopologicalLink(entitiesMap.get(src), entitiesMap.get(dest), latency, bandwidth);
        graph.addLink(link);
        generateMatrices(false);
    }

    /**
     * Remove the link in the network topology graph, haven't been supported.
     * @param src   {@link SimEntity} that represents the link's source node
     * @param dest  {@link SimEntity} that represents the link's destination node
     */
    @Override
    public void removeLink(final SimEntity src, final SimEntity dest) {
        throw new UnsupportedOperationException("Removing links is not yet supported on BriteNetworkTopologies");
    }

    /**
     * Adds a new node in the network topology graph if it's absent.
     *
     * @param entity the CloudSim entity to check if there isn't a BRITE mapping yet.
     */
    private void addNodeMapping(final SimEntity entity) {
        if (entitiesMap.putIfAbsent(entity, nextIdx) == null) {
            graph.addNode(new TopologicalNode(nextIdx));
            nextIdx++;
        }
    }

    /**
     * Maps a {@link SimEntity} to a BRITE node in the network topology.
     *
     * @param entity  {@link SimEntity} being mapped
     * @param briteID ID of the BRITE node that corresponds to the CloudSim
     */
    public void mapNode(final SimEntity entity, final int briteID) {
        if (!networkEnabled) {
            return;
        }

        if (entitiesMap.containsKey(entity)) {
            LOGGER.warn("Network mapping: CloudSim entity {} already mapped.", entity);
            return;
        }

        if (entitiesMap.containsValue(briteID)) {
            LOGGER.warn("BRITE node {} already in use.", briteID);
            return;
        }
        entitiesMap.put(entity, briteID);
    }

    /**
     * Un-maps a previously mapped {@link SimEntity} to a BRITE node in the network
     * topology.
     *
     * @param entity {@link SimEntity} being unmapped
     */
    public void unmapNode(final SimEntity entity) {
        if (!networkEnabled) {
            return;
        }

        entitiesMap.remove(entity);
    }

    /**
     * Get the delay of the link between the two nodes.
     * @param src  {@link SimEntity} that represents the link's source node
     * @param dest {@link SimEntity} that represents the link's destination node
     * @return the delay of the link between the two nodes.
     */
    @Override
    public double getDelay(final SimEntity src, final SimEntity dest) {
        if (!networkEnabled) {
            return 0.0;
        }

        try {
            final int srcEntityBriteId = entitiesMap.getOrDefault(src, -1);
            final int destEntityBriteId = entitiesMap.getOrDefault(dest, -1);
            return delayMatrix.getDelay(srcEntityBriteId, destEntityBriteId);
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0.0;
        }
    }

    /**
     * Get the bandwidth of the link between the two nodes.
     * @param src  {@link SimEntity} that represents the link's source node
     * @param dest {@link SimEntity} that represents the link's destination node
     * @return the bandwidth of the link between the two nodes.
     */
    @Override
    public double getBw(final SimEntity src, final SimEntity dest) {
        if (!networkEnabled) {
            return 0;
        }

        try {
            final int srcEntityBriteId = entitiesMap.getOrDefault(src, -1);
            final int destEntityBriteId = entitiesMap.getOrDefault(dest, -1);
            return bwMatrix[srcEntityBriteId][destEntityBriteId];
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    /**
     * The actual function to manipulate the bandwidth matrix and update
     * the TCO of network
     * 
     * @param srcId ID of the source entity
     * @param destId ID of the destination entity
     * @param deltaBw The amount to increase the remaining bandwidth by
     */
    private void updateBw(int srcId, int destId, double deltaBw) {
        bwMatrix[srcId][destId] += deltaBw;
        TCONetwork = -deltaBw * bwUnitPriceMatrix[srcId][destId];
    }

    /**
     * Allocate bandwidth to the link of the two nodes.
     * @param src  {@link SimEntity} that represents the link's source node
     * @param dest {@link SimEntity} that represents the link's destination node
     * @param allocateBw  the bandwidth to be allocated
     * @return  true if success to allocate bandwidth else false
     */
    @Override
    public boolean allocateBw(SimEntity src, SimEntity dest, double allocateBw) {
        double availableBw = getBw(src, dest);
        if (availableBw < allocateBw) {
            return false;
        }
        updateBw(entitiesMap.get(src), entitiesMap.get(dest), -allocateBw);

//        打印bwMatrix
//        System.out.println("allocate bwMatrix:"+src.getId()+" "+dest.getId() + " "+allocateBw);
//        for (int i = 0; i < bwMatrix.length; i++) {
//            for (int j = 0; j < bwMatrix[i].length; j++) {
//                System.out.print(bwMatrix[i][j] + " ");
//            }
//            System.out.println();
//        }
//        System.out.println();

        return true;
    }

    /**
     * Release bandwidth to the link of the two nodes.
     * @param src  {@link SimEntity} that represents the link's source node
     * @param dest {@link SimEntity} that represents the link's destination node
     * @param releaseBw  the bandwidth to be released
     */
    @Override
    public void releaseBw(SimEntity src, SimEntity dest, double releaseBw) {
        releaseBw(entitiesMap.get(src), entitiesMap.get(dest), releaseBw);
    }

    /**
     * Release bandwidth to the link of the two nodes.
     * @param srcId ID of the source entity
     * @param destId ID of the destination entity
     * @param releaseBw  the bandwidth to be released
     */
    @Override
    public void releaseBw(int srcId, int destId, double releaseBw) {
        updateBw(srcId, destId, releaseBw);
//        打印bwMatrix
//        System.out.println("release bwMatrix:" + srcId + " " + destId + " " + releaseBw);
//        for (int i = 0; i < bwMatrix.length; i++) {
//            for (int j = 0; j < bwMatrix[i].length; j++) {
//                System.out.print(bwMatrix[i][j] + " ");
//            }
//            System.out.println();
//        }
//        System.out.println();
    }

    /**
     * Set delayDynamicModel.
     * @param delayDynamicModel the delayDynamicModel to be set
     */
    @Override
    public void setDelayDynamicModel(DelayDynamicModel delayDynamicModel) {
        this.delayDynamicModel = delayDynamicModel;
    }

    /**
     * Get the dynamic delay of the two nodes.
     * @param src  {@link SimEntity} that represents the link's source node
     * @param dest {@link SimEntity} that represents the link's destination node
     * @param time  the random seed
     * @return  the dynamic delay of two nodes.
     */
    @Override
    public double getDynamicDelay(SimEntity src, SimEntity dest, double time) {
        if (delayDynamicModel == null) {
            return getDelay(src, dest);
        }
        return delayDynamicModel.getDynamicDelay(src, dest, getDelay(src, dest), time);
    }

    /**
     * Gets a <b>copy</b> of the matrix containing the bandwidth (in Megabits/s)
     * between every pair of {@link SimEntity}s in the network.
     */
    public double[][] getBwMatrix() {
        return Arrays.copyOf(bwMatrix, bwMatrix.length);
    }

    /**
     * Get the access latency of the link between the two nodes.
     * @param src  {@link SimEntity} that represents the link's source node
     * @param dest {@link SimEntity} that represents the link's destination node
     * @return the access latency of the link between the two nodes.
     */
    @Override
    public double getAcessLatency(SimEntity src, SimEntity dest) {
        int srcId = entitiesMap.get(src);
        int dstId = entitiesMap.get(dest);
        return graph.getNodeList().get(srcId).getAccessLatency(dstId);
    }
}
