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

// Parser for TextToElasticsearch (generic Spark port of my Pig => Elasticsearch unstructured 
// text / log files Hadoop job originally from https://github.com/harisekhon/toolbox)

package HariSekhon.Spark

// Parser class to be called in TextToElasticsearch
@SerialVersionUID(100L)
object Parser extends Serializable {
  // TODO: add DateLineParser logic here
  def parse(path: String, offset: Long, line: String): ElasticsearchDocument = {
    val path2 = //if (path.isEmpty()) {
    //  ""
    //} else {
      path.replaceFirst("^file:", "").replaceFirst("^hdfs:\\/\\/[\\w.-]+(?:\\d+)?", "")
    //}
    val date: String = null
    if (date == null) {
      new FileLineDocument(path2, offset, line)
    } else {
      new FileDateLineDocument(path2, offset, date, line)
    }
  }
}

class Parser {}