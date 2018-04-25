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

    public static void usage() {
        System.out.println("Usage:\n$ java -jar GradientDescent.jar (--no-spark | <master url>) <csv filename>");
    }

    public static void main(String[] args) {

        boolean withoutSpark = false;
        boolean localMode = false;
        int localThreadCount = 0;
        String masterLocation = "";
        String fileName = "";

        if (args.length < 2) {
            usage();
            return;
        }

        if (args[0].equals("--no-spark")) {
            withoutSpark = true;
            System.out.println("Set mode to NO_SPARK");
        } else {
            if (args[0].matches("^local[\\[][0-9]+[]]$")) {
                System.out.println("Set mode to SPARK_LOCAL_CLUSTER");
                localMode = true;
                localThreadCount = Integer.valueOf(args[0].replaceAll("[^0-9]", ""));
                System.out.println("Local mode with thread count : " + localThreadCount);
            } else if (args[0].matches("^spark://.+")) {
                System.out.println("Set mode to SPARK_CLUSTER at " + args[0]);
                localMode = false;
                masterLocation = args[0];
            } else {
                System.out.println(args[0]);
                usage();
                return;
            }
        }

        fileName = args[1];

        GradientDescentCalculator calculator = null;

        if (withoutSpark) {
            System.out.println("Importing file " + fileName);

            long startTime = System.currentTimeMillis();
            List<Pair<Double>> data = DummyDataLoader.importCSV(fileName);
            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            System.out.println("File import time: " + totalTime + " ms");
            System.out.println("Dataset size: " + data.size());

            calculator = new BasicGradientDescentCalculator(data, new LinearHypothesis());
        } else {
            System.out.println("Configuring Spark...");
            Logger.getLogger("org").setLevel(Level.WARN);
            Logger.getLogger("akka").setLevel(Level.WARN);

            SparkConf conf = new SparkConf().setAppName("GradientDescentApplication");

            if (localMode) {
                conf.setMaster("local[" + localThreadCount + "]")
                    .set("spark.driver.host", "localhost");
            } else {
                conf.setMaster(masterLocation);
                conf.setJars(new String[]{"/home/hotaro/IdeaProjects/GradientDescent/target/GradientDescent-0.1-SNAPSHOT.jar"});
            }

            JavaSparkContext sc = new JavaSparkContext(conf);

            System.out.println("Importing file " + fileName);

            long startTime = System.currentTimeMillis();
            JavaRDD<Pair<Double>> data = DummyDataLoader.importCSV(sc, fileName);
            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            System.out.println("File import time: " + totalTime + " ms");
            System.out.println("Dataset size: " + data.count());

            calculator = new SparkGradientDescentCalculator(sc, data, new LinearHypothesis());
        }

        long startTime = System.currentTimeMillis();

        Pair<Double> finalTheta = calculator.calculate(0.1, 0.1);

        System.out.printf("theta0 = %f, theta1 = %f\n", finalTheta.getFirst(), finalTheta.getSecond());
        System.out.printf("y = %f * x + %f\n", finalTheta.getSecond(), finalTheta.getFirst());
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Took " + totalTime + " ms to calculate.");

    }
}