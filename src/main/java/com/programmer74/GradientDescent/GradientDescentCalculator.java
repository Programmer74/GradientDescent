package com.programmer74.GradientDescent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import com.programmer74.util.Pair;

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

            BigDecimal sum0 = calculateGradientOfThetaN(data, theta0, theta1, hypothesis, x -> 1);
            BigDecimal sum1 = calculateGradientOfThetaN(data, theta0, theta1, hypothesis, x -> x);

            theta0 = theta0 - sum0.multiply(new BigDecimal(alpha * (1.0 / data.size()))).doubleValue();
            theta1 = theta1 - sum1.multiply(new BigDecimal(alpha * (1.0 / data.size()))).doubleValue();
        }
        return new Pair<>(theta0, theta1);
    }

    public Pair<Double> doSingleVarGradientDescentMultithreaded(List<Pair<Double>> data, double initialTheta0,
                                                                double initialTheta1, int maxThreadCount)
    {
        double theta0 = initialTheta0, theta1 = initialTheta1;
        double oldTheta0 = 0, oldTheta1 = 0;

        //Generating a pool of workers and a pool of threads with those workers
        List<GradientWorkerNode> nodesTheta0 = new ArrayList<>();
        List<GradientWorkerNode> nodesTheta1 = new ArrayList<>();
        List<Thread> nodesTheta0Thread = new ArrayList<>();
        List<Thread> nodesTheta1Thread = new ArrayList<>();

        int elementsPerWorker = (int)(data.size() * 1.0 / maxThreadCount);

        for (int i = 0; i < maxThreadCount; i++) {

            int from;
            int to;

            from = i * elementsPerWorker;

            if (i != maxThreadCount - 1) {
                to = (i + 1) * elementsPerWorker;
            } else {
                to = data.size();
            }

            //The threads won't be stopped or re-generated until we calculate what we need

            nodesTheta0.add(new GradientWorkerNode(hypothesis, data.subList(from, to), x -> 1));
            nodesTheta0Thread.add(new Thread(nodesTheta0.get(i)));
            nodesTheta0Thread.get(i).start();

            nodesTheta1.add(new GradientWorkerNode(hypothesis, data.subList(from, to), x -> x));
            nodesTheta1Thread.add(new Thread(nodesTheta1.get(i)));
            nodesTheta1Thread.get(i).start();
        }


        for (int i = 0 ; i < maxIterations; i++) {
            if (hasConverged(oldTheta0, theta0) && hasConverged(oldTheta1, theta1)) {
                break;
            }

            oldTheta0 = theta0;
            oldTheta1 = theta1;

            for (int j = 0; j < maxThreadCount; j++) {
                nodesTheta0.get(j).setThetas(theta0, theta1);
                nodesTheta0.get(j).beginCalculating();

                nodesTheta1.get(j).setThetas(theta0, theta1);
                nodesTheta1.get(j).beginCalculating();
            }

            //Waiting for those workers to finish their job
            boolean notCalculatedYet = true;
            while (notCalculatedYet) {
                notCalculatedYet = false;
                //Counting from bottom to top since the last worker may have slightly bigger data slice
                for (int j = maxThreadCount - 1; j >= 0; j--) {
                    if (!nodesTheta0.get(j).hasCalculated() || !nodesTheta1.get(j).hasCalculated()) {
                        notCalculatedYet = true;
                        Thread.yield();
                        break;
                    }
                }
            }

            BigDecimal sum0 = new BigDecimal(0), sum1 = new BigDecimal(0);
            for (int j = 0; j < maxThreadCount; j++) {
                sum0.add(nodesTheta0.get(j).getAnswer());
                sum1.add(nodesTheta1.get(j).getAnswer());
            }

            theta0 = theta0 - sum0.multiply(new BigDecimal(alpha * (1.0 / data.size()))).doubleValue();
            theta1 = theta1 - sum1.multiply(new BigDecimal(alpha * (1.0 / data.size()))).doubleValue();
        }

        //Stopping those threads since we won't need them anymore
        for (int i = 0; i < maxThreadCount; i++) {
            nodesTheta0.get(i).stop();
            nodesTheta1.get(i).stop();

            try {
                nodesTheta0Thread.get(i).join();
                nodesTheta1Thread.get(i).join();
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }

        }

        return new Pair<>(theta0, theta1);
    }

    protected static BigDecimal calculateGradientOfThetaN(List<Pair<Double>> data, double theta0, double theta1,
                                             Hypothesis hypothesis, DoubleUnaryOperator factor) {
        return calculateSigma(data, (x, y) ->  (
                hypothesis.calculateHypothesis(x, theta0, theta1) - y) * factor.applyAsDouble(x)
        );
    }

    protected static BigDecimal calculateSigma(List<Pair<Double>> data, DoubleBinaryOperator inner) {
        BigDecimal res = new BigDecimal(0);
        for (Pair<Double> entry : data) {
            double x = entry.getFirst(), y = entry.getSecond();
            res = res.add(new BigDecimal(inner.applyAsDouble(x, y)));
        }
        return res;
    }
}
