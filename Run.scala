// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC ## MongoDB connection and execution of a code

// COMMAND ----------

import com.mongodb.spark._
import org.apache.spark.sql.SparkSession
import com.mongodb.spark.config._
import org.apache.spark.sql.functions._


val sparkSess = SparkSession.builder()
  .master("local")
  .appName("MongoSparkConnectorIntro
  .getOrCreate()

val customReadConfig = ReadConfig(Map("uri" -> "mongodb:/url:27017/" , "database" -> "ok","collection"->"lpa","readPreference.name" -> "secondaryPreferred"))

val df = MongoSpark.load(sparkSess,customReadConfig)

val grouped = df.groupBy("label")
val z = grouped.count.sort(desc("count")).take(5)

val read_piccolo = df.filter("LABEL == 68719486681")

//Eges
val user_friends = read_piccolo.select("id","friends");
val exploded_friends = user_friends.withColumn("friend", org.apache.spark.sql.functions.explode(user_friends.col("friends"))).drop("friends")
val new_col_names1 = Seq("src","dst")
val friends_rel = exploded_friends.toDF("src","dst")

//Vertices
val utenti_def = read_piccolo.withColumn("attivo",lit(false : Boolean)).select("id","impatto","attivo")

// COMMAND ----------

import org.graphframes._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.graphx._

val grafo_f = GraphFrame(utenti_def,friends_rel)
val grafo = grafo_f.toGraphX
//Mapping Dataframe -> RDD per lavorare da GraphFrame -> GraphX
val vertici : RDD[(VertexId, (String, Boolean, Double))]= grafo.vertices.map(p => (p._1.asInstanceOf[VertexId], (p._2.getAs[String](0), p._2.getAs[Boolean](2), p._2.getAs[Double](1))))
val archi : RDD[Edge[String]] = grafo.edges.map(p => Edge(p.srcId,p.dstId,"friends"))


// COMMAND ----------

//Running pagerank

val results_pagerank : GraphFrame = grafo_f.pageRank.resetProbability(0.15).maxIter(1).run()

// COMMAND ----------

//Creating the new graph with GraphX
val graffone = Graph(vertici,archi)

// COMMAND ----------

//Grabbing the first vertex
results_pagerank.vertices.first

// COMMAND ----------

//Using the first vertex to start the games
 val initialMsg: (String, Double) = ("ypm9IB9iZkwY71FKkYeLkg",9999.0)

  def rcvMsg(vertexId: VertexId, nodoCorrente: (String,Boolean, Double), message: (String,Double)): (String,Boolean, Double) = {
    val nomeNodoCorrente = nodoCorrente._1
    if (message == initialMsg) {
      //I activate myself only if my name is equal to the initiator name
      if(nomeNodoCorrente.equals(initialMsg._1)) {
        println("Messaggio iniziale preso da " + nodoCorrente)
        //Metto true perchè il nodo iniziatore deve attivarsi.
        return (nodoCorrente._1, true, nodoCorrente._3)
      }
      return nodoCorrente
    }
    else{
      if(Math.random() < message._2){
        println("Nodo" + nodoCorrente + ", influenzato da : " + message)
        (nodoCorrente._1,true,nodoCorrente._3)
      }
      else
        nodoCorrente
    }
  }

  def sendMsg(triplet: EdgeTriplet[(String,Boolean, Double), String]): Iterator[(VertexId, (String,Double))] = {
    val sourceVertex = triplet.srcAttr
    val sorgenteAttiva : Boolean = triplet.srcAttr._2
    val destinazioneAttiva : Boolean = triplet.dstAttr._2
    //If this node is inactive or the destination node is already active I'll avoid sending at all.
    if (!sorgenteAttiva || destinazioneAttiva)
      Iterator.empty
    else {
      Iterator((triplet.dstId, (triplet.srcAttr._1,sourceVertex._3)))
    }
  }

  def mergeMsg(msg1: (String,Double), msg2: (String,Double)): (String,Double) = {
    if(Math.random() < msg1._2 || Math.random() < msg2._2)
      return (msg1._1,1f)
    return (msg1._1,0f)
  }



// COMMAND ----------

var conteggio : Long = 0

// COMMAND ----------

graffone.cache()
val minGraph = graffone.pregel( initialMsg,
  Int.MaxValue,
  EdgeDirection.Out)(
  rcvMsg,
  sendMsg,
  mergeMsg)
//I store in a variable how many people the starting node influenced.
val influenced = minGraph.vertices.filter(p => p._2._2)
conteggio = conteggio + influenced.count.asInstanceOf[Long]

// COMMAND ----------


