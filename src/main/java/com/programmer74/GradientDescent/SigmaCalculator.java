package com.programmer74.GradientDescent;

import com.programmer74.util.Pair;
import org.apache.spark.api.java.function.Function;

import java.io.Serializable;

public class SigmaCalculator implements Function<Pair<Double>, Double>, Serializable {
    double theta0, theta1;
    int pow;
    public SigmaCalculator(double theta0, double theta1, int pow) {
        this.theta0 = theta0;
        this.theta1 = theta1;
        this.pow = pow;
    }
    @Override
    public Double call(Pair<Double> sample) throws Exception {
        return (theta0 + (theta1 * sample.getFirst()) - sample.getSecond())
                * Math.pow(sample.getFirst(), pow);

    }
}