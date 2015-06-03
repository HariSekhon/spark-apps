package HariSekhon.Spark

import java.lang.Long
import java.util.HashMap

trait ParserTrait {
  def parse(path: String, offset: Long, line: String): HashMap[String, String]
  def returns(): List[Any]
}