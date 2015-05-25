//
//  Author: Hari Sekhon
//  Date: 2015-05-23 09:28:48 +0100 (Sat, 23 May 2015)
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

name := "spark-to-elasticsearch"

version := "0.1"

scalaVersion := "2.10.4"

//resolvers += "clojars" at "https://clojars.org/repo"
//resolvers += "conjars" at "http://conjars.org/repo"

libraryDependencies ++= Seq (
    // %% appends scala version to spark-core
    "org.apache.spark" %% "spark-core" % "1.3.1" % "provided",
    "org.apache.hadoop" % "hadoop-client" % "2.6.0" % "provided",
    //"org.elasticsearch" % "elasticsearch" % "1.4.1"
    // this pulled in loads of deps for Clojure and others which wouldn't resolve, using elasticsearch-spark instead
    //"org.elasticsearch" % "elasticsearch-hadoop" % "2.0.2"
    "org.elasticsearch" %% "elasticsearch-spark" % "2.1.0.Beta4"
)
