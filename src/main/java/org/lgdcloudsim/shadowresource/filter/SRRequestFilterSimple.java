package org.lgdcloudsim.shadowresource.filter;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.lifepredictor.LifePredictor;
import org.lgdcloudsim.shadowresource.requestmapper.MapCoordinator;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import java.util.List;
import java.util.Random;

public class SRRequestFilterSimple implements SRRequestFilter {
    Random random;
    LifePredictor lifePredictor;

    MapCoordinator mapCoordinator;

    @Override
    public SRRequest filter(Instance instance) {
        double predictLife = lifePredictor.predictLife(instance);
        if(predictLife<3 && mapCoordinator.getSRRequestCpuTotal() < 1000) {
            return new SRRequest(instance, predictLife);
        }else{
            return null;
        }
    }

    @Override
    public List<SRRequest> filter(List<Instance> instances) {
        return instances.stream().map(this::filter).toList();
    }

    public SRRequestFilterSimple(LifePredictor lifePredictor, MapCoordinator mapCoordinator) {
        random = new Random();
        this.lifePredictor = lifePredictor;
        this.mapCoordinator = mapCoordinator;
    }
}
