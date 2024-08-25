package org.lgdcloudsim.shadowresource.filter;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.lifepredictor.LifePredictor;
import org.lgdcloudsim.shadowresource.partitionmanager.SRCoordinator;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import lombok.Setter;

import java.util.List;
import java.util.Random;

public class SRRequestFilterSimple implements SRRequestFilter {
    Random random;
    LifePredictor lifePredictor;
    @Setter
    SRCoordinator srCoordinator;

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
        if(predictLife<3 && random.nextDouble()<0.2){
            return new SRRequest(instance, predictLife);
        }else{
            return null;
        }
    }

    public SRRequestFilterSimple(LifePredictor lifePredictor) {
        random = new Random();
        this.lifePredictor = lifePredictor;
    }
}
