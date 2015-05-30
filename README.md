Spark => Elasticsearch [![Build Status](https://travis-ci.org/harisekhon/spark-to-elasticsearch.svg?branch=master)](https://travis-ci.org/harisekhon/spark-to-elasticsearch)
================================

Generic Spark to Elasticsearch indexing application written in Scala.

Provides full-text search with file path and offset for data stored in Hadoop or local data depending on the setup and URI prefix given. Still on the todo list is attempting date parsing from logs for creating a extra time range query-able field in Elasticsearch.

This is based off my Pig & Hive freebies for indexing structured and unstructured data in Hadoop to Elasticsearch & Solr/SolrCloud, see my adjacent toolbox repo https://github.com/harisekhon/toolbox for those programs.

Hari Sekhon

Big Data Contractor, United Kingdom

http://www.linkedin.com/in/harisekhon

### Build ###

Uses the standard SBT build process to automatically pull in all dependencies and the assembly plugin to build an uber jar for simpler Spark deployments. Run the following commands to download and build the jar:

```
git clone https://github.com/harisekhon/spark-to-elasticsearch
cd spark-to-elasticsearch
sbt clean assembly
```
<!--
make
```
This will download my java utility library from Github and then run ```sbt clean assembly```.
-->

After this finishes you can find the Spark application jar under target/scala-*/.

### Usage ###

The given data path may be a directory, a file glob or comma separated list and can decompress formats for which Hadoop natively supports such as .gz / .bz2, but does not support recursion at this time and the Spark job will error out if it detects subdirectories.

The order of the arguments is important here:
```
spark-submit ... --class TextToElasticsearch target/scala-*/spark-to-elasticsearch-assembly-*.jar '/path/to/*.log' <index>/<type> <Elasticsearch,node,list,comma,separated>
```

You will likely need to throttle this job given it's easy for a Hadoop/Spark cluster to overwhelm an Elasticsearch cluster, even when using all the performance tuning tricks available and running on high spec nodes. In that case you will get task failures reporting ES as overloaded. I recommend using a capacity constrained queue on Yarn.

### Contributions ###

Patches, improvements and even general feedback are welcome in the form of GitHub pull requests and issue tickets.

### See Also ###

My Toolbox repo adjacent to this one which contains the original Pig & Hive programs among other goodies related to Hadoop, NoSQL, Linux CLI tools etc:

https://github.com/harisekhon/toolbox

The Advanced Nagios Plugins Collection for monitoring your Hadoop & NoSQL clusters including Spark, Yarn, Elasticsearch etc:

https://github.com/harisekhon/nagios-plugins
