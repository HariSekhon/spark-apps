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

// Spark Kafka => Line & Word Counts Application for monitoring a Kafka topic on the command line

package com.linkedin.harisekhon.spark

import com.linkedin.harisekhon.Utils._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext._
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
// non needed Spark 1.3+
import org.apache.spark.streaming.StreamingContext._

//import scala.collection.mutable.WrappedArray
//import scala.collection.mutable.WrappedArray.ofRef

// Apache Commons CLI is stuck on old static version compiled directly in to Spark's assembly and
// static chaining doesn't work in Scala, making arg parsing really ugly, switching to another parser
import org.apache.commons.cli.OptionBuilder
//import joptsimple.OptionParser
//import joptsimple.OptionSet

import java.lang.Integer

object NetworkCounts {

  def main(args: Array[String]) {

    /*
    val parser = new OptionParser( "h*H:P:")
    
    parser.accepts("host").withRequiredArg()
    parser.accepts("port").withRequiredArg()
    parser.accepts("interval").withRequiredArg()
    
    val options = parser.parse(args)
    
    val host_opt: String = if(options.has("host") && options.hasArgument("host")){
      String.valueOf(options.valueOf("host"))
    } else if(options.has("H") && options.hasArgument("H")){
      String.valueOf(options.valueOf("H"))
    } else {
      // usage prints apache commons builder
    	quit("-H / --host not defined"); ""
    }
    
    val port_opt: String = if(options.has("port") && options.hasArgument("port")){
    	String.valueOf(options.valueOf("port"))
    } else if(options.has("P") && options.hasArgument("P")){
    	String.valueOf(options.valueOf("P"))
    } else {
    	quit("-P / --port not defined"); ""
    }
    
    val interval_opt: String = if(options.has("interval") && options.hasArgument("interval")){
    	String.valueOf(options.valueOf("interval"))
    } else if(options.has("i") && options.hasArgument("i")){
    	String.valueOf(options.valueOf("i"))
    } else {
    	quit("-i / --interval not defined"); ""
    }
    */
    
    HostOptions
    
    OptionBuilder.withLongOpt("interval")
    OptionBuilder.withDescription("Interval at which to print counts (default: 10)")
    OptionBuilder.hasArg()
    OptionBuilder.withArgName("secs")
    // just default to 10 secs
    //OptionBuilder.isRequired()
    options.addOption(OptionBuilder.create("i"))
    
    val cmd = get_options(args)

    val host: String = if (cmd.hasOption("H")) {
      cmd.getOptionValue("H")
    } else {
      usage("-H / --host not defined"); ""
    }
    
    val port: String = if (cmd.hasOption("P")) {
      cmd.getOptionValue("P")
    } else {
      usage("-P / --port not defined"); ""
    }
    
    val interval: String = if (cmd.hasOption("i")) {
      cmd.getOptionValue("i")
    } else {
      "10"
    }
    validate_host(host)
    validate_port(port)
    validate_int(interval, "interval", 1, 3600)
    
    val checkpointDirectory = "/tmp/spark_checkpoint_kafka_network_counts"

    // AppName needs to be short since it gets truncated in Spark 4040 job Web UI
    val conf = new SparkConf().setAppName("HS.NetworkCounts")
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
    val lines = ssc.socketTextStream(host, port.toInt)
    val lineCount = lines.count()
    val words = lines.flatMap(_.split(" "))
    val wordCount = words.count()
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)
    // XXX: do not use println it prints once at start the method toString hex, but doesn't print each aggregate per window
    lineCount.print()
    wordCount.print()
    wordCounts.print()
    ssc.start()
    ssc.awaitTermination()
  }

}