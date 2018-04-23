package com.programmer74.GradientDescent;

import com.programmer74.util.Pair;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class GradientWorkerNode implements Runnable {

    private Boolean hasCalculated;
    private boolean shouldCalculate;
    private boolean shouldStop;
    private double answer;
    private Hypothesis hypothesis;
    private List<Pair<Double>> data;
    private double theta0;
    private double theta1;
    private DoubleUnaryOperator factor;

    public GradientWorkerNode(Hypothesis hypothesis, List<Pair<Double>> data, DoubleUnaryOperator factor) {
        this.hypothesis = hypothesis;
        this.data = data;
        this.factor = factor;
        theta0 = 0;
        theta1 = 0;
        answer = 0;
        hasCalculated = false;
        shouldCalculate = false;
        shouldStop = false;
    }

    public void setThetas(double theta0, double theta1) {
        synchronized (this) {
            this.theta0 = theta0;
            this.theta1 = theta1;
        }
    }

    public void beginCalculating() {
        this.hasCalculated = false;
        this.shouldCalculate = true;
    }

    public void stop() {
        this.shouldStop = true;
    }

    public boolean hasCalculated() {
        return hasCalculated;
    }

    public double getAnswer() {
        return answer;
    }

    @Override
    public void run() {
        while (!shouldStop) {
            if (!shouldCalculate) {
                Thread.yield();
                continue;
            }
            synchronized (this) {
                hasCalculated = false;
                answer = GradientDescentCalculator.calculateGradientOfThetaN(data, theta0, theta1, hypothesis, factor);
                hasCalculated = true;
                shouldCalculate = false;
                Thread.yield();
            }
        }
    }
}
