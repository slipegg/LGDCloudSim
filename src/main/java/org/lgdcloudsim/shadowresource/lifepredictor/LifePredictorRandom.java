package org.lgdcloudsim.shadowresource.lifepredictor;

import org.lgdcloudsim.request.Instance;

import java.util.Random;

public class LifePredictorRandom implements LifePredictor{
    double avgLife;
    double stdLife;

    Random random;
    @Override
    public double predictLife(Instance instance) {
        return random.nextGaussian()*stdLife+avgLife;
    }

    public LifePredictorRandom(double avgLife, double stdLife) {
        this.avgLife = avgLife;
        this.stdLife = stdLife;
        random = new Random();
    }
}
