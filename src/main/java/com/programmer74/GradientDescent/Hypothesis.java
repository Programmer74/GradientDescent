package com.programmer74.GradientDescent;

import java.math.BigDecimal;

public interface Hypothesis {
    double calculateHypothesis(double x, double theta0, double theta1);
    BigDecimal calculateHypothesis(BigDecimal x, BigDecimal theta0, BigDecimal theta1);
}
