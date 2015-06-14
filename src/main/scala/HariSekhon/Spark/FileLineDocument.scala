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

package HariSekhon.Spark

// Elasticsearch document
//@SerialVersionUID(101L) // only serializes in ESHadoop if I make it a case class again
case class FileLineDocument(path: String, line: String)
  extends ElasticsearchDocument //with Serializable 
  {

}