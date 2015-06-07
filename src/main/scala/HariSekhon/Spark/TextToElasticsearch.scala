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

package HariSekhon.Spark

import HariSekhon.Utils._
import org.elasticsearch.spark._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.{ FileSplit, TextInputFormat }
import org.apache.spark.rdd.HadoopRDD
import java.io.{ PrintWriter, File }
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.TextInputFormat
<<<<<<< HEAD
import org.apache.commons.cli.OptionBuilder
=======
import java.util.HashMap
>>>>>>> custom-parsers
// for Kryo serialization
import java.lang.Long

object TextToElasticsearch {

  def main(args: Array[String]) {

    // set index.refresh_interval = -1 on the index and then set back at end of job
    // actually do this in the the shell/perl script outside of code to give flexibility as ppl may not want this

    BaseOptions
    
    OptionBuilder.withLongOpt("path")
    OptionBuilder.withArgName("dir|file|glob")
    OptionBuilder.withDescription("HDFS / File / Directory path / glob to recurse and index to Elasticsearch")
    OptionBuilder.hasArg()
    OptionBuilder.isRequired()
    options.addOption(OptionBuilder.create("p"))

    val cmd = get_options(args)

    val path: String = cmd.getOptionValue("p")
    val index: String = cmd.getOptionValue("i")
    val es_type: String = if (cmd.hasOption("y")) {
      cmd.getOptionValue("y")
    } else {
      index
    }
    //val es_nodes = validate_nodeport_list(args(2))
    val es_nodes: String = cmd.getOptionValue("E")
    // TODO: in testing this makes little difference to performance, test this more at scale
    val do_count: Boolean = cmd.hasOption("c")
    val parser: String = if (cmd.hasOption("parser")) {
      cmd.getOptionValue("parser")
    } else {
      "HariSekhon.Spark.Parser"
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
    if (path == null) {
      usage("--path not set")
    }
    if (index == null) {
      usage("--index not set")
    }
    if (es_nodes == null) {
      usage("--es-nodes not set")
    }
    validate_nodeport_list(es_nodes)

    // XXX: TODO: remove this after getting reflection working
    //if(es_doc != "FileLineDocument"){
    //  if(parser == "Parser"){
    //    usage("must specify custom --parser if using custom --esDocClass")
    //  }
    //}

    // by default you will get tuple position field names coming out in Elasticsearch (eg. _1: line, _2: content), so use case classes
    // case classes for use with lower level Hadoop InputFormat - now moved to top level ElasticsearchDocuments
    // ES Hadoop also blows up if using Hadoop types in the case class
    //case class FileLine(file: Text, line: Text)
    //case class FileDateLine(file: Text, date: Text, line: Text)
    //val parserClassName = Class.forName(parser);
    //case class FileLine(path: String, offset: Long, line: String)
    //case class FileDateLine(file: String, offset: Long, date: String, line: String)

    //es_doc_class = 

    // AppName needs to be short since it gets truncated in Spark 4040 job Web UI
    val conf = new SparkConf().setAppName("Text=>ES:" + index)
    if (!no_kryo) {
      println("using Kryo serialization for efficiency")
      //conf.set("spark.serializer", classOf[org.apache.spark.serializer.KryoSerializer].getName)
      conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      // enforce registering Kryo classes, don't allow sloppiness, doing things at high scale performance tuning matters
      conf.set("spark.kryo.registrationRequired", "true")
      conf.registerKryoClasses(
        Array(
          //classOf[String],
          //classOf[Array[String]],
          //classOf[scala.Long],
          //classOf[Array[scala.Long]],
          // Kryo doesn't seem to like serializing Scala Long so use Java Long
          //classOf[Long],
          //classOf[Array[Long]],
          // Kryo also doesn't seem to like serializing Hadoop Writable types, so converting back to basic types instead
          //classOf[Text],
          //classOf[LongWritable],
          //classOf[Array[Text]],
          //classOf[Array[LongWritable]],
          //classOf[Array[Object]]
          //classOf[FileLineDocument],
          //classOf[Array[FileLineDocument]],
          //classOf[Class.forName(es_doc)]
          //classOf[ElasticsearchDocument],
          // this is not enough, as suspected must also register the inheriting document classes
          //classOf[Array[ElasticsearchDocument]],
          // TODO: XXX: this will break without the class return type being auto-determined and added here
          // what about multiple possible class returns? Must use a list of classes to be returned?? Or return a Map instead so this isn't an issue?
          classOf[FileLineDocument],
          classOf[FileDateLineDocument],
          classOf[FileOffsetLineDocument],
          classOf[FileOffsetDateLineDocument],
          // using generic Java Hashmap instead of classes, it's easier to extend
          classOf[HashMap[String, String]],
          classOf[Array[HashMap[String, String]]],
          classOf[HashMap[String, Long]],
          classOf[Array[HashMap[String, Long]]]))
          // XXX: TODO: should have to do any of this any more if getting reflection working and then using the returns() method and iterating over the list of possible return objects and then registering each one and Array[eachOne] 
    } else {
      println("not using Kryo serialization, defaulting to slower but safer Java serialization")
    }
    // this isn't doing anything, it's the 
    //nf.set("es.resource", index + "/" + es_type)
    conf.set("es.nodes", es_nodes)
    // avoid this as it will introduce unnecessary duplicates into the Elasticsearch index, trade off as we may get slowed down a little by straggler tasks
    conf.set("spark.speculation", "false")
    //conf.set("es.query", "?q=me*")
    val sc = new SparkContext(conf)
    println("Created Spark Context on Spark version %s".format(sc.version))
    //val lines = sc.textFile(path).cache()
    // thought I was on to something with lines.name but this returns the file glob, not the individual file names so
    // must go lower level to Hadoop InputFormat to allow us to index the originating filename as textFile() doesn't allow for this
    sc.hadoopConfiguration.set("mapreduce.input.fileinputformat.input.dir.recursive", "true")
    println("Reading in data from user supplied source: %s".format(path))
    val lines = sc.hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text], sc.defaultMinPartitions)
    val hadoopRdd = lines.asInstanceOf[HadoopRDD[LongWritable, Text]]
    val fileLines = hadoopRdd.mapPartitionsWithInputSplit { (inputSplit, iterator) =>
      // get the filename we're currently processing to record in Elasticsearch and strip off redundant file:/ and hdfs://nn:8020/ prefixes, not worth the extra bytes at scale
      val filepath = inputSplit.asInstanceOf[FileSplit].getPath().toString() // do this in Parser now for more flexibility: .replaceFirst("^file:", "").replaceFirst("^hdfs:\\/\\/[\\w.-]+(?:\\d+)?", "")
      iterator.map { l =>
        {
          // Keeping the offset now so you could order by offset and see the original ordering of lines in each file if wanted
          //println("\n*** INPUTSPLIT %s,%s,%s\n".format(filepath,l._1,l._2))
          //(filepath, l._1, l._2)
          // XXX: Hadoop Text formats blow up Kryo and later on ElasticSearch so convert them to basic types - review this as surely everyone else must have worked around this before without having to do what might be costly at scale conversions
          //(filepath.toString(), l._1.toString().toLong, l._2.toString())
          // Kryo serialization blows up on Hadoop types, do String conversions to work around
          // Kryo doesn't seem to like serializing Scala Long so use Java Long too
          (filepath.toString(), Long.valueOf(l._1.toString()).longValue(), l._2.toString())
        }
      }
    } // .cache() might actually slow this down it's better to re-read than to serialize to disk

    val start: Long = System.currentTimeMillis
    // ====================================================
    // XXX: TODO: instead of doing .count() can we use an accumulator here?
    var count: String = "uncounted"
    if (do_count) {
      println("\n*** Calculating how many records we are going to be dealing with - there is overhead to this as it's basically a pre-job but it allows us to check the counts in Elasticsearch later on for higher confidence in the correctness and completeness of the indexing\n")
      //val count = lines.count().toString()
      count = fileLines.count().toString()
      // this is collected not an RDD at this point, save using local file PrintWriter at the end after Spark job
      // count.saveAsTextFile("/tmp/" + path)   
    }
    // =====================================================

    println("\n*** Indexing path '%s' (%s records) to Elasticsearch index '%s' (nodes: '%s')\n".format(path, count, index, es_nodes))
    /*
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
    */
    
    // XXX: consider doing this in the new Scala Reflections API from 2.10 (marked experimental) to better support Scala types
    // primary driver for this is to allow a client of mine to use custom Java parser classes
    // this is what's been giving me java.lang.ClassNotFoundException: Parser
    //try {
    //import scala.reflect.runtime.{universe => ru}
    //def getTypeTag[T: ru.TypeTag](obj: T) = ru.typeTag[T]
    //val parserA = getTypeTag(parser).tpe
    //val m = ru.runtimeMirror(getClass.getClassLoader)
    //val classLoader = MainClass.class.getClassLoader();
    val parserClass = Class.forName(parser)
    //val parserInstance = parserClass.newInstance().asInstanceOf[Class[parserClass.getName]]
    //val params = Array[Class] // classOf[String], classOf[Long], classOf[String]]
    //params[0] = classOf[String]
    //params[1] = classOf[Long]
    //params[2] = classOf[String]
    //val Parser = parserClass.getDeclaredMethod("parse", classOf[Parser])
    println("Parser = " + parserClass.getName())
    val classLoader = java.lang.ClassLoader.getSystemClassLoader()
    //val parserInstance = classLoader.loadClass(parserClass)
    //val parserInstance = Class.forName(parser).getConstructor().newInstance() // parser.newInstance()(l._1, l._2, l._3)
    //val params = Array[classOf[String], classOf[Long], classOf[String]]
    //val parse = cls.getDeclaredMethod("parse", params)
    val parserInstance2 = Class.forName(parser).getConstructor().newInstance()
    val parserInstance = parserInstance2.asInstanceOf(parserClass)

    //} catch {
    //  case e: ClassNotFoundException => e.printStackTrace();
    //}
    
    // case class to annotate fields for indexing in Elasticsearch
    //val es_map = lines.map(fileLine => {
    val es_map = fileLines.map(l => {
      // TODO: add Date formats parsing and add date conversion and date field if suitable date is found
      // XXX: only use this for debugging at small scale on your laptop!
      //println("\n*** path: '%s', offset: '%s', line: '%s'\n".format(l._1, l._2, l._3))
      // converting earlier in the pipeline now to avoid Kryo serialization errors
      // last minute conversions to prevent org.elasticsearch.hadoop.serialization.EsHadoopSerializationException, did earlier conversions in fileLines mapPartitionsWithInputSplit and changed case class instead
      //FileLine(l._1.toString(), Long.valueOf(l._2.toString()).longValue(), l._3.toString())
      //HariSekhon.Spark.Parser.parse(l._1.toString(), Long.valueOf(l._2.toString()).longValue(), l._3.toString())
      //parserInstance.parse(_, _, _)
      //parserInstance.parse(l._1.toString(), Long.valueOf(l._2.toString()).longValue(), l._3.toString())
      parse.invoke(l._1.toString(), Long.valueOf(l._2.toString()).longValue(), l._3.toString())
    })
    es_map.saveToEs(index + "/" + es_type)

    lines.unpersist()
    // TODO: do elasticsearch query vs count reporting here
    // the high level API would pull all data through RDD whereas I just want the hit count header on first page of 10 results
    // maybe handle this in calling script instead
    sc.stop()
    // TODO: tie in with count above to be an optional switch
    val count_file = index.replaceAll("[^A-Za-z0-9_-]", "_")
    val secs = (System.currentTimeMillis - start) / 1000
    println("\n*** Finished indexing path '%s' (%s records) to Elasticsearch index '%s' (nodes: %s) in %d secs\n".format(path, count, index, es_nodes, secs))
    // leave count file with contents "uncounted" as a placeholder to reduce confusion
    if (count != "uncounted") {
      println("writing index count for index '%s' to /tmp/%s.count for convenience for cross referencing later if needed\n".format(index, count_file))
    } else {
      println("writing uncounted placeholder file for index '%s' to /tmp/%s.count\n".format(index, count_file))
    }
    // Still write the file for uniformity
    // raises FileNotFoundException at end of job when using globs for Spark's textFile(), use index name converted for filename safety instead
    val pw = new PrintWriter(new File("/tmp/" + count_file + ".count"))
    pw.write(count.toString)
    pw.close()
    //}

  }

}
