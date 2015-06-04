Spark => Elasticsearch [![Build Status](https://travis-ci.org/harisekhon/spark-to-elasticsearch.svg?branch=master)](https://travis-ci.org/harisekhon/spark-to-elasticsearch)
================================

Generic Spark to Elasticsearch indexing application written in Scala.

Provides 2 Applications:

1. Spark Streaming from Kafka to Elasticsearch. This is [near] real-time.

2. Full-text search of Hadoop HDFS text/compressed files with file path and offset, or local files depending on the Spark setup and specified URI prefix. Still on the todo list is attempting date parsing from logs for creating a extra time range query-able field in Elasticsearch.

This is based off my Pig & Hive freebies for indexing structured and unstructured data in Hadoop to Elasticsearch & Solr/SolrCloud, see my adjacent toolbox repo https://github.com/harisekhon/toolbox for those programs.

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
Requires SBT and Maven to be in the $PATH. Make will first download and build my Java utility library with Maven to be included as a dependency before using SBT to generate the Spark application jar.

After this finishes you can find the Spark application jar under target/scala-*/.

### Usage ###

The given data path may be a directory, a file glob or comma separated list and can decompress formats for which Hadoop has native support such as .gz / .bz2. Also supports directory recursion.

The order of the arguments is important here:

##### Real-time Kafka Spark Streaming to Elasticsearch #####

```
spark-submit ... --class KafkaToElasticsearch \
                 target/scala-*/spark-to-elasticsearch-assembly-*.jar \
                 --kafka <kafka1:9092,kafka2:9092,...> \
                 --topic <topic> \
                 --index <index>/<type> \
                 --es-nodes <elasticsearch1:9200,elasticseach2:9200,...>
```

##### Batch HDFS / local storage to Elasticsearch #####

You will likely need to throttle this job given it's easy for a Hadoop/Spark cluster to overwhelm an Elasticsearch cluster, even when using all the performance tuning tricks available and running on high spec nodes. In that case you will get task failures reporting ES as overloaded. I recommend using a capacity constrained queue on Yarn.

```
spark-submit ... --class HariSekhon.Spark.TextToElasticsearch \
                 target/scala-*/spark-to-elasticsearch-assembly-*.jar \
                 --path '/path/to/*.log.bz2' \
                 --index <index> [--type <type>] \
                 --es-nodes <elasticsearch1:9200,elasticsearch2:9200,...>
```

You will likely need to throttle this job given it's easy for a Hadoop/Spark cluster to overwhelm an Elasticsearch cluster, even when using all the performance tuning tricks available and running on high spec nodes. In that case you will get task failures reporting ES as overloaded. I recommend using a capacity constrained queue on Yarn.

### Updating ###
```
make update
```
This will not only git pull but also fetch the correct version of the library submodule to match and then run a clean make to rebuild all dependencies for the library submodule followed by the Spark application.

### Contributions ###

Patches, improvements and even general feedback are welcome in the form of GitHub pull requests and issue tickets.

### See Also ###

My Toolbox repo adjacent to this one which contains the original Pig & Hive programs among other goodies related to Hadoop, NoSQL, Solr, Elasticsearch, Linux CLI tools etc:

https://github.com/harisekhon/toolbox

The Advanced Nagios Plugins Collection for monitoring your Hadoop & NoSQL clusters including Spark, Yarn, Elasticsearch etc:

https://github.com/harisekhon/nagios-plugins
