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

package com.linkedin.harisekhon.spark

import com.linkedin.harisekhon.Utils._
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

object KafkaToHDFS {

  // enforce hdfs:// prefix - leave viewfs://
  
  def main(args: Array[String]) {

    // TODO: ZooKeeper or Kafka brokers, topic name, consumer group, num partitions

    val kafka_brokers = "localhost:9092"
    val zookeepers = "localhost:2181"
    val topics = "test"
    val consumer_group = "KafkaCounts"
    val num_threads = 1
    val path = "/tmp/kafka_to_textfiles"
    val topic_map = topics.split(",").map((_, num_threads.toInt)).toMap
    val checkpointDirectory = "/tmp/spark_checkpoint_kafka_to_textfiles"
    validate_nodeport_list(zookeepers)

    // AppName needs to be short since it gets truncated in Spark 4040 job Web UI
    val conf = new SparkConf().setAppName("KafkaToTextFiles")
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
