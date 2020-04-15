# odsc-east-2020-decision-intelligence
This is the home of the 2020 Open Data Science Conference workshop (Creating Streaming Predictive Analytics and Decision Intelligence Systems with Apache Spark)

## Workshop Material
http://bit.ly/learn-spark-ml

The workshop material is a procedural series of Zeppelin notebooks. Everything can be installed and run inside of the Docker environment that is described in the README.md in the Learn Spark ML.

## Slides and Presentation Notes
Slides are in the `presentation` directory

# Streaming Predictions

## Predicting KidSafe Content from Netflix Movies
The source code is under `/spark-structured-streaming`

## PreReqs for running the streaming example
1. You have already gone through and run All the notebooks from http://bit.ly/learn-spark-ml. This will prime the redis keys necessary to run the streaming example.
2. maven installed (I run maven `3.3.9`) - install with HomeBrew (`brew install maven@3.3`)
3. java version 1.8.0 (I run `1.8.0_241`)
4. scala version 2.11 (I run `2.11.12`)

### Build the Jar
~~~
mvn clean verify
~~~

### Run the Spark App
~~~bash
export SPARK_HOME=/path/to/spark-2.4.5
$SPARK_HOME/bin/spark-submit \
  --master "local[8]" \
  --class "com.twilio.learn.PredictionStream" \
  target/spark-redis-predict.jar \
  conf/app.yaml
~~~

Alternatively if SPARK_HOME is set and you have Spark-2.4.5 installed
~~~
scripts/run.sh
~~~

### Send Movies to be Predicted
First open up a new terminal window and connect to the Redis docker instance to monitor redis
~~~
docker exec -it redis5 redis-cli monitor
~~~

Next open up another terminal window to use the redis-cli
~~~
docker exec -it redis5 redis-cli monitor
~~~

Lastly paste the following commands into the terminal
~~~
xadd v1:movies:test:kidSafe * show_id 80115338
xadd v1:movies:test:kidSafe * show_id 80196367
~~~

You should see the following
~~~
1586918227.329652 [0 172.23.0.1:42906] "HMSET" "v1:movies:test:kidSafe:predict:80196367" "category" "Thrillers" "prediction" "0.0022742774331638237" "rating" "TV-MA"
1586918227.329962 [0 172.23.0.1:42862] "HMSET" "v1:movies:test:kidSafe:predict:80115338" "category" "Kids' TV" "rating" "TV-Y" "prediction" "0.9772088004695866"
~~~

Now you are a machine learning expert
