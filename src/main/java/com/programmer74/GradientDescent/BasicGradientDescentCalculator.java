package com.programmer74.GradientDescent;

import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import com.programmer74.util.Pair;

public class BasicGradientDescentCalculator implements GradientDescentCalculator {

    private double alpha;
    private int maxIterations;
    private Hypothesis hypothesis;
    private List<Pair<Double>> data;
    private double epsilon;

    public BasicGradientDescentCalculator(List<Pair<Double>> data, Hypothesis hypothesis) {
        this.data = data;
        this.alpha = 0.01;
        this.maxIterations = 10_000;
        this.epsilon = 0.0001;
        this.hypothesis = hypothesis;
    }

    public BasicGradientDescentCalculator(List<Pair<Double>> data, Hypothesis hypothesis, double alpha, int maxIterations, double epsilon) {
        this.data = data;
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.maxIterations = maxIterations;
        this.hypothesis = hypothesis;
    }

    private boolean hasConverged(double old, double current) {
        return Math.abs(current - old) < epsilon;
    }

    @Override
    public Pair<Double> calculate(double initialTheta0, double initialTheta1)
    {
        double theta0 = initialTheta0, theta1 = initialTheta1;
        double oldTheta0 = 0, oldTheta1 = 0;

        long startTime = 0;

        for (int i = 0 ; i < maxIterations; i++) {
            if (hasConverged(oldTheta0, theta0) && hasConverged(oldTheta1, theta1)) {
                break;
            }

            if (i == 20) {
                startTime = System.currentTimeMillis();
            }

            oldTheta0 = theta0;
            oldTheta1 = theta1;

            double sum0 = calculateGradientOfThetaN(data, theta0, theta1, hypothesis, x -> 1);
            double sum1 = calculateGradientOfThetaN(data, theta0, theta1, hypothesis, x -> x);

            theta0 = theta0 - (alpha * (1.0 / data.size()) * sum0);
            theta1 = theta1 - (alpha * (1.0 / data.size()) * sum1);

            if (i == 20) {
                long endTime  = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Single iteration was for " + totalTime + " ms");
                System.out.println("sums: " + sum0 + " " + sum1);
                System.out.println("thetas: " + theta0 + " " + theta1);
            }
        }
        return new Pair<>(theta0, theta1);
    }

    protected static double calculateGradientOfThetaN(List<Pair<Double>> data, double theta0, double theta1,
                                                      Hypothesis hypothesis, DoubleUnaryOperator factor) {
        return calculateSigma(data, (x, y) ->  (
                hypothesis.calculateHypothesis(x, theta0, theta1) - y) * factor.applyAsDouble(x)
        );
    }

    protected static double calculateSigma(List<Pair<Double>> data, DoubleBinaryOperator inner) {
        return data.stream()
                .mapToDouble(theta -> {
                    double x = theta.getFirst(), y = theta.getSecond();
                    return inner.applyAsDouble(x, y);
                })
                .sum();
    }

}
