/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudsimplus.network.topologies;

import lombok.Getter;

/**
 * Represents a link (edge) of a network graph
 * where the network topology was defined
 * from a file in <a href="http://www.cs.bu.edu/brite/user_manual/node29.html">BRITE format</a>.
 *
 * @author Thomas Hohnstein
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Toolkit 1.0
 */
public class TopologicalLink {

    /**
     * The BRITE id of the source node of the link.
     */
    @Getter
    private final int srcNodeID;

    /**
     * {@return the BRITE id} of the destination node of the link.
     */
    @Getter
    private final int destNodeID;

    /**
     * {@return the delay} of the link (in seconds).
     */
    @Getter
    private final double linkDelay;

    /**
     * {@return the bandwidth} of the link (in Megabits/s).
     */
    //TODO: 需要考虑是double类型还是int类型
    @Getter
    private final double linkBw;

    /**
     * {@return the unit price of bandwidth} of the link.
     */
    @Getter
    private final double linkUnitPrice;

    /**
     * Creates a new Topological Link.
     *
     * @param srcNode   the BRITE id of the source node of the link.
     * @param destNode  the BRITE id of the destination node of the link.
     * @param delay     the link delay of the connection (in seconds).
     * @param bandwidth the link bandwidth (in Megabits/s)
     * @param bwUnitPrice the link bandwidth unit price
     */
    public TopologicalLink(final int srcNode, final int destNode, final double delay, final double bandwidth,
            final double bwUnitPrice) {
        srcNodeID = srcNode;
        destNodeID = destNode;
        linkDelay = delay;
        linkBw = bandwidth;
        linkUnitPrice = bwUnitPrice;
    }
}
