import HariSekhon.Spark._
import org.scalatest._

class ParserTest extends FunSuite {

  val parser = new Parser()
  
  test("parser strip_file_scheme should remove prefix hdfs://nameservice1:8020"){
    assert(parser.strip_file_scheme("hdfs://nameservice1/path/to/dir").equals("/path/to/dir"))
  }
  test("parser strip_file_scheme should remove prefix file://"){
    assert(parser.strip_file_scheme("file:///path/to/dir").equals("/path/to/dir"))
  }
  
}
