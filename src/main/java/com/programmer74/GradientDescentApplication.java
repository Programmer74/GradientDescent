package com.programmer74;

import com.programmer74.GradientDescent.BasicGradientDescentCalculator;
import com.programmer74.GradientDescent.GradientDescentCalculator;
import com.programmer74.GradientDescent.SparkGradientDescentCalculator;
import com.programmer74.GradientDescent.LinearHypothesis;
import com.programmer74.util.DummyDataLoader;
import com.programmer74.util.Pair;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;

public class GradientDescentApplication {

    private static final int triesCount = 3;
    private static final int samplesCount = 100;
    public static double benchmark(GradientDescentCalculator calculator) {
        double avgTime = 0;
        for (int i = 0; i < triesCount; i++) {
            long startTime = System.currentTimeMillis();

            Pair<Double> finalTheta = calculator.calculate(0.1, 0.1);
            System.out.printf("theta0 = %f, theta1 = %f\n", finalTheta.getFirst(), finalTheta.getSecond());

            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println(totalTime + " ms");
            avgTime += totalTime;
        }
        avgTime = avgTime / triesCount;
        return avgTime;
    }


    public static void main(String[] args) {
        //List<Pair<Double>> data = DummyDataLoader.loadBigDummyData(new LinearHypothesis(), samplesCount, 0.001);
        List<Pair<Double>> data = DummyDataLoader.importCSV("test.csv");
        System.out.println("Input dataset size: " + data.size());
        GradientDescentCalculator calculator = new BasicGradientDescentCalculator(data, new LinearHypothesis());

        System.out.println("Benchmarking Basic GradientDescent");
        double avgTime = benchmark(calculator);
        System.out.println("Basic Gradient Descent required " + avgTime + "ms to calculate.");

        System.out.println("Firing up Spark...");

        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);

        SparkConf conf = new SparkConf().setAppName("GradientDescentApplication")
                .setJars(new String[]{"/home/hotaro/IdeaProjects/GradientDescent/target/GradientDescent-0.1-SNAPSHOT.jar"})
                .setMaster("spark://192.168.1.111:7077")
                .set("spark.executor.memory", "8g");
                //.set("spark.driver.host", "localhost");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> dummydata = Arrays.asList(1, 2, 3, 4, 5);
        JavaRDD<Integer> distData = sc.parallelize(dummydata);
        int sum = distData.reduce((a, b) -> (a + b));

        System.out.println("Spark says that the sum of " + dummydata + " is " + sum);

        //calculator = new SparkGradientDescentCalculator(sc, sc.parallelize(data), new LinearHypothesis());
        JavaRDD<Pair<Double>> scdata = sc.parallelize(data);
        calculator = new SparkGradientDescentCalculator(sc, scdata, new LinearHypothesis());

        System.out.println("Benchmarking Spark GradientDescent");
        avgTime = benchmark(calculator);
        System.out.println("Spark Gradient Descent required " + avgTime + "ms to calculate.");
    }
}