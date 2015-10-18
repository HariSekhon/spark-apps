//
//  Author: Hari Sekhon
//  Date: 2015-05-31 23:53:29 +0100 (Sun, 31 May 2015)
//
//  vim:ts=2:sts=2:sw=2:et
//
//  https://github.com/harisekhon/spark-apps
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
//
//  http://www.linkedin.com/in/harisekhon
//

// Spark Streaming Kafka => Elasticsearch Application
// 
// For other Elasticsearch indexing programs for Hive & Pig see:
//
// https://github.com/harisekhon/toolbox

package HariSekhon.Spark

import HariSekhon.Utils._
import org.elasticsearch.spark._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext._
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.commons.cli.OptionBuilder


object KafkaToElasticsearch {

  def main(args: Array[String]) {

    ElasticsearchOptions
    
    //
    //OptionBuilder.withLongOpt("zookeepers")
    //OptionBuilder.withDescription("ZooKeeper node list, comma separated with optional port numbers after each host (eg. node1:9200,node2:9200,...). Required")
    //OptionBuilder.hasArg()
    //OptionBuilder.withArgName("zoo1,zoo2,...")
    //OptionBuilder.isRequired()
    //options.addOption(OptionBuilder.create("Z"))

    OptionBuilder.withLongOpt("kafka-brokers")
    OptionBuilder.withDescription("Kafka broker list, comma separated with optional port numbers after each host (eg. kafka1:9092,kafka2:9092,...). Required")
    OptionBuilder.hasArg()
    OptionBuilder.withArgName("kafka1,kafka2,...")
    OptionBuilder.isRequired()
    options.addOption(OptionBuilder.create("K"))
    //
    OptionBuilder.withLongOpt("topic")
    OptionBuilder.withDescription("Kafka topic to read from")
    OptionBuilder.hasArg()
    OptionBuilder.withArgName("topicname")
    OptionBuilder.isRequired()
    options.addOption(OptionBuilder.create("T"))
    //
    //OptionBuilder.withLongOpt("consumer-group")
    //OptionBuilder.withDescription("Kafka consumer group")
    //OptionBuilder.hasArg()
    //OptionBuilder.withArgName("group")
    //OptionBuilder.isRequired()
    //options.addOption(OptionBuilder.create("G"))
    //val num_partitions_to_consume = args(2)
    //OptionBuilder.withLongOpt("num-partitions")
    //OptionBuilder.withDescription("Kafka number of partitions to consume")
    //OptionBuilder.hasArg()
    //OptionBuilder.withArgName("num")
    //OptionBuilder.isRequired()
    //options.addOption(OptionBuilder.create("N"))
    
    val cmd = get_options(args)

    // this doesn't supported properly chrooted Kafka installs, must create new validation methods
    //val zk_list = validate_nodeport_list(args(0))
    //val zk_list = args(0);
    val kafka_brokers = cmd.getOptionValue("K")
    val topic = cmd.getOptionValue("T")
    // TODO: validate these are sane
    //val consumer_group = cmd.getOptionValue("G")
    //val num_partitions_to_consume = cmd.getOptionValue("N")
    
    // TODO: input validation of index/type
    val index: String = cmd.getOptionValue("i")
    val es_type: String = if (cmd.hasOption("y")) {
      cmd.getOptionValue("y")
    } else {
      index
    }
    //val es_nodes = validate_nodeport_list(args(2))
    val es_nodes: String = if(cmd.hasOption("E")){
      cmd.getOptionValue("E")
    } else {
      "localhost:9200"
    }
    // TODO: in testing this makes little difference to performance, test this more at scale
    val do_count: Boolean = cmd.hasOption("c")
    val parser: String = if (cmd.hasOption("parser")) {
      cmd.getOptionValue("parser")
    } else {
      "Parser"
    }
    // must know the doc type for Kryo efficient serialization
    // TODO: try to find a way to not have to specify this, eg. Kryo the ElasticsearchDocument trait instead
    //val es_doc: String = if(cmd.hasOption("esDocClass")){
    //  cmd.getOptionValue("esDocClass")
    //} else {
    //  "FileLineDocument"
    //}

    val no_kryo = if (cmd.hasOption("safe-serialization")) {
      true
    } else {
      false
    }

    // TODO: proper input validation of path dir/file/globs and <index/type> using my java lib later 
    if (kafka_brokers == null) {
      usage("-K / --kafka-brokers not defined")
    }
    if (topic == null) {
      usage("-T / --topic not defined")
    }
    if (es_nodes == null) {
      usage("-E / --es-nodes not defined")
    }
    if (index == null) {
    	usage("-I / --index not set")
    }
    validate_nodeport_list(kafka_brokers)
    validate_nodeport_list(es_nodes)
    
    // by default you will get tuple position field names coming out in Elasticsearch (eg. _1: line, _2: content), so use case classes
    case class Line(line: String)
    case class DateLine(date: String, line: String)
    
    // AppName needs to be short since it gets truncated in Spark 4040 job Web UI
    val conf = new SparkConf().setAppName("Kafka=>ES:" + index)
    //conf.set("spark.serializer", classOf[org.apache.spark.serializer.KryoSerializer].getName)
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    // enforce registering Kryo classes, don't allow sloppiness, doing things at high scale performance tuning matters
    conf.set("spark.kryo.registrationRequired", "true")
    conf.registerKryoClasses(
      Array(
        classOf[String],
        classOf[Array[String]],
        classOf[Line],
        classOf[DateLine]
        //classOf[Array[Object]]
      )
    )
    conf.set("es.resource", index)
    conf.set("es.nodes", es_nodes)
    conf.set("spark.speculation", "false")
    //conf.set("es.query", "?q=me*")
    conf.set("spark.streaming.receiver.writeAheadLog.enable", "true")
    //def functionToCreateContext(): StreamingContext = {
      //val ssc = new StreamingContext(conf, Seconds(1))
      //val lines = ssc.socketTextStream(...) // create DStreams
      //...
      //ssc.checkpoint(checkpointDirectory)
      //ssc
    //}

    //val ssc = StreamingContext.getOrCreate(checkpointDirectory, functionToCreateContext _)
    
    // this is the old API, switching to using the new experimental API in Spark 1.3.1 should prevent dups on worker failures
    //val numInputDStreams = 3
    //val kafkaDStreams = (1 to numInputDStreams).map { _ => KafkaUtils.createStream(...) }
    //val kafkaParams = Map("group.id" -> "KafkaToElasticsearch") 
    //val topics = Map("test" -> 1)
    //val kafkaStream = KafkaUtils.createStream(ssc, zk_list, kafkaParams, topics, StorageLevel.MEMORY_AND_DISK)
    /*
    val kafkaParams = Map("metadata.broker.list" -> broker_list)

    // new Direct API
    val kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](streamingContext, kafkaParams, topics)
    
    val lines = kafkaStream
    //val lines = sc.textFile(path).cache()
    // thought I was on to something with lines.name but this returns the file glob, not the individual file names so
    // must go lower level to Hadoop InputFormat to allow us to index the originating filename as textFile() doesn't allow for this
   
    //println("\n*** Calculating how many records are in this batch - there is overhead to this\n")
    val count = lines.count()
    println("\n*** Indexing %s records to Elasticsearch index '%s' (nodes: '%s')\n".format(count, index, es_nodes))
    
    // case class to annotate fields for indexing in Elasticsearch
    val es_map = lines.map(line => {
      // TODO: implement date parsing and make use of DateLine if possible for Elasticsearch range time queries
      // if we find don't find the date (date == null) create only Line class not DateLine which would end up being
      if (true) {
        //println("sending only line")
        Line(line)
      } else {
        //println("sending date + line")
        DateLine(null, line)
      }
    })
    es_map.saveToEs(index)

    lines.unpersist()
    ssc.start()
    ssc.awaitTermination()
    */
  }

}
