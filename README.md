Spark => Elasticsearch
================================

*Incomplete - don't fork or use it yet please.*

Generic Spark to Elasticsearch indexing application written in Scala.

This is based off my Pig & Hive freebies for indexing structured and unstructured data in Hadoop to Elasticsearch & Solr/SolrCloud, see my adjacent toolbox repo https://github.com/harisekhon/toolbox for those programs.

Hari Sekhon

Big Data Contractor, United Kingdom

http://www.linkedin.com/in/harisekhon

### Build ###

Use the standard SBT build process to automatically pull in all dependencies and build the job jar:

```
git clone https://github.com/harisekhon/spark-to-elasticsearch
cd spark-to-elasticsearch
sbt clean assembly
```
Now find the jar under target/scala-*/.

### Usage ###

The data path is passed to Spark context's textFile() method which can take a directory or glob of files including compressed files such as bz2.

The order of the arguments is important here:
```
spark-submit ... --class TextToElasticsearch target/scala-*/spark-to-elasticsearch-assembly-*.jar '/path/to/*.log' <Elasticsearch_Cluster_name> <index>/<type> <Elasticsearch,node,list,comma,separated>
```

You will likely need to throttle this job given it's easy for a Hadoop/Spark cluster to overwhelm an Elasticsearch cluster, even when using all the performance tuning tricks available and running on high spec nodes. In that case you will get task failures reporting ES as overloaded. I recommend using a capacity constrained queue on Yarn, see your cluster admin for help with this.

### Contributions ###

Patches, improvements and even general feedback are welcome in the form of GitHub pull requests and issue tickets.

### See Also ###

My Toolbox repo adjacent to this one which contains the original Pig & Hive programs among other goodies related to Hadoop, NoSQL, Linux CLI tools etc:

https://github.com/harisekhon/toolbox

The Advanced Nagios Plugins Collection for monitoring your Hadoop & NoSQL clusters including Spark, Yarn, Elasticsearch etc:

https://github.com/harisekhon/nagios-plugins
