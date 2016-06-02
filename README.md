Spark Apps (Scala)
==================
[![Build Status](https://travis-ci.org/HariSekhon/spark-apps.svg?branch=master)](https://travis-ci.org/HariSekhon/spark-apps)
![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20OS%20X-lightgrey.svg)

### Spark => Elasticsearch Indexer

Note: this is functional but early stages and as such isn't productionized yet as there is a lot of code cleanup, refactoring, unit tests and validation functions to be added.

Generic Spark to Elasticsearch indexing application written in Scala to provide fast scalable Full-Text search on your Hadoop "Big Data" cluster's HDFS contents.
<!--
Spark Applications written in Scala:

- ```HDFS/File => Elasticsearch indexer``` - provides fast Full-Text search on data stored in Hadoop (can also be used to index local filesystem data it's just a prefix hdfs:// or file://)
- ```Kafka     => Elasticsearch indexer``` - provides near real-time Full-Text search
- ```Kafka     => HDFS/File writer``` - writes data out of Kafka to HDFS or local filesystem as Text files
- Network Socket => Word/Line Counts (Spark Streaming toy app)

Provides 2 ready-to-run Applications:

1. Spark Streaming from Kafka to Elasticsearch. This is [near] real-time indexing.

2. Batch indexing of Hadoop HDFS text/compressed files with file path and offset, or even local files depending on the Spark setup and specified URI prefix since the file access method is abstracted behind a Hadoop InputFormat.
-->
This is based off my Pig & Hive freebies for indexing structured and unstructured data in Hadoop to Elasticsearch & Solr/SolrCloud, see my adjacent [Toolbox repo](https://github.com/harisekhon/toolbox) for those programs.

Includes Kryo serialization optimization, and pluggable parsers, as well as option to count records for reporting and comparison. Serialization optimization can be disabled with a command line switch to use the slower but slightly more robust Java serialization.

Still on the todo list is adding varied date detection + parsing from which to create a time range query-able field in Elasticsearch.

Hari Sekhon

Big Data Contractor, United Kingdom

https://www.linkedin.com/in/harisekhon

### Build ###

Uses the standard SBT build process to automatically pull in all dependencies and the assembly plugin to build an uber jar for simpler Spark deployments. Run the following commands to download and build the jar:

```
git clone https://github.com/harisekhon/spark-apps
cd spark-apps
make
```
Requires SBT and Maven to be in the $PATH. Make will first download and build my personal Java utility library with Maven to be included as a dependency before using SBT to generate the Spark application jar.

After this finishes you can find the Spark application jar under target/scala-*/.

### Usage ###

The given data path may be a directory, a file glob or comma separated list and can decompress formats for which Hadoop has native support such as .gz / .bz2. Also supports directory recursion.

As per Spark standard option handling ```--class``` must come before the jar, switches after the jar belong to this application:

####  Spark Batch Apps ####

##### HDFS => Elasticsearch #####

You will likely need to throttle this job given it's easy for a Hadoop/Spark cluster to overwhelm an Elasticsearch cluster, even when using all the performance tuning tricks available and running on high spec nodes. In that case you will get task failures reporting ES as overloaded. I recommend using a capacity constrained queue on Yarn.

```
spark-submit ... --class com.linkedin.harisekhon.spark.TextToElasticsearch \
                 target/scala-*/spark-apps-assembly-*.jar \
                 --path 'hdfs://namenode/path/to/dir' \
                 --index <index> [--type <type>] \
                 --es-nodes <elasticsearch1:9200,elasticsearch2:9200,...>
```

Or to only take certain files you can use a glob:

```
spark-submit ... --class com.linkedin.harisekhon.spark.TextToElasticsearch \
                 target/scala-*/spark-apps-assembly-*.jar \
                 --path 'hdfs://namenode/path/to/*.log.bz2' \
                 --index <index> [--type <type>] \
                 --es-nodes <elasticsearch1:9200,elasticsearch2:9200,...>
```

##### Local Storage => Elasticsearch #####

```
spark-submit ... --class com.linkedin.harisekhon.spark.TextToElasticsearch \
                 target/scala-*/spark-apps-assembly-*.jar \
                 --path '/path/to/*.log.bz2' \
                 --index <index> [--type <type>] \
                 --es-nodes <elasticsearch1:9200,elasticsearch2:9200,...>
```

<!--

#### Real-time Spark Streaming Apps ####

Replace --master with your cluster, or specify minimum cores otherwise Spark doesn't process the stream

##### Kafka => Elasticsearch #####

```
spark-submit --master local[3] \
             --class com.linkedin.harisekhon.spark.KafkaToElasticsearch \
             target/scala-*/spark-apps-assembly-*.jar \
             --kafka <kafka1:9092,kafka2:9092,...> \
             --topic <topic> \
             --index <index>/<type> \
             --es-nodes <elasticsearch1:9200,elasticseach2:9200,...>
```

##### Kafka => HDFS #####

```
spark-submit --master local[3] \
             --class com.linkedin.harisekhon.spark.KafkaToTextFiles \
             target/scala-*/spark-apps-assembly-*.jar \
             --kafka <zkhost1:2181,zkhost2:2181,zkhost3:2181> \
             --topic <topic> \
             --path hdfs://namenode/etl/topic
```

##### Spark Streaming Network Socket Word / Line Counts #####
```
spark-submit --master local[3] \
             --class com.linkedin.harisekhon.spark.NetworkCounts \
             target/scala-*/spark-apps-assembly-*.jar \
             --host localhost \
             --port 9999 \
             --interval 5
```
-->

##### Environment variables #####

As with a lot of my programs, such as those in the [Advanced Nagios Plugins Collection](https://github.com/harisekhon/nagios-plugins) and [Toolbox](https://github.com/harisekhon/toolbox) this application supports use of the following environment variables for convenience instead of having to specify all the switches for ```--index/--type/--es-nodes``` (switches take precedence when present though):

* ```$ELASTICSEARCH_HOST```
* ```$ELASTICSEARCH_INDEX```
* ```$ELASTICSEARCH_TYPE```

### Advanced - Custom Parsers ###

To create your own parser extend the abstract class ```AbstractParser``` returning a serializable object containing only the fields you want to index to Elasticsearch.
<!--
Uses Scala's new Reflection API in 2.10 to dynamically load the parser to allow for supplying your own Parser class at runtime for custom extensible parsing without modifying this stable base code.

Package the parser class/object and the Elasticsearch document class in to a jar and then supply your class/object name and jar names as options on the ```spark-submit``` command line by specifying ```--jars my-parser.jar``` before spark-apps-assembly-*.jar and  ```--parser com.domain.MyParser``` after it.
-->

### Updating ###
```
make update
```
This will not only git pull but also fetch the correct version of the library submodule to match and then run a clean make to rebuild all dependencies for the library submodule followed by the Spark application.

### Testing ###

Continuous Integration is run with ScalaTest and a sample test run found under ```tests/run.sh``` which indexes some sample data and then retrieves it from Elasticsearch to verify that everything is working end-to-end.

### Contributions ###

Patches, improvements and even general feedback are welcome in the form of GitHub pull requests and issue tickets.

### See Also ###

* [PyTools](https://github.com/harisekhon/pytools) - programs for Hadoop, Spark, Pig, Elasticsearch, Solr, Linux CLI - contains the original Pig => Elasticsearch / Solr programs this Spark => Elasticsearch application was based off

* [Tools](https://github.com/harisekhon/tools) - dozens of programs for Hadoop, Hive, Solr, Ambari, Web, Linux CLI - contains Hive => Elasticsearch related to this Spark => Elasticsearch program

* [The Advanced Nagios Plugins Collection](https://github.com/harisekhon/nagios-plugins) - 220+ programs for Nagios monitoring your Hadoop & NoSQL clusters including Spark, Yarn, Elasticsearch etc. Covers every Hadoop vendor's management API and every major NoSQL technology (HBase, Cassandra, MongoDB, Elasticsearch, Solr, Riak, Redis etc.) as well as traditional Linux and infrastructure

* [My Java utility library](https://github.com/harisekhon/lib-java) - leveraged in this code as a submodule
