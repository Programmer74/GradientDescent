# Gradient Descent

Gradient Descent implementation in Java using Apache Spark.

Works for linear dependence. Written on Java. Supports parallel computation and CSV file importing.

## Requirements

- Java 1.8
- Maven

## Building

```
$ git clone https://github.com/Programmer74/GradientDescent
$ cd GradientDescent
$ mvn package [-DskipTests]
```

## Usage

```
$ java -jar GradientDescent-0.1-SNAPSHOT.jar (--no-spark | <master url>) <csv filename> [--set-alpha <alpha>] [--set-epsilon <epsilon>] [--set-iterations <max iterations count>] [--set-theta-0 <initial-theta-0>] [--set-theta-1 <initial-theta-1> [--set-worker-memory <worker memory>]
```
_assuming that you have the required classpath set OR the jar file is actually a symlink to ```GradientDescent-0.1-SNAPSHOT-jar-with-dependencies.jar```_

## Example

```
hotaro@hotaro-S551LB:~/IdeaProjects/GradientDescent/target$ cat /tmp/test.csv
0.619056212090985,2.61905621209099
0.0961672315347357,2.09616723153474
0.112253623587218,2.11225362358722
0.0867928684192023,2.0867928684192
<...>

hotaro@hotaro-S551LB:~/IdeaProjects/GradientDescent/target$ java -jar GradientDescent-0.1-SNAPSHOT-jar-with-dependencies.jar spark://192.168.0.106:7077 /tmp/test.csv
Set mode to SPARK_CLUSTER at spark://192.168.0.106:7077
Configuring Spark...
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
18/04/29 18:59:10 WARN Utils: Your hostname, hotaro-S551LB resolves to a loopback address: 127.0.1.1; using 192.168.0.106 instead (on interface enp2s0)
18/04/29 18:59:10 WARN Utils: Set SPARK_LOCAL_IP if you need to bind to another address
18/04/29 18:59:10 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Importing file /tmp/test.csv
File import time: 973 ms
Dataset size: 20
theta0 = 2,037922, theta1 = 0,882634
y = 0,882634 * x + 2,037922
Took 59220 ms to calculate.
```

## Troubleshooting

In case of some weird exceptions/errors being thrown, please check the following:

- If it is possible to **allocate the required amount of memory** being asked by ```--set-worker-memory```
- If the dataset is **normalized** and/or won't cause overflow while computation process
- If the Spark Master being asked by ```<master url>``` **is running**
- If the required ```<csv filename>``` **is available for all cluster nodes** (e.g. copied to each node to the specified path which is the same for all nodes, or the file is on a remote disk being available under the same path for all nodes)
- If the ```GDA_JAR_FILE``` environment variable **is exported** and contains the path to ```GradientDescent.jar``` (the prompt will appear if this variable is not exported). This is required for Spark to be able to (de)serialize custom classes correctly. Currently this is the only solution to this problem implemented in the software.
