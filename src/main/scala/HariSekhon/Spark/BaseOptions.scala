package HariSekhon.Spark

import HariSekhon.Utils._

import org.apache.commons.cli.OptionBuilder

object BaseOptions {

    // must use older commons-cli API to not conflict with Spark's embedded commons-cli
    // urgh this would work in Java but not in Scala since due to static class 
    //options.addOption(OptionBuilder.withLongOpt("index").withArgName("index").withDescription("Elasticsearch index and type in the format index/type").create("i"))
    //options.addOption(OptionBuilder.withLongOpt("path").withArgName("dir").withDescription("HDFS / File / Directory path to recurse and index to Elasticsearch").create("p"))
    //options.addOption(OptionBuilder.withLongOpt("es-nodes").withArgName("nodes").withDescription("Elasticsearch node list, comma separated (eg. node1:9200,node2:9200,...). Required, should list all nodes for balancing load since the Elastic spark driver using a direct connection and doesn't yet use a native cluster join as it does in the regular Java API").create("E"))
    //options.addOption(OptionBuilder.withLongOpt("count").withDescription("Generate a total count of the lines to index, for both reporting during the index job as well as writing it to '/tmp/<index_name>.count' to later comparison. This causes an extra job and shuffle and should be avoided for high volume or when you need to get things done quicker").create("c"))
    //
    // XXX: Apache Commons CLI is awful - consider replacing later
    //
    // this way doesn't print the description: 
    //OptionBuilder.withLongOpt("count")
    //OptionBuilder.withDescription("Generate a total count of the lines to index before sending to Elasticsearch, for both reporting how much there is to do before starting indexing as well as writing it to '/tmp/<index_name>.count' at the end for later comparison. This causes an extra Spark job and network shuffle and will slow you down")
    //options.addOption(OptionBuilder.create("c"))
    options.addOption("c", "count", false, "Generate a total count of the lines to index before sending to Elasticsearch, for both reporting how much there is to do before starting indexing as well as writing it to '/tmp/<index_name>.count' at the end for later comparison. This causes an extra Spark job and network shuffle and will slow you down")
    //
    options.addOption("s", "safe-serialization", false, "Use safer Java serialization (slower). Only use this if you are getting Kryo serialization errors")
    //
    OptionBuilder.withLongOpt("index")
    OptionBuilder.withArgName("index")
    OptionBuilder.withDescription("Elasticsearch index to send data to")
    OptionBuilder.hasArg()
    OptionBuilder.isRequired()
    options.addOption(OptionBuilder.create("i"))
    //
    OptionBuilder.withLongOpt("type")
    OptionBuilder.withArgName("type")
    OptionBuilder.withDescription("Elasticsearch type (defaults to the same name as --index)")
    OptionBuilder.hasArg()
    options.addOption(OptionBuilder.create("y"))
    //
    OptionBuilder.withLongOpt("es-nodes")
    OptionBuilder.withArgName("nodes")
    OptionBuilder.withDescription("Elasticsearch node list, comma separated with optional port numbers after each host (eg. node1:9200,node2:9200,..., defaults to \"localhost:9200\"). Required, should list all nodes for balancing load since the Elastic spark driver uses a direct connection and doesn't yet have native cluster join support as it does in the regular Elasticsearch Java API")
    OptionBuilder.hasArg()
    OptionBuilder.isRequired()
    options.addOption(OptionBuilder.create("E"))
    
    //
    /*
    OptionBuilder.withLongOpt("parser")
    OptionBuilder.withArgName("com.domain.MyParser")
    OptionBuilder.withDescription("Custom parser class to use. Need to use --jars to supply the jar to spark if using this")
    OptionBuilder.hasArg()
    options.addOption(OptionBuilder.create())
    */
    
    //
    //OptionBuilder.withLongOpt("esDocClass")
    //OptionBuilder.withArgName("com.domain.ESDocumentClass")
    //OptionBuilder.withDescription("Custom Elasticsearch document class for custom --parser to return, should be supplied using --jars as with --parser. Requires --parser")
    //OptionBuilder.hasArg()
    //options.addOption(OptionBuilder.create())
    
}