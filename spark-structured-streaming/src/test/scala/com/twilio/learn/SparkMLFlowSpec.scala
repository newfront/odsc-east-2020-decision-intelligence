package com.twilio.learn

import com.twilio.learn.PredictionStream
import com.twilio.learn.ml.{IsKidSafeTransformer, RatingClassificationTransformer, Transformers}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{OneHotEncoderEstimator, StringIndexer, VectorAssembler}
import org.apache.spark.sql.DataFrame
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{FunSuite, Matchers}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.spark.sql.streaming.{DataStreamReader, OutputMode, StreamingQuery, Trigger}
import org.slf4j.{Logger, LoggerFactory}

class SparkMLFlowSpec extends FunSuite with Matchers with SharedSparkSql {

  val logger: Logger = LoggerFactory.getLogger(classOf[SparkMLFlowSpec])

  override def conf: SparkConf = {
    new SparkConf()
      .setMaster("local[*]")
      .setAppName("command-controller-woot")
      .set("spark.sql.session.timeZone", "UTC")
      .set("spark.app.id", appID)
      .set("spark.driver.host", "localhost")
      .set("spark.sql.shuffle.partitions", "32")
      .set("spark.ui.enabled", "false")
      .set("spark.redis.host", "localhost")
      .set("spark.redis.port", "6379")
      .setJars(SparkContext.jarOfClass(classOf[PredictionStream]).toList)
  }

  test("should run the whole stage") {
    import org.apache.spark.ml.feature.{IndexToString, StringIndexer, OneHotEncoderEstimator, VectorAssembler}
    import org.apache.spark.ml.Pipeline
    import org.apache.spark.ml.clustering.KMeans
    import org.apache.spark.ml.evaluation.ClusteringEvaluator

    val spark = sparkSql

    val netflixCategoryDataWithRatingSchema = "`show_id` BIGINT,`category` STRING, `rating` STRING"
    val netflixCategoryStructSchema = StructType.fromDDL(netflixCategoryDataWithRatingSchema)

    val contentWithCategories = spark.read
      .format("org.apache.spark.sql.redis")
      .schema(netflixCategoryStructSchema)
      .option("key.column", "show_id")
      .option("table", "netflix_category_rating")
      .load()

    contentWithCategories.printSchema

    /*
    // 1. call this to fit changes in the pipeline - it will save into src/test/resources/pipeline
    // 2. store the fitted pipeline in
    fitPipeline(contentWithCategories)
     */

    /*import org.apache.spark.ml.PipelineModel
    val defrostedPipeline = PipelineModel.load("src/test/resources/pipeline")
    val transformed = defrostedPipeline.transform(contentWithCategories)
    // move to the conf directory if you want to test changing the pipeline (since the Transformers class is expecting it there)
    */

    /* Create the Structure of the Data we will read from the Redis Stream */

    /*
    val streamDataDDL = "`show_id` BIGINT"
    val netflixShowIdSchema = StructType.fromDDL(streamDataDDL)

    val processingTimeTrigger = Trigger.ProcessingTime("2 seconds")

    // create the stream reader (this isn't active until we tell it to start())
    val inputStream: DataStreamReader = spark
      .readStream
      .format("redis")
      .option("stream.keys", "v1:movies:test:kidSafe")
      .schema(netflixShowIdSchema)

    val query: StreamingQuery = inputStream
      .load()
      .join(contentWithCategories, Seq("show_id"))
      .transform(Transformers.transform)
      .transform(Transformers.predict)
      .writeStream
      .format("memory")
      .queryName("predictions")
      .outputMode(OutputMode.Append())
      .trigger(processingTimeTrigger)
      .start()

    query.processAllAvailable()

    query.stop()
    */

  }

  def fitPipeline(df: DataFrame): Unit = {
    val categoryIndexer = new StringIndexer()
      .setHandleInvalid("keep") // options are keep, skip, error
      .setInputCol("category")
      .setOutputCol("category_index")

    val ratingIndexer = new StringIndexer()
      .setHandleInvalid("keep")
      .setInputCol("rating")
      .setOutputCol("rating_index")

    val fittedCategoryIndexer = categoryIndexer.fit(df.select("category").distinct())
    val fittedRatingIndexer = ratingIndexer.fit(df.select("rating").distinct())

    val encoder = new OneHotEncoderEstimator()
      .setInputCols(Array("category_index","rating_index", "rating_class"))
      .setOutputCols(Array("category_vec", "rating_vec", "rating_class_vec"))

    val vectorAssembler = new VectorAssembler()
      .setInputCols(Array("category_vec","rating_vec", "rating_class_vec"))
      .setOutputCol("features")

    val transformationPipeline = new Pipeline()
      .setStages(
        Array(
          fittedRatingIndexer,
          fittedCategoryIndexer,
          new RatingClassificationTransformer,
          new IsKidSafeTransformer,
          encoder,
          vectorAssembler
        )
      )

    val fittedPipeline = transformationPipeline.fit(df)
    fittedPipeline.write.overwrite.save("src/test/resources/pipeline")
  }

}
