package final_project

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx._
import org.apache.spark.storage.StorageLevel
import org.apache.log4j.{Level, Logger}
import scala.collection.mutable
import scala.util.Random

case class MatchedEdge(vertex1: Long, vertex2: Long)

object main {
  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  Logger.getLogger("org.spark-project").setLevel(Level.WARN)

  def greedyMatching(graphInput: Graph[Int, Int]): RDD[MatchedEdge] = {
    val matchedVertices = mutable.HashSet[VertexId]()

    // Sort edges randomly within each partition and cache the data for reusability
    val sortedEdges = graphInput.edges.map(e => (e, Random.nextDouble())).sortBy(_._2).map(_._1).cache()

    sortedEdges.mapPartitions { edges =>
      val selectedEdges = mutable.HashSet[Edge[Int]]()
      edges.foreach { edge =>
        if (!matchedVertices.contains(edge.srcId) && !matchedVertices.contains(edge.dstId)) {
          matchedVertices += edge.srcId
          matchedVertices += edge.dstId
          selectedEdges += edge
        }
      }
      selectedEdges.iterator.map(e => MatchedEdge(e.srcId, e.dstId))
    }
  }

  def maximalMatching(graph: Graph[Int, Int]): RDD[MatchedEdge] = {
    var matchedVertices = Set[Long]()
    val edges = graph.edges.collect().toList

    val matching = edges.filter { edge =>
      val u = edge.srcId
      val v = edge.dstId

      if (!matchedVertices.contains(u) && !matchedVertices.contains(v)) {
        matchedVertices += u
        matchedVertices += v
        true
      } else {
        false
      }
    }

    val sc = SparkContext.getOrCreate()
    sc.parallelize(matching.map(edge => MatchedEdge(edge.srcId, edge.dstId)))
  }

  def lineToCanonicalEdge(line: String): Edge[Int] = {
    val x = line.split(",")
    val src = x(0).toLong
    val dst = x(1).toLong
    if (src < dst) Edge(src, dst, 1) else Edge(dst, src, 1)
  }

  def saveMatching(matching: RDD[MatchedEdge], outputFile: String): Unit = {
    val spark = SparkSession.builder().getOrCreate()
    import spark.implicits._
    val df = matching.toDF("vertex1", "vertex2")
    df.coalesce(1).write.format("csv").mode("overwrite").save(outputFile)
  }

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("GraphMatching")
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder.config(conf).getOrCreate()
    import spark.implicits._

    if (args.length < 2) {
      println("Usage: Main <command> <graph_path> [output_path]")
      sys.exit(1)
    }

    val operation = args(0)
    val graphEdges = sc.textFile(args(1)).map(lineToCanonicalEdge)
    val graph = Graph.fromEdges[Int, Int](graphEdges, defaultValue = 0)

    operation match {
      case "greedy" if args.length == 3 =>
        val startTimeMillis = System.currentTimeMillis()

        val matchedEdges = greedyMatching(graph)

        val endTimeMillis = System.currentTimeMillis()
        val durationSeconds = (endTimeMillis - startTimeMillis) / 1000
        println("==================================")
        println(s"Greedy Matching algorithm completed in $durationSeconds s.")
        println("==================================")

        saveMatching(matchedEdges, args(2))

      case "luby" if args.length == 3 =>
        val startTimeMillis = System.currentTimeMillis()

        val matchedEdges = maximalMatching(graph)

        val endTimeMillis = System.currentTimeMillis()
        val durationSeconds = (endTimeMillis - startTimeMillis) / 1000
        println("==================================")
        println(s"Luby's Matching Algorithm completed in $durationSeconds s.")
        println("==================================")

        saveMatching(matchedEdges, args(2))

      case "verify" if args.length == 3 =>
        val matchedEdges = sc.textFile(args(2)).map(lineToCanonicalEdge)

        if (matchedEdges.distinct().count() != matchedEdges.count()) {
          println("The matched edges contain duplications of an edge.")
          sys.exit(1)
        }

        if (matchedEdges.intersection(graphEdges).count() != matchedEdges.count()) {
          println("The matched edges are not a subset of the input graph.")
          sys.exit(1)
        }

        val matchedGraph = Graph.fromEdges[Int, Int](matchedEdges, 0, edgeStorageLevel = StorageLevel.MEMORY_AND_DISK, vertexStorageLevel = StorageLevel.MEMORY_AND_DISK)
        if (matchedGraph.ops.degrees.aggregate(0)((x, v) => scala.math.max(x, v._2), (x, y) => scala.math.max(x, y)) >= 2) {
          println("The matched edges do not form a matching.")
          sys.exit(1)
        }

        println("The matched edges form a matching of size: " + matchedEdges.count())

      case _ =>
        println("Invalid command or number of arguments")
        sys.exit(1)
    }
  }
}
