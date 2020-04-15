## Predicting KidSafe Content from Netflix Movies

## PreReqs
1. maven installed (I run maven `3.3.9`) - install with HomeBrew (`brew install maven@3.3`)
2. java version 1.8.0 (I run `1.8.0_241`)
3. scala version 2.11 (I run `2.11.12`)

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
First open up a new terminal window and connect to the Redis docker instance
~~~
docker exec -it redis5 redis-cli
~~~

Next open up another terminal window to monitor redis
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
