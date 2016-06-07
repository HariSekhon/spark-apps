//
//  Author: Hari Sekhon
//  Date: 2015-05-25 19:39:56 +0100 (Mon, 25 May 2015)
//
//  vim:ts=2:sts=2:sw=2:et
//
//  https://github.com/harisekhon/spark-apps
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
//
//  http://www.linkedin.com/in/harisekhon
//

package com.linkedin.harisekhon.spark

import java.lang.Long
import java.util.HashMap
import java.util.ArrayList

abstract class AbstractParser extends Serializable {
  def parse(path: String, offset: Long, line: String): AnyRef // HashMap[String, String]
  def returns(): AnyRef
}
