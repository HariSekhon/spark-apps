//
//  Author: Hari Sekhon
//  Date: Sun Sep 27 18:01:04 BST 2015
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

// Spark Kafka => HDFS Application for writing from Kafka to HDFS or local as text files
// 
// See also:
// 
// Spark HDFS => Elasticsearch & Spark Streaming Kafka => Elasticsearch Applications:
//
// https://github.com/harisekhon/spark-apps
//
// Hive & Pig => Elasticsearch:
//
// https://github.com/harisekhon/toolbox

package HariSekhon.Spark

import com.linkedin.harisekhon.Utils._
import org.apache.commons.cli.OptionBuilder
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext._
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
// non needed Spark 1.3+
import org.apache.spark.streaming.StreamingContext._

import scala.collection.mutable.WrappedArray
import scala.collection.mutable.WrappedArray.ofRef

import joptsimple.OptionParser;
import joptsimple.OptionSet;

object KafkaToTextFiles {

  def main(args: Array[String]) {

    // XXX: without a leading short option it fails to populate the first long opt - investigate later
    options.addOption("s", "safe-serialization", false, "Use safer Java serialization (slower). Only use this if you are getting Kryo serialization errors")
    
    OptionBuilder.withLongOpt("zookeeper")
    OptionBuilder.withDescription("Zookeeper list, comma separated with optional port numbers after each host (eg. zkhost1:2181,zkhost2:2181,zkhost3:2181,...). Required")
    OptionBuilder.hasArg()
    OptionBuilder.withArgName("zkhost1,zkhost2,...")
    OptionBuilder.isRequired()
    options.addOption(OptionBuilder.create("z"))

    OptionBuilder.withLongOpt("topic")
    OptionBuilder.withDescription("Kafka topic to read from")
    OptionBuilder.hasArg()
    OptionBuilder.withArgName("topicname")
    OptionBuilder.isRequired()
    options.addOption(OptionBuilder.create("T"))
    
    OptionBuilder.withLongOpt("path")
    OptionBuilder.withDescription("Path to write the Kafka topic data to")
    OptionBuilder.hasArg()
    OptionBuilder.withArgName("dir")
    OptionBuilder.isRequired()
    options.addOption(OptionBuilder.create("d"))
    
    OptionBuilder.withLongOpt("interval")
    OptionBuilder.withDescription("Interval at which to write out data from Kafka (default: 10)")
    OptionBuilder.hasArg()
    OptionBuilder.withArgName("secs")
    // just default to 10 secs
    //OptionBuilder.isRequired()
    options.addOption(OptionBuilder.create("i"))
    
    OptionBuilder.withLongOpt("num-threads")
    OptionBuilder.withDescription("Number of threads for topic parallelism")
    OptionBuilder.hasArg()
    OptionBuilder.withArgName("integer")
    //OptionBuilder.isRequired()
    options.addOption(OptionBuilder.create("n"))
    
    val cmd = get_options(args)
    
    val zookeepers = cmd.getOptionValue("z")
    val topics = cmd.getOptionValue("T")
    // enforce a single alnum topic for now
    val path = cmd.getOptionValue("d")
    val consumer_group = "KafkaToTextFiles"
    val num_threads_opt = if(cmd.hasOption("n")){
      cmd.getOptionValue("n")
    } else {
      "1"
    }
        
    val interval: String = if (cmd.hasOption("i")) {
      cmd.getOptionValue("i")
    } else {
      "10"
    }
    validate_nodeport_list(zookeepers)
    validate_alnum(topics, "topic")
    validate_dirname(path)
    validate_int(interval, "interval", 1, 3600)
    validate_int(num_threads_opt, "num threads", 1, 10)
    val num_threads = num_threads_opt.toInt
    val checkpointDirectory = "/tmp/spark_checkpoint_kafka_to_textfiles"
    // only allowing a single topic now
    //val topic_map = topics.split(",").map((_, num_threads.toInt)).map((topic, threads) => validate_alnum(topic, "topic")).toMap
    val topic_map = topics.split(",").map((_, num_threads.toInt)).toMap

    val no_kryo = if (cmd.hasOption("safe-serialization")) {
      true
    } else {
      false
    }
    
    // AppName needs to be short since it gets truncated in Spark 4040 job Web UI
    val conf = new SparkConf().setAppName("KafkaToTextFiles")
    if(!no_kryo){
      conf.set("spark.kryo.registrationRequired", "false")
      //conf.set("spark.serializer", classOf[org.apache.spark.serializer.KryoSerializer].getName)
      conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      // enforce registering Kryo classes, don't allow sloppiness, doing things at high scale performance tuning matters
      conf.registerKryoClasses(
        Array(
          classOf[String],
          classOf[Array[String]]
          //classOf[scala.collection.mutable.WrappedArray]
        )
      )
    }
    conf.set("spark.speculation", "false")
    conf.set("spark.streaming.receiver.writeAheadLog.enable", "true")
    def functionToCreateContext(): StreamingContext = {
      val ssc = new StreamingContext(conf, Seconds(10))
      ssc.checkpoint(checkpointDirectory)
      ssc
    }

    val ssc = StreamingContext.getOrCreate(checkpointDirectory, functionToCreateContext _)
    val kafkaStream = KafkaUtils.createStream(ssc, zookeepers, consumer_group, topic_map)

    kafkaStream.cache()
    val count = kafkaStream.count()
    println(s"*** Writing ${count} lines to ${path}")
    kafkaStream.saveAsTextFiles(path + "/data", "txt")
    ssc.start()
    ssc.awaitTermination()
  }

}