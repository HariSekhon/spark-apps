//
//  Author: Hari Sekhon
//  Date: 2015-05-25 19:39:56 +0100 (Mon, 25 May 2015)
//
//  vim:ts=2:sts=2:sw=2:et
//
//  https://github.com/harisekhon/spark-to-elasticsearch
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
//
//  http://www.linkedin.com/in/harisekhon
//

// Generic Spark port of my Pig => Elasticsearch unstructured text / log files Hadoop job
// 
// based off 'pig-text-to-elasticsearch.pig' (see my Pig / Hive => Solr / Elasticsearch freebies in my public toolbox:
//
// https://github.com/harisekhon/toolbox

import org.elasticsearch.spark._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
//import org.elasticsearch.node.NodeBuilder._
import java.io.{ PrintWriter, File }

object TextToElasticsearch {

  def main(args: Array[String]) {

    // copy elasticsearch.yml to classpath to get the cluster name to join
    // find and add ./elasticsearch.yml, then /etc/elasticsearch/elasticsearch.yml, then /usr/local/elasticsearch/config/elasticsearch.yml to
    // classpath automatically with preference to local directory
    // set index.refresh_interval = -1 as the index and then set back at end of job
    // actually do this in the the shell script outside of code to give flexibility as ppl may not want th

    if (args.length < 4) {
      println("usage: TextToElasticsearch </path/to/*.log> <Elasticsearch_Cluster_Name> <index>/<type> <Elasticsearch,node,list,comma,separated>")
      System.exit(3)
    }

    // TODO: input validation of path globs, cluster name, index/type and nodes against hosts/IPs
    val path = args(0)
    val es_cluster_name = args(1)
    val index = args(2)
    val es_nodes = args(3)
    
    // by default you will get tuple prefixes coming out in Elasticsearch (eg. _1: line, _2: content), so use case classes
    case class Line(line: String)
    case class DateLine(date: String, line: String)
    
    // AppName needs to be short since it gets truncated in Spark 4040 job Web UI
    val conf = new SparkConf().setAppName("Text=>ES:" + index)
    //conf.set("spark.serializer", classOf[org.apache.spark.serializer.KryoSerializer].getName) 
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    // enforce registering Kryo classes, don't allow sloppiness, doing things at high scale performance tuning matters
    conf.set("spark.kryo.registrationRequired", "true" )
    conf.registerKryoClasses(
        Array(
          classOf[Line], 
          classOf[DateLine],
          classOf[Array[Object]]
        )
    )
    //conf.setOutputFormatClass(EsOutputFormat.class)
    //conf.set("mapred.output.format.class", "org.elasticsearch.hadoop.mr.EsOutputFormat")
    //conf.setOutputCommitter(classOf[FileOutputCommitter])
    //conf.set(ConfigurationOptions.ES_RESOURCE_WRITE, index)
    //conf.set(ConfigurationOptions.ES_NODES, es_nodes)
    conf.set("es.resource", index)
    conf.set("es.nodes", es_nodes)
    //conf.set("es.query", "?q=me*")
    val sc = new SparkContext(conf)
    val lines = sc.textFile(path).cache()
    println("calculating how many records we are going to be dealing with")
    val count = lines.count()
    // this is collected not an RDD at this point
    // count.saveAsTextFile("/tmp/" + path)
    println("Indexing %s (%s records) to Elasticsearch index '%s' (nodes: '%s')".format(path, count, index, es_nodes))
    val es_map = lines.map(line => {
      // TODO: implement date parsing and make use of DateLine if possible for Elasticsearch range time queries
      // if we find don't find the date (date == null) create only Line class not DateLine which would end up being
      if(true){
        println("sending only line")
        Line(line)
      } else {
        println("sending date + line")
        DateLine(null, line)
      }
    })
    es_map.saveToEs(index)

    // Don't do this use the high level Spark API provided by Elastic company
    //
    //println("Instantiating Elasticsearch native cluster node to be a direct routing client")
    //val node = nodeBuilder().clusterName(es_cluster_name).client(true).node()
    //val client = node.client()
    //node.close()
    // do per partition iteration on RDDs to ameliorate cost of client creation
    //val json = "{ \"line\": " + "}" 

    lines.unpersist()
    // TODO: do elasticsearch query vs count reporting here
    // the high level API would pull all data through RDD whereas I just want the hit count header on first page of 10 results
    // maybe handle this in calling script instead
    sc.stop()

    // raises FileNotFoundException at end of job when using globs for Spark's textFile()
    val count_file = index.replaceAll("[^A-Za-z0-9_-]", "_")
    println("Finished, writing index count for index '%s' to /tmp/%s.count for convenience for cross referencing later if needed".format(index, count_file))
    val pw = new PrintWriter(new File("/tmp/" + count_file + ".count"))
    pw.write(count.toString)
    pw.close()

  }

}