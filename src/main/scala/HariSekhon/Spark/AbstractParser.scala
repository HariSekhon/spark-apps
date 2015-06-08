package HariSekhon.Spark

import java.lang.Long
import java.util.HashMap
import java.util.ArrayList

abstract class AbstractParser extends Serializable {
  def parse(path: String, offset: Long, line: String): HashMap[String, String]
  def returns(): ArrayList[AnyRef]
}