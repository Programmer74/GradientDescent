package com.programmer74;

import com.programmer74.GradientDescent.GradientDescentCalculator;
import com.programmer74.GradientDescent.LinearHypothesis;
import com.programmer74.util.DummyDataLoader;
import com.programmer74.util.Pair;

import java.util.List;

public class GradientDescentApplication {

    private static final int triesCount = 3;
    private static final int samplesCount = 100000;

    public static void main(String[] args) {
        List<Pair<Double>> data = DummyDataLoader.loadBigDummyData(new LinearHypothesis(), samplesCount, 0.001);
        GradientDescentCalculator calculator = new GradientDescentCalculator(new LinearHypothesis());

        System.out.println("Benchmarking simple GradientDescent");

        double avgTime = 0;
        for (int i = 0; i < triesCount; i++) {
            long startTime = System.currentTimeMillis();

            Pair<Double> finalTheta = calculator.doSingleVarGradientDescent(data, 0.1, 0.1);
            System.out.printf("theta0 = %f, theta1 = %f\n", finalTheta.getFirst(), finalTheta.getSecond());

            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println(totalTime + " ms");
            avgTime += totalTime;
        }
        avgTime = avgTime / triesCount;
        System.out.println("Calculations done in " + avgTime + " ms.");

        System.out.println("Benchmarking multithreaded GradientDescent with three threads");

        avgTime = 0;
        for (int i = 0; i < triesCount; i++) {
            long startTime = System.currentTimeMillis();

            Pair<Double> finalTheta = calculator.doSingleVarGradientDescentMultithreaded(data, 0.1, 0.1, 3);
            System.out.printf("theta0 = %f, theta1 = %f\n", finalTheta.getFirst(), finalTheta.getSecond());

            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println(totalTime + " ms");
            avgTime += totalTime;
        }
        avgTime = avgTime / triesCount;
        System.out.println("Calculations done in " + avgTime + " ms.");

        System.out.println("Benchmarking multithreaded GradientDescent with five threads");

        avgTime = 0;
        for (int i = 0; i < triesCount; i++) {
            long startTime = System.currentTimeMillis();

            Pair<Double> finalTheta = calculator.doSingleVarGradientDescentMultithreaded(data, 0.1, 0.1, 5);
            System.out.printf("theta0 = %f, theta1 = %f\n", finalTheta.getFirst(), finalTheta.getSecond());

            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println(totalTime + " ms");
            avgTime += totalTime;
        }
        avgTime = avgTime / triesCount;
        System.out.println("Calculations done in " + avgTime + " ms.");
    }
}