package org.lgdcloudsim.shadowresource.lifepredictor;

import org.lgdcloudsim.request.Instance;

import java.util.Random;

public class LifePredictorRandom implements LifePredictor{
    double avgLife;
    double stdLife;

    Random random;
    @Override
    public double predictLife(Instance instance) {
        return Math.min(random.nextGaussian()*stdLife+avgLife, 0.1);
    }

    public LifePredictorRandom() {
        this(5,5);
    }

    public LifePredictorRandom(double avgLife, double stdLife) {
        this.avgLife = avgLife;
        this.stdLife = stdLife;
        random = new Random();
    }
}
