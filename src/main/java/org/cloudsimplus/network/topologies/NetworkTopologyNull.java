/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2021 Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudsimplus.network.topologies;

import org.cloudsimplus.core.SimEntity;
import org.cloudsimplus.network.DelayDynamicModel;

/**
 * A class that implements the Null Object Design Pattern for {@link org.cloudsimplus.network.topologies.NetworkTopology}
 * class.
 *
 * @author Manoel Campos da Silva Filho
 * @see org.cloudsimplus.network.topologies.NetworkTopology#NULL
 */
final class NetworkTopologyNull implements org.cloudsimplus.network.topologies.NetworkTopology {
    @Override
    public void addLink(SimEntity src, SimEntity dest, double bandwidth, double lat) {/**/}

    @Override
    public void removeLink(SimEntity src, SimEntity dest) {/**/}

    @Override
    public double getDelay(SimEntity src, SimEntity dest) {
        return 0;
    }

    @Override
    public double getBw(SimEntity src, SimEntity dest) {
        return 0;
    }

    @Override
    public void allocateBw(SimEntity src, SimEntity dest, double allocateBw) {
    }

    @Override
    public void releaseBw(SimEntity src, SimEntity dest, double releaseBw) {

    }

    @Override
    public void setDelayDynamicModel(DelayDynamicModel delayDynamicModel) {

    }

    @Override
    public double getDynamicDelay(SimEntity src, SimEntity dest, double time) {
        return 0;
    }

    @Override
    public double getAcessLatency(SimEntity src, SimEntity dest) {
        return 0;
    }
}
