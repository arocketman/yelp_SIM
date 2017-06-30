import org.apache.log4j.{Level, LogManager}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * A working example with fake data
  */
object PregelFake {
  val conf = new SparkConf().setAppName("SparkGrep").setMaster("local[*]")
  val sc = new SparkContext(conf)
  val sparkSess = SparkSession.builder()
    .master("local")
    .config(conf)
    .appName("MongoSparkConnectorIntro")
    .config("log4j.rootCategory", "WARN")
    .getOrCreate()

  val log = LogManager.getRootLogger
  log.setLevel(Level.WARN)

  def main(args: Array[String]): Unit = {
    val vertices: RDD[(VertexId, (String, Boolean, Float))] =
      sc.parallelize(Array((1L, ("uno", false, 0.7f)), (2L, ("due", false, 0.4f)),
        (3L, ("tre", false, 0.5f)), (4L, ("quattro", false, 0.6f)), (5L, ("cinque", false, 0.6f))))

    val relationships: RDD[Edge[String]] =
      sc.parallelize(Array(Edge(1L, 2L, "friends"), Edge(2L, 1L, "friends"), Edge(1L, 3L, "friends"), Edge(3L, 1L, "friends"),
        Edge(2L, 4L, "friends"), Edge(4L, 2L, "friends"), Edge(3L, 4L, "friends"), Edge(4L, 3L, "friends"), Edge(1L, 5L, "friends"), Edge(5L, 1L, "friends")))

    // Create the graph
    val graffone = Graph(vertices, relationships)


    val initialMsg = ("due", 9999f)

    //Value : Boolean (mi sono attivato o no) , Float(Il valore di impatto)
    def rcvMsg(vertexId: VertexId, value: (String, Boolean, Float), message: (String, Float)): (String, Boolean, Float) = {
      val log = LogManager.getRootLogger
      log.setLevel(Level.ERROR)

      if (message == initialMsg || value._2) {
        //Check if my node is equal to the initiator node, if so I activate myself
        if (value._1.equals(initialMsg._1)) {
          return (value._1, true, value._3)
        }
        value
      }
      else {
        val x = Math.random().toFloat;
        // println("Valore di random : " + x)
        if (x < message._2) {
          println("I am  " + value._1 + " , I was influenced by " + message._1 + " because I received " + message + " random was : " + x + "my last value was : " + value)
          (value._1, true, value._3)
        }
        else
          value
      }
    }

    def sendMsg(triplet: EdgeTriplet[(String, Boolean, Float), String]): Iterator[(VertexId, (String, Float))] = {
      val sourceVertex = triplet.srcAttr
      val sourceIsActive: Boolean = triplet.srcAttr._2
      val log = LogManager.getRootLogger
      if (!sourceIsActive || triplet.dstAttr._2)
        Iterator.empty
      else {
        println("I am the vertex: " + triplet.srcAttr._1 + " , My destination is' : " + triplet.dstAttr._1 + " , My value is : " + sourceVertex._3)
        Iterator((triplet.dstId, (triplet.srcAttr._1, sourceVertex._3)))
      }
    }

    def mergeMsg(msg1: (String, Float), msg2: (String, Float)): (String, Float) = {
      if (Math.random() < msg1._2)
        return (msg1._1, 1f)
      else if (Math.random() < msg2._2)
        return (msg2._1, 1f)
      return (msg1._1, 0f)
    }


    graffone.cache()
    val minGraph = graffone.pregel(initialMsg,
      Integer.MAX_VALUE,
      EdgeDirection.Out)(
      rcvMsg,
      sendMsg,
      mergeMsg)


    val count = minGraph.vertices.filter(p => p._2._2)
    println(count.count())

  }
}
