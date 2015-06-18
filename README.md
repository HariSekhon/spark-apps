Spark => Elasticsearch Indexer [![Build Status](https://travis-ci.org/harisekhon/spark-to-elasticsearch.svg?branch=master)](https://travis-ci.org/harisekhon/spark-to-elasticsearch)
================================

Generic Spark to Elasticsearch indexing application written in Scala to provide real-time Full-Text search on your Big Data.
<!--
Provides 2 ready-to-run Applications:

1. Spark Streaming from Kafka to Elasticsearch. This is [near] real-time indexing.

2. Batch indexing of Hadoop HDFS text/compressed files with file path and offset, or even local files depending on the Spark setup and specified URI prefix since the file access method is abstracted behind a Hadoop InputFormat.
-->
This is based off my Pig & Hive freebies for indexing structured and unstructured data in Hadoop to Elasticsearch & Solr/SolrCloud, see my adjacent [Toolbox repo](https://github.com/harisekhon/toolbox) for those programs.

Includes Kryo serialization optimization, and pluggable parsers, as well as option to count records for reporting and comparison. Serialization optimization can be disabled with a command line switch to use the slower but slightly more robust Java serialization.

Still on the todo list is adding varied date detection + parsing from which to create a time range query-able field in Elasticsearch.

Hari Sekhon

Big Data Contractor, United Kingdom

http://www.linkedin.com/in/harisekhon

### Build ###

Uses the standard SBT build process to automatically pull in all dependencies and the assembly plugin to build an uber jar for simpler Spark deployments. Run the following commands to download and build the jar:

```
git clone https://github.com/harisekhon/spark-to-elasticsearch
cd spark-to-elasticsearch
make clean make
```
Requires SBT and Maven to be in the $PATH. Make will first download and build my personal Java utility library with Maven to be included as a dependency before using SBT to generate the Spark application jar.

After this finishes you can find the Spark application jar under target/scala-*/.

### Usage ###

The given data path may be a directory, a file glob or comma separated list and can decompress formats for which Hadoop has native support such as .gz / .bz2. Also supports directory recursion.

The order of the arguments is important here:
<!--
##### Real-time Kafka Spark Streaming => Elasticsearch #####

```
spark-submit ... --class KafkaToElasticsearch \
                 target/scala-*/spark-to-elasticsearch-assembly-*.jar \
                 --kafka <kafka1:9092,kafka2:9092,...> \
                 --topic <topic> \
                 --index <index>/<type> \
                 --es-nodes <elasticsearch1:9200,elasticseach2:9200,...>
```

##### Batch HDFS / local storage => Elasticsearch #####
-->

```
spark-submit ... --class HariSekhon.Spark.TextToElasticsearch \
                 target/scala-*/spark-to-elasticsearch-assembly-*.jar \
                 --path '/path/to/*.log.bz2' \
                 --index <index> [--type <type>] \
                 --es-nodes <elasticsearch1:9200,elasticsearch2:9200,...>
```

You will likely need to throttle this job given it's easy for a Hadoop/Spark cluster to overwhelm an Elasticsearch cluster, even when using all the performance tuning tricks available and running on high spec nodes. In that case you will get task failures reporting ES as overloaded. I recommend using a capacity constrained queue on Yarn.

##### Environment variables #####

As with a lot of my programs, such as those in the [Advanced Nagios Plugins Collection](https://github.com/harisekhon/nagios-plugins) and [Toolbox](https://github.com/harisekhon/toolbox) this application supports use of the following environment variables for convenience instead of having to specify all the switches for ```--index/--type/--es-nodes``` (switches take precedence when present though):

* ```$ELASTICSEARCH_HOST```
* ```$ELASTICSEARCH_INDEX```
* ```$ELASTICSEARCH_TYPE```

### Advanced - Custom Parsers ###

To create your own parser extend the abstract class ```AbstractParser``` returning a serializable object containing only the fields you want to index to Elasticsearch.
<!--
Uses Scala's new Reflection API in 2.10 to dynamically load the parser to allow for supplying your own Parser class at runtime for custom extensible parsing without modifying this stable base code.

Package the parser class/object and the Elasticsearch document class in to a jar and then supply your class/object name and jar names as options on the ```spark-submit``` command line by specifying ```--jars my-parser.jar``` before spark-to-elasticsearch-assembly-*.jar and  ```--parser com.domain.MyParser``` after it.
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

* [Toolbox](https://github.com/harisekhon/toolbox) - 30+ programs for Hadoop, NoSQL, Solr, Elasticsearch, Pig, Hive, Web, Linux CLI - contains the original Pig & Hive programs this application was based off

* [The Advanced Nagios Plugins Collection](https://github.com/harisekhon/nagios-plugins) - 220+ programs for Nagios monitoring your Hadoop & NoSQL clusters including Spark, Yarn, Elasticsearch etc. Covers every Hadoop vendor's management API and every major NoSQL technology (HBase, Cassandra, MongoDB, Elasticsearch, Solr, Riak, Redis etc.) as well as traditional Linux and infrastructure

* [My Java utility library](https://github.com/harisekhon/lib-java) - leveraged in this code as a submodule
