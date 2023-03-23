package org.scalecloudsim.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InstanceGroupGraphList implements InstanceGroupGraph{
    List<InstanceGroupEdge> graph;
    boolean directed;

    InstanceGroupGraphList(boolean directed){
        this.directed=directed;
        graph= new ArrayList<>();
    }
    @Override
    public InstanceGroupGraph setDirected(boolean directed) {
        this.directed=directed;
        return this;
    }

    @Override
    public boolean getDirected() {
        return directed;
    }

    @Override
    public InstanceGroupGraph addEdge(InstanceGroup src, InstanceGroup dst, double delay, long bw) {
        InstanceGroupEdge edge=new InstanceGroupEdge(src,dst,delay,bw);
        graph.add(edge);
        return this;
    }

    @Override
    public InstanceGroupGraph addEdge(InstanceGroupEdge edge) {
        graph.add(Objects.requireNonNull(edge));
        return this;
    }

    @Override
    public int removeEdge(InstanceGroup src, InstanceGroup dst) {
        for(int i=0;i<graph.size();i++){
            if(graph.get(i).getSrc()==src&&graph.get(i).getDst()==dst){
                graph.remove(i);
                return 0;
            }
            if(!directed){
                if(graph.get(i).getSrc()==dst&&graph.get(i).getDst()==src){
                    graph.remove(i);
                    return 0;
                }
            }
        }
        LOGGER.info("There is no edge(src={},dst={}) to remove.",src,dst);
        return -1;
    }

    @Override
    public InstanceGroupEdge getEdge(InstanceGroup src, InstanceGroup dst) {
        for (InstanceGroupEdge instanceGroupEdge : graph) {
            if (instanceGroupEdge.getSrc() == src && instanceGroupEdge.getDst() == dst) {
                return instanceGroupEdge;
            }
            if (!directed) {
                if (instanceGroupEdge.getSrc() == dst && instanceGroupEdge.getDst() == src) {
                    return instanceGroupEdge;
                }
            }
        }
        LOGGER.info("There is no edge(src={},dst={}).",src,dst);
        return null;
    }

    @Override
    public List getGraph() {
        return graph;
    }

    @Override
    public List getDstList(InstanceGroup src) {
        List res = new ArrayList<>();
        for(InstanceGroupEdge instanceGroupEdge : graph) {
            if(instanceGroupEdge.src==src){
                res.add(instanceGroupEdge.dst);
            }
            if(!this.directed&&(instanceGroupEdge.dst==src)){
                res.add(instanceGroupEdge.src);
            }
        }
        return res;
    }
}
