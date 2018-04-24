import com.programmer74.GradientDescent.*;
import com.programmer74.util.DummyDataLoader;
import com.programmer74.util.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Tests {

    private static JavaSparkContext sc = null;

    @BeforeClass
    public static void prepareSparkContext() {

        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);

        SparkConf conf = new SparkConf().setAppName("GradientDescentApplication")
                .setMaster("local[2]")
                .set("spark.driver.host", "localhost");
        sc = new JavaSparkContext(conf);
    }

    @Test
    public void BasicGradientDescentCalculatorSanityCheck() {

        System.out.println("Sanity check the Basic GradientDescentCalculator...");

        List<Pair<Double>> dummyData = DummyDataLoader.loadDummyData();
        GradientDescentCalculator calculator = new BasicGradientDescentCalculator(dummyData, new LinearHypothesis(),
                0.01, 10_000, Double.MIN_VALUE);

        Pair<Double> answer = calculator.calculate(0.1, 0.1);

        assertTrue(" theta0: " + Math.round(answer.getFirst()), Math.round(answer.getFirst()) == 2.0);
        assertTrue(" theta1: " + Math.round(answer.getSecond()), Math.round(answer.getSecond()) == 1.0);
    }

    @Test
    public void SparkGradientDescentCalculatorSanityCheck() {

        System.out.println("Sanity check the Spark GradientDescentCalculator...");

        List<Pair<Double>> dummyData = DummyDataLoader.loadDummyData();
        GradientDescentCalculator calculator = new SparkGradientDescentCalculator(sc, sc.parallelize(dummyData),
                new LinearHypothesis());

        Pair<Double> answer = calculator.calculate(0.1, 0.1);

        assertTrue(" theta0: " + Math.round(answer.getFirst()), Math.round(answer.getFirst()) == 2.0);
        assertTrue(" theta1: " + Math.round(answer.getSecond()), Math.round(answer.getSecond()) == 1.0);
    }

    @Test
    public void testSameCalculatorsOutput() {
        System.out.println("Checking that both calculators output the same result...");

        List<Pair<Double>> dummyData = DummyDataLoader.loadDummyData();

        GradientDescentCalculator calculatorB = new BasicGradientDescentCalculator(dummyData, new LinearHypothesis());

        GradientDescentCalculator calculatorS = new SparkGradientDescentCalculator(sc, sc.parallelize(dummyData),
                new LinearHypothesis());

        Pair<Double> answerB = calculatorB.calculate(0.1, 0.1);
        Pair<Double> answerS = calculatorS.calculate(0.1, 0.1);

        assertTrue("theta0 : " + answerB.getFirst() + " vs " + answerS.getFirst(),
                Math.abs(answerB.getFirst() - answerS.getFirst()) < 0.0001);
        assertTrue("theta1 : " + answerB.getSecond() + " vs " + answerS.getSecond(),
                Math.abs(answerB.getSecond() - answerS.getSecond()) < 0.0001);
    }

    @Test
    public void testAnotherThetas() {

        System.out.println("Testing with another theta values...");

        double theta0 = -0.5;
        double theta1 = 2.4;
        Hypothesis hypothesis = new LinearHypothesis();

        List<Pair<Double>> dummyData = DummyDataLoader.loadBigDummyDataWithThetas(hypothesis,
                10, 0.01, theta0, theta1);
        System.out.println(dummyData);

        GradientDescentCalculator calculator = new SparkGradientDescentCalculator(sc, sc.parallelize(dummyData),
                new LinearHypothesis());

        Pair<Double> answer = calculator.calculate(0.1, 0.1);

        assertTrue(" theta0: " + answer.getFirst() + " vs correct value " + theta0,
                Math.abs(answer.getFirst() - theta0) < 0.1);
        assertTrue(" theta1: " + answer.getSecond() + " vs correct value " + theta1,
                Math.abs(answer.getSecond() - theta1) < 0.01);
    }

    @Test
    public void testLoadingCSVFile() {

        System.out.println("Testing CSV file import...");

        double theta0 = 1.5;
        double theta1 = -0.4;
        Hypothesis hypothesis = new LinearHypothesis();

        List<Pair<Double>> dummyData = DummyDataLoader.loadBigDummyDataWithThetas(hypothesis,
                10, 0.01, theta0, theta1);
        System.out.println(dummyData);

        try {
            PrintWriter pw = new PrintWriter(new FileWriter("/tmp/gddummy.csv"));

            for (Pair<Double> sample : dummyData) {
                pw.print(String.format(Locale.ROOT, "%f,%f\n", sample.getFirst(), sample.getSecond()));
            }
            pw.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
            fail(ioex.getMessage());
        }

        GradientDescentCalculator calculator = new SparkGradientDescentCalculator(sc,
                DummyDataLoader.importCSV(sc, "/tmp/gddummy.csv"), new LinearHypothesis());

        Pair<Double> answer = calculator.calculate(0.1, 0.1);

        assertTrue(" theta0: " + answer.getFirst() + " vs correct value " + theta0,
                Math.abs(answer.getFirst() - theta0) < 0.1);
        assertTrue(" theta1: " + answer.getSecond() + " vs correct value " + theta1,
                Math.abs(answer.getSecond() - theta1) < 0.01);
    }
}
