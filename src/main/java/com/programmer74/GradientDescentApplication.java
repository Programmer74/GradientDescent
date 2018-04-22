package com.programmer74;

import com.programmer74.GradientDescent.GradientDescentCalculator;
import com.programmer74.GradientDescent.LinearHypothesis;
import com.programmer74.util.DummyDataLoader;
import com.programmer74.util.Pair;

import java.util.List;

public class GradientDescentApplication {

    public static void main(String[] args) {
        List<Pair<Double>> data = DummyDataLoader.loadDummyData();
        GradientDescentCalculator calculator = new GradientDescentCalculator(new LinearHypothesis());

        Pair<Double> finalTheta = calculator.doSingleVarGradientDescent(data, 0.1, 0.1);
        System.out.printf("theta0 = %f, theta1 = %f\n", finalTheta.getFirst(), finalTheta.getSecond());
    }
}