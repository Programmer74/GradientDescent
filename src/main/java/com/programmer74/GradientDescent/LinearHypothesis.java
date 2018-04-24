package com.programmer74.GradientDescent;

import java.io.Serializable;
import java.math.BigDecimal;

public class LinearHypothesis implements Hypothesis, Serializable {
    @Override
    public double calculateHypothesis(double x, double theta0, double theta1) {
        return theta0 + (theta1 * x);
    }
    @Override
    public BigDecimal calculateHypothesis(BigDecimal x, BigDecimal theta0, BigDecimal theta1) {
        return theta0.add(theta1.multiply(x));
    }
}
