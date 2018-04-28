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
    public static double benchmark(GradientDescentCalculator calculator, double initialTheta0, double initialTheta1 ) {
        double avgTime = 0;
        for (int i = 0; i < triesCount; i++) {
            long startTime = System.currentTimeMillis();

            Pair<Double> finalTheta = calculator.calculate(initialTheta0, initialTheta1);
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
        System.out.println("Usage:\n$ java -jar GradientDescent.jar (--no-spark | <master url>) <csv filename>\n" +
                "    [--set-alpha <alpha>] [--set-epsilon <epsilon>] [--set-iterations <max iterations count>]\n" +
                "    [--set-theta-0 <initial-theta-0>] [--set-theta-1 <initial-theta-1>]\n" +
                "    [--set-worker-memory <worker memory>]");
    }

    public static void main(String[] args) {

        boolean withoutSpark = false;
        boolean localMode = false;
        int localThreadCount = 0;
        String masterLocation = "";
        String fileName;
        double alpha = 0.01;
        double epsilon = 0.0001;
        int maxIterations = 10_000;
        double initialTheta0 = 0.1;
        double initialTheta1 = 0.1;
        String workingMemory = "8g";
        boolean benchmarkMode = false;

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

        try {
            for (int i = 2; i < args.length; i++) {
                switch (args[i]) {
                    case "--set-alpha":
                        i++;
                        alpha = Double.parseDouble(args[i]);
                        System.out.println("Set alpha to " + alpha);
                        break;
                    case "--set-iterations":
                        i++;
                        maxIterations = Integer.parseInt(args[i]);
                        System.out.println("Set Maximum iterations to " + maxIterations);
                        break;
                    case "--set-epsilon":
                        i++;
                        epsilon = Double.parseDouble(args[i]);
                        System.out.println("Set epsilon to " + epsilon);
                        break;
                    case "--set-theta-0":
                        i++;
                        initialTheta0 = Double.parseDouble(args[i]);
                        System.out.println("Set Initial Theta0 to " + initialTheta0);
                        break;
                    case "--set-theta-1":
                        i++;
                        initialTheta1 = Double.parseDouble(args[i]);
                        System.out.println("Set Initial Theta1 to " + initialTheta1);
                        break;
                    case "--set-worker-memory":
                        i++;
                        workingMemory = args[i];
                        System.out.println("Set worker memory to " + workingMemory);
                        break;
                    case "--benchmark":
                        benchmarkMode = true;
                        System.out.println("Benchmark Mode enabled");
                        break;
                    default:
                        usage();
                        return;
                }
            }
        } catch (Exception ex) {
            System.err.println("Incorrect arguments.");
            usage();
            System.exit(-2);
        }

        GradientDescentCalculator calculator = null;

        if (withoutSpark) {
            System.out.println("Importing file " + fileName);

            long startTime = System.currentTimeMillis();
            List<Pair<Double>> data = DummyDataLoader.importCSV(fileName);
            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            System.out.println("File import time: " + totalTime + " ms");
            System.out.println("Dataset size: " + data.size());

            calculator = new BasicGradientDescentCalculator(data, new LinearHypothesis(), alpha, maxIterations, epsilon);
        } else {
            System.out.println("Configuring Spark...");
            Logger.getLogger("org").setLevel(Level.WARN);
            Logger.getLogger("akka").setLevel(Level.WARN);

            SparkConf conf = new SparkConf().setAppName("GradientDescentApplication");

            if (localMode) {
                conf.setMaster("local[" + localThreadCount + "]")
                    .set("spark.driver.host", "localhost");
            } else {

                String jarPath = System.getenv("GDA_JAR_FILE");
                if ((jarPath == null) || (jarPath.equals(""))) {
                    System.err.println("GDA_JAR_FILE not set.");
                    System.err.println("Please, run \"mvn package -DskipTests\" and \"export GDA_JAR_FILE=<file.java>\"");
                    System.err.println("Otherwise, the Spark Cluster won't be able to (de)serialize our custom classes.");
                    System.exit(-1);
                }

                conf.setMaster(masterLocation);
                conf.set("spark.executor.memory", workingMemory);
                conf.setJars(new String[]{jarPath});
            }

            JavaSparkContext sc = new JavaSparkContext(conf);

            System.out.println("Importing file " + fileName);

            long startTime = System.currentTimeMillis();
            JavaRDD<Pair<Double>> data = DummyDataLoader.importCSV(sc, fileName);
            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            System.out.println("File import time: " + totalTime + " ms");
            System.out.println("Dataset size: " + data.count());

            calculator = new SparkGradientDescentCalculator(sc, data, new LinearHypothesis(), alpha, maxIterations, epsilon);
        }

        long startTime = System.currentTimeMillis();

        Pair<Double> finalTheta = calculator.calculate(initialTheta0, initialTheta1);

        System.out.printf("theta0 = %f, theta1 = %f\n", finalTheta.getFirst(), finalTheta.getSecond());
        System.out.printf("y = %f * x + %f\n", finalTheta.getSecond(), finalTheta.getFirst());
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Took " + totalTime + " ms to calculate.");

        if (benchmarkMode) {
            System.out.println("Doing additional benchmarking...");
            System.out.println("Average: " + benchmark(calculator, initialTheta0, initialTheta1));
            System.out.println("Compared to single basic method...");
            calculator = new BasicGradientDescentCalculator(DummyDataLoader.importCSV(fileName), new LinearHypothesis(), alpha, maxIterations, epsilon);
            System.out.println("Average: " + benchmark(calculator, initialTheta0, initialTheta1));
        }
    }
}