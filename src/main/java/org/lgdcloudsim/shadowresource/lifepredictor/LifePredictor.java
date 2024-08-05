package org.lgdcloudsim.shadowresource.lifepredictor;

import org.lgdcloudsim.request.Instance;

public interface LifePredictor {
    double predictLife(Instance instance);
}
