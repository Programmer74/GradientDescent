package com.programmer74.GradientDescent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import com.programmer74.util.Pair;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class GradientDescentCalculator {

    private double alpha;
    private int maxIterations;
    private Hypothesis hypothesis;
    private static final double epsilon = 0.0001;

    public GradientDescentCalculator(Hypothesis hypothesis) {
        this.alpha = 0.01;
        this.maxIterations = 10_000;
        this.hypothesis = hypothesis;
    }

    public GradientDescentCalculator(Hypothesis hypothesis, double alpha, int maxIterations) {
        this.alpha = alpha;
        this.maxIterations = maxIterations;
        this.hypothesis = hypothesis;
    }

    private boolean hasConverged(double old, double current) {
        return Math.abs(current - old) < epsilon;
    }

    public Pair<Double> doSingleVarGradientDescent(List<Pair<Double>> data, double initialTheta0, double initialTheta1)
    {
        double theta0 = initialTheta0, theta1 = initialTheta1;
        double oldTheta0 = 0, oldTheta1 = 0;

        for (int i = 0 ; i < maxIterations; i++) {
            if (hasConverged(oldTheta0, theta0) && hasConverged(oldTheta1, theta1)) {
                break;
            }

            oldTheta0 = theta0;
            oldTheta1 = theta1;

            double sum0 = calculateGradientOfThetaN(data, theta0, theta1, hypothesis, x -> 1);
            double sum1 = calculateGradientOfThetaN(data, theta0, theta1, hypothesis, x -> x);

            theta0 = theta0 - (alpha * (1.0 / data.size()) * sum0);
            theta1 = theta1 - (alpha * (1.0 / data.size()) * sum1);
        }
        return new Pair<>(theta0, theta1);
    }

    public Pair<Double> doSingleVarGradientDescentSpark(JavaSparkContext sc, List<Pair<Double>> data, double initialTheta0, double initialTheta1)
    {
        double theta0 = initialTheta0, theta1 = initialTheta1;
        double oldTheta0 = 0, oldTheta1 = 0;

        JavaRDD<Pair<Double>> pdata = sc.parallelize(data);

        for (int i = 0 ; i < maxIterations; i++) {
            if (hasConverged(oldTheta0, theta0) && hasConverged(oldTheta1, theta1)) {
                break;
            }

            oldTheta0 = theta0;
            oldTheta1 = theta1;

            double sum0 = calculateGradientOfThetaNSpark(sc, pdata, theta0, theta1, hypothesis, 0);
            double sum1 = calculateGradientOfThetaNSpark(sc, pdata, theta0, theta1, hypothesis, 1);

            theta0 = theta0 - (alpha * (1.0 / data.size()) * sum0);
            theta1 = theta1 - (alpha * (1.0 / data.size()) * sum1);
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

    protected double calculateGradientOfThetaNSpark(JavaSparkContext sc,  JavaRDD<Pair<Double>> pdata, Double theta0, Double theta1,
                                                      Hypothesis hypothesis, Integer pow) {
        JavaRDD<Double> calcHypothesises = pdata.map(sample ->
                (hypothesis.calculateHypothesis(sample.getFirst(), theta0, theta1) - sample.getSecond())
                * Math.pow(sample.getFirst(), pow)
        );
        return calcHypothesises.reduce((a, b) -> (a + b));
    }
}
