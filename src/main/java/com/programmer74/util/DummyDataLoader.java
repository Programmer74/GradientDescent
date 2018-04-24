package com.programmer74.util;

import com.programmer74.GradientDescent.Hypothesis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DummyDataLoader {
    public static List<Pair<Double>> loadDummyData() {
        List<Pair<Double>> data = new ArrayList<>();
        data.add(new Pair<>(0.0, 2.0));
        data.add(new Pair<>(1.0, 3.0));
        data.add(new Pair<>(2.0, 4.0));
        data.add(new Pair<>(3.0, 5.0));
        data.add(new Pair<>(4.0, 6.0));
        data.add(new Pair<>(5.0, 7.0));
        data.add(new Pair<>(6.0, 8.0));
        return data;
    }

    public static List<Pair<Double>> loadBigDummyData(Hypothesis hypothesis, int samplesCount, double marginOfError) {
        List<Pair<Double>> data = new ArrayList<>();
        Random r = new Random();

        for (int i = 0; i < samplesCount; i++) {
            double x = i * 10.0; /// samplesCount;
            double y = hypothesis.calculateHypothesis(x, 2, 1);
            y += r.nextGaussian() * marginOfError;
            data.add(new Pair<>(x, y));
        }
        return data;
    }
}
