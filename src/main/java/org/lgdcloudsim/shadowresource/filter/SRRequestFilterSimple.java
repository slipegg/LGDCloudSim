package org.lgdcloudsim.shadowresource.filter;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.lifepredictor.LifePredictor;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequestMapCoordinator;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import java.util.List;
import java.util.Random;

public class SRRequestFilterSimple implements SRRequestFilter {
    Random random;
    LifePredictor lifePredictor;

    SRRequestMapCoordinator mapCoordinator;

    @Override
    public SRRequestFilterRes filter(Instance instance) {
        SRRequestFilterRes res = new SRRequestFilterRes();

        SRRequest srRequest = isChangeToSRRequest(instance);
        if (srRequest != null) {
            res.add(srRequest);
        }else {
            res.add(instance);
        }

        return res;
    }

    @Override
    public SRRequestFilterRes filter(List<Instance> instances) {
        SRRequestFilterRes res = new SRRequestFilterRes();
        for (Instance instance : instances) {
            SRRequest srRequest = isChangeToSRRequest(instance);
            if (srRequest != null) {
                res.add(srRequest);
            }else {
                res.add(instance);
            }
        }
        return res;
    }

    private SRRequest isChangeToSRRequest(Instance instance) {
        double predictLife = lifePredictor.predictLife(instance);
        if(predictLife<3 && mapCoordinator.getSRRequestCpuTotal() < 1000) {
            return new SRRequest(instance, predictLife);
        }else{
            return null;
        }
    }

    public SRRequestFilterSimple(LifePredictor lifePredictor, SRRequestMapCoordinator mapCoordinator) {
        random = new Random();
        this.lifePredictor = lifePredictor;
        this.mapCoordinator = mapCoordinator;
    }
}
