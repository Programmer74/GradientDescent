package com.programmer74.GradientDescent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import com.programmer74.util.Pair;

public class BigDecimalGradientDescentCalculator implements GradientDescentCalculator {

    private double alpha;
    private int maxIterations;
    private Hypothesis hypothesis;
    private List<Pair<Double>> data;
    private static final double epsilon = 0.0001;

    public BigDecimalGradientDescentCalculator(List<Pair<Double>> data, Hypothesis hypothesis) {
        this.data = data;
        this.alpha = 0.01;
        this.maxIterations = 10_000;
        this.hypothesis = hypothesis;
    }

    public BigDecimalGradientDescentCalculator(List<Pair<Double>> data, Hypothesis hypothesis, double alpha, int maxIterations) {
        this.data = data;
        this.alpha = alpha;
        this.maxIterations = maxIterations;
        this.hypothesis = hypothesis;
    }

    private boolean hasConverged(double old, double current) {
        return Math.abs(current - old) < epsilon;
    }

    private boolean hasConverged(BigDecimal old, BigDecimal current) {
        return Math.abs(old.subtract(current).doubleValue()) < epsilon;
    }

    interface BigDecimalUnaryOperator {
        BigDecimal operation(BigDecimal a);
    }
    interface BigDecimalBinaryOperator {
        BigDecimal operation(BigDecimal a, BigDecimal b);
    }

    @Override
    public Pair<Double> calculate(double initialTheta0, double initialTheta1)
    {
        BigDecimal theta0 = BigDecimal.valueOf(initialTheta0);
        BigDecimal theta1 = BigDecimal.valueOf(initialTheta0);
        BigDecimal oldTheta0 = BigDecimal.ZERO;
        BigDecimal oldTheta1 = BigDecimal.ZERO;

        for (int i = 0 ; i < maxIterations; i++) {

            //System.out.println(theta0 + ", " + theta1);

            if (hasConverged(oldTheta0, theta0) && hasConverged(oldTheta1, theta1)) {
                break;
            }

            oldTheta0 = theta0;
            oldTheta1 = theta1;

            BigDecimal sum0 = calculateGradientOfThetaN(data, theta0, theta1, hypothesis, x -> BigDecimal.valueOf(1));
            BigDecimal sum1 = calculateGradientOfThetaN(data, theta0, theta1, hypothesis, x -> x);

            //System.out.println(" > " + sum0 + ", " + sum1);

            theta0 = theta0.subtract(BigDecimal.valueOf(alpha).divide(BigDecimal.valueOf(data.size()), 12, RoundingMode.CEILING).multiply(sum0));
            theta1 = theta1.subtract(BigDecimal.valueOf(alpha).divide(BigDecimal.valueOf(data.size()), 12, RoundingMode.CEILING).multiply(sum1));
        }
        return new Pair<>(theta0.doubleValue(), theta1.doubleValue());
    }

    protected static BigDecimal calculateGradientOfThetaN(List<Pair<Double>> data, BigDecimal theta0, BigDecimal theta1,
                                                          Hypothesis hypothesis, BigDecimalUnaryOperator factor) {
        return calculateSigma(data, (x, y) ->  (
                hypothesis.calculateHypothesis(x, theta0, theta1).subtract(y).multiply(factor.operation(x))
        ));
    }

    protected static BigDecimal calculateSigma(List<Pair<Double>> data, BigDecimalBinaryOperator operator) {
        BigDecimal result = BigDecimal.ZERO;
        for (Pair<Double> sample : data) {
            double x = sample.getFirst(), y = sample.getSecond();
            //System.out.println(" + oper(" + x + "," + y + ") = +" + operator.operation(BigDecimal.valueOf(x), BigDecimal.valueOf(y)));
            result = result.add(operator.operation(BigDecimal.valueOf(x), BigDecimal.valueOf(y)));
        }
        return result;
    }

}
