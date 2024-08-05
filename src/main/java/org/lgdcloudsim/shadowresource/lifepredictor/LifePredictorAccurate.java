package org.lgdcloudsim.shadowresource.lifepredictor;

import org.lgdcloudsim.request.Instance;

public class LifePredictorAccurate implements LifePredictor {
    @Override
    public double predictLife(Instance instance) {
        return instance.getLifecycle();
    }
}
