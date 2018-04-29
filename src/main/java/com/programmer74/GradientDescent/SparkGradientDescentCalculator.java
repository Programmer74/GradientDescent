package com.programmer74.GradientDescent;

import com.programmer74.util.Pair;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class SparkGradientDescentCalculator implements GradientDescentCalculator {

    private double alpha;
    private int maxIterations;
    private Hypothesis hypothesis;
    private double epsilon = 0.0001;
    private JavaSparkContext sc;
    private JavaRDD<Pair<Double>> pdata;

    public SparkGradientDescentCalculator(JavaSparkContext sc, JavaRDD<Pair<Double>> pdata, Hypothesis hypothesis) {
        this.sc = sc;
        this.pdata = pdata;
        this.alpha = 0.01;
        this.epsilon = 0.0001;
        this.maxIterations = 10_000;
        this.hypothesis = hypothesis;
    }

    public SparkGradientDescentCalculator(JavaSparkContext sc, JavaRDD<Pair<Double>> pdata, Hypothesis hypothesis, double alpha, int maxIterations, double epsilon) {
        this.sc = sc;
        this.pdata = pdata;
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

            if (i == 20) {
                startTime = System.currentTimeMillis();
            }

            if (hasConverged(oldTheta0, theta0) && hasConverged(oldTheta1, theta1)) {
                System.out.println("Converged at iteration " + (i + 1));
                break;
            }

            oldTheta0 = theta0;
            oldTheta1 = theta1;

            double sum0 = calculateGradientOfThetaNSpark(sc, pdata, theta0, theta1, hypothesis, 0);
            double sum1 = calculateGradientOfThetaNSpark(sc, pdata, theta0, theta1, hypothesis, 1);

            theta0 = theta0 - (alpha * (1.0 / pdata.count()) * sum0);
            theta1 = theta1 - (alpha * (1.0 / pdata.count()) * sum1);

            if (i == 20) {
                long endTime  = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Single iteration was for " + totalTime + " ms");
            }
        }
        return new Pair<>(theta0, theta1);
    }

    protected double calculateGradientOfThetaNSpark(JavaSparkContext sc,  JavaRDD<Pair<Double>> pdata, Double theta0, Double theta1,
                                                      Hypothesis hypothesis, Integer pow) {
        JavaRDD<Double> calcHypothesises = pdata.map(new SigmaCalculator(hypothesis, theta0, theta1, pow));
        return calcHypothesises.reduce((a, b) -> (a + b));
    }
}
