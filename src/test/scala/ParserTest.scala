
import org.scalatest._

class ParserTest extends FunSuite {

  // Most of the unit tests are in the underlying library
  //
  // This is mostly a client code application so there is limited unit testing that can be done
  // 
  // see tests/run.sh for functional tests
  
//  val parser = new Parser()
  
  /* 
   * Not using this any more, now using strip_scheme_host() from use my standard utils
   *
  test("parser strip_file_scheme should remove prefix hdfs://nameservice1:8020"){
    assert(parser.strip_file_scheme("hdfs://nameservice1:8020/path/to/dir").equals("/path/to/dir"))
  }
  
  test("parser strip_file_scheme should remove prefix hdfs://namenode/ => /"){
	  assert(parser.strip_file_scheme("hdfs://namenode/path/to/dir").equals("/path/to/dir"))
  }
  
  test("parser strip_file_scheme should remove prefix hdfs:/// => /"){
	  assert(parser.strip_file_scheme("hdfs:///path/to/dir").equals("/path/to/dir"))
  }
  
  test("parser strip_file_scheme should remove prefix file:/// => /"){
    assert(parser.strip_file_scheme("file:///path/to/dir").equals("/path/to/dir"))
  }
  
  test("parser strip_file_scheme should remove prefix file:/ => /"){
    assert(parser.strip_file_scheme("file:/path/to/dir").equals("/path/to/dir"))
  }
  */
  
}
