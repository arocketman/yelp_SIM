import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.{ReadConfig, WriteConfig}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.functions.lit
import org.graphframes.GraphFrame

/**
  * Runs the LPA calculations over the entire dataset
  */
object LPAcalc {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("SparkGrep").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val sparkSess = SparkSession.builder()
      .master("local")
      .config(conf)
      .appName("MongoSparkConnectorIntro")
      .config("log4j.rootCategory", "WARN")
      .config("spark.mongodb.input.uri", "mongodb://127.0.0.1:27017/db.db")
      .config("spark.mongodb.output.uri", "mongodb://127.0.0.1:27017/db.db")
      .config("spark.sql.shuffle.partitions", 10)
      .getOrCreate()

    val customReadConfig = ReadConfig(Map("uri" -> "mongodb://127.0.0.1:27017/", "database" -> "ok", "collection" -> "ok", "readPreference.name" -> "secondaryPreferred"))
    val customWriteConfig = WriteConfig(Map("uri" -> "mongodb://127.0.0.1:27017/" , "database" -> "ok","collection"->"lpa","readPreference.name" -> "secondaryPreferred"))

    val read_piccolo = MongoSpark.load(sparkSess, customReadConfig)
    val sqlContext = sparkSess.sqlContext

    //Creazione delle relazioni del grafo, gli edges E.
    val user_friends = read_piccolo.select("user_id", "friends")
    val exploded_friends = user_friends.withColumn("friend", org.apache.spark.sql.functions.explode(user_friends.col("friends"))).drop("friends")
    val new_col_names1 = Seq("src", "dst")
    val friends_rel = exploded_friends.toDF(new_col_names1: _*)
    val cached_users_t = read_piccolo.select("user_id","name","impatto","friends").toDF("id","name","impatto","friends")

    cached_users_t.createOrReplaceTempView("cache1")
    friends_rel.createOrReplaceTempView("cache2")
    sqlContext.cacheTable("cache1")
    sqlContext.cacheTable("cache2")

    val grafo_t = GraphFrame(cached_users_t,friends_rel)
    val result = grafo_t.labelPropagation.maxIter(1).run();
    MongoSpark.save(result,customWriteConfig)
  }
}
