//
//  Author: Hari Sekhon
//  Date: 2015-05-25 23:27:15 +0100 (Mon, 25 May 2015)
//
//  vim:ts=4:sts=4:sw=4:et
//
//  https://github.com/harisekhon/spark-to-elasticsearch
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
//
//  http://www.linkedin.com/in/harisekhon
//

import AssemblyKeys._

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("META-INF", "maven","org.slf4j","slf4j-api", ps) if ps.startsWith("pom") => MergeStrategy.discard
    case x => old(x)
  }
}
