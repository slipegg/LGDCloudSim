package org.cpnsim.statemanager;

/**
 * An interface to be implemented by each class that generates the state of a host.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public interface HostStateGenerator {

    /**
     * Generate the state of a host.
     *
     * @return the state of the host.the state includes 4 integers: cpu, ram, storage and bw.
     * */
    int[] generateHostState();
}
