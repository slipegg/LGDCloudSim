package org.scalecloudsim.datacenter;

import org.cloudsimplus.core.Simulation;

import java.util.*;

public class CollaborationManagerSimple implements CollaborationManager {
    private Map<Integer, Set<Datacenter>> collaborationMap;

    public CollaborationManagerSimple() {
        this.collaborationMap = new HashMap<>();
    }

    public CollaborationManagerSimple(Simulation simulation) {
        this();
        simulation.setCollaborationManager(this);
    }

    public CollaborationManagerSimple(Map<Integer, Set<Datacenter>> collaborationMap) {
        this.collaborationMap = new HashMap<>();
        addDatacenter(collaborationMap);
    }

    @Override
    public CollaborationManager addDatacenter(Datacenter datacenter, int collaborationId) {
        Set<Datacenter> datacenters = collaborationMap.get(collaborationId);
        if (datacenters == null) {
            datacenters = new HashSet<>();
            collaborationMap.put(collaborationId, datacenters);
        }

        datacenters.add(datacenter);
        datacenterAddCollaborationId(datacenter, collaborationId);
        return this;
    }

    private void datacenterAddCollaborationId(Datacenter datacenter, int collaborationId) {
        Set<Integer> collaborationIds = datacenter.getCollaborationIds();
        if (collaborationIds.contains(collaborationId)) {
            LOGGER.warn("the datacenter(" + datacenter + ") already belongs to the collaboration " + collaborationId);
        } else {
            collaborationIds.add(collaborationId);
        }
    }

    @Override
    public CollaborationManager addDatacenter(Map<Integer, Set<Datacenter>> collaborationMap) {
        for (Map.Entry<Integer, Set<Datacenter>> entry : collaborationMap.entrySet()) {
            int collaborationId = entry.getKey();
            Set<Datacenter> addDatacenters = entry.getValue();
            Set<Datacenter> datacenters = this.collaborationMap.get(entry.getKey());
            if (datacenters == null) {
                datacenters = new HashSet<>();
                this.collaborationMap.put(collaborationId, datacenters);
            }
            datacenters.addAll(addDatacenters);
            for (Datacenter datacenter : addDatacenters) {
                datacenterAddCollaborationId(datacenter, collaborationId);
            }
        }
        return this;
    }

    @Override
    public CollaborationManager removeDatacenter(Datacenter datacenter, int collaborationId) {
        Set<Datacenter> datacenters = collaborationMap.get(collaborationId);
        if (datacenters != null) {
            datacenters.remove(datacenter);
        }
        datacenterRemoveCollaborationId(datacenter, collaborationId);
        return this;
    }

    private void datacenterRemoveCollaborationId(Datacenter datacenter, int collaborationId) {
        Set<Integer> collaborationIds = datacenter.getCollaborationIds();
        if (!collaborationIds.contains(collaborationId)) {
            LOGGER.warn("the datacenter(" + datacenter + ") does not belong to the collaboration " + collaborationId + " to be removed");
        } else {
            collaborationIds.remove(collaborationId);
        }
    }

    @Override
    public CollaborationManager removeDatacenter(Datacenter datacenter) {
        Set<Integer> collaborationIds = datacenter.getCollaborationIds();
        for (Integer collaborationId : collaborationIds) {
            removeDatacenter(datacenter, collaborationId);
        }
        return this;
    }

    @Override
    public List<Datacenter> getOtherDatacenters(Datacenter datacenter, int collaborationId) {
        Set<Datacenter> datacenters = collaborationMap.get(collaborationId);
        if (datacenters == null) {
            return new ArrayList<>();
        }
        List<Datacenter> datacenterList = new ArrayList<>(datacenters);
        datacenterList.remove(datacenter);
        return datacenterList;
    }

    @Override
    public List<Datacenter> getDatacenters(int collaborationId) {
        Set<Datacenter> datacenters = collaborationMap.get(collaborationId);
        if (datacenters == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(datacenters);
    }

    @Override
    public List<Datacenter> getDatacenters(Datacenter datacenter) {
        Set<Datacenter> datacenters = new HashSet<>();
        for (Map.Entry<Integer, Set<Datacenter>> entry : collaborationMap.entrySet()) {
            Integer collaborationId = entry.getKey();
            if (datacenter.getCollaborationIds().contains(collaborationId)) {
                Set<Datacenter> collaborationDatacenters = entry.getValue();
                datacenters.addAll(collaborationDatacenters);
            }
        }
        return new ArrayList<>(datacenters);
    }

    @Override
    public Map<Integer, Set<Datacenter>> getCollaborationMap() {
        return collaborationMap;
    }

    @Override
    public List<Datacenter> getOtherDatacenters(Datacenter datacenter) {
        List<Datacenter> datacenters = getDatacenters(datacenter);
        datacenters.remove(datacenter);
        return datacenters;
    }
}
