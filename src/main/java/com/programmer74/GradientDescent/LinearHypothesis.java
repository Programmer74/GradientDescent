package com.programmer74.GradientDescent;

import java.io.Serializable;

public class LinearHypothesis implements Hypothesis, Serializable {
    public double calculateHypothesis(double x, double theta0, double theta1) {
        return theta0 + (theta1 * x);
    }
}
