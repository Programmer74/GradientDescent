package com.programmer74.util;

import com.programmer74.GradientDescent.Hypothesis;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static List<Pair<Double>> loadBigDummyDataWithThetas(Hypothesis hypothesis, int samplesCount,
                                                                double marginOfError, double theta0, double theta1) {
        List<Pair<Double>> data = new ArrayList<>();
        Random r = new Random();

        for (int i = 0; i < samplesCount; i++) {
            double x = i * 10.0 / samplesCount;
            double y = hypothesis.calculateHypothesis(x, theta0, theta1);
            y += r.nextGaussian() * marginOfError;
            data.add(new Pair<>(x, y));
        }
        return data;
    }

    public static List<Pair<Double>> loadBigDummyData(Hypothesis hypothesis, int samplesCount, double marginOfError) {
       return loadBigDummyDataWithThetas(hypothesis, samplesCount, marginOfError, 2, 1);
    }

    public static List<Pair<Double>> importCSV(String filename) {
        List<String> lines = new ArrayList<>();
        try {
            Path path = Paths.get(filename);
            lines = Files.readAllLines(path);
        } catch (IOException ioex) {
            //should never get here
            ioex.printStackTrace();
            System.exit(-1);
        }
        List<Pair<Double>> data = new ArrayList<>();
        for (String line : lines) {
            String[] fields = line.split(",");
            Pair<Double> pair = new Pair<>(Double.valueOf(fields[0]), Double.valueOf(fields[1]));
            data.add(pair);
        }
        return data;
    }

    public static JavaRDD<Pair<Double>> importCSV(JavaSparkContext sc, String filename) {
        JavaRDD<String> textData = sc.textFile(filename);

        JavaRDD<Pair<Double>> data = textData.map(
                (Function<String, Pair<Double>>) line -> {
                    String[] fields = line.split(",");
                    Pair<Double> pair = new Pair<>(Double.valueOf(fields[0]), Double.valueOf(fields[1]));
                    return pair;
                });
        return data;
    }
}
