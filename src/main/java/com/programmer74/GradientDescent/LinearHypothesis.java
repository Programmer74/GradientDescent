package com.programmer74.GradientDescent;

public class LinearHypothesis implements Hypothesis {
    public double calculateHypothesis(double x, double theta0, double theta1) {
        return theta0 + (theta1 * x);
    }
}
