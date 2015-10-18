//
//  Author: Hari Sekhon
//  Date: 2015-05-23 09:28:48 +0100 (Sat, 23 May 2015)
//
//  vim:ts=4:sts=4:sw=4:et
//
//  https://github.com/harisekhon/spark-apps
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
//
//  http://www.linkedin.com/in/harisekhon
//

name := "spark-apps"

version := "0.5.7"

scalaVersion := "2.10.4"

//resolvers += "clojars" at "https://clojars.org/repo"
//resolvers += "conjars" at "http://conjars.org/repo"

unmanagedBase := baseDirectory.value / "lib/target"

libraryDependencies ++= Seq (
    // %% appends scala version to spark-core
    "org.apache.spark" %% "spark-core" % "1.3.1" % "provided",
    "org.apache.spark" %% "spark-streaming" % "1.3.1" % "provided",
    "org.apache.hadoop" % "hadoop-client" % "2.6.0" % "provided",
    "org.apache.spark" %% "spark-streaming-kafka" % "1.3.1",
    //"org.elasticsearch" % "elasticsearch" % "1.4.1",
    // this pulled in loads of deps for Clojure and others which wouldn't resolve, using elasticsearch-spark instead
    //"org.elasticsearch" % "elasticsearch-hadoop" % "2.0.2",
    "org.elasticsearch" %% "elasticsearch-spark" % "2.1.0.Beta4",
    // Spark has it's own older version of commons-cli and using the newer commons-cli 1.3 non-static API methods causes:
    // Exception in thread "main" java.lang.NoSuchMethodError: org.apache.commons.cli.Option.builder(Ljava/lang/String;)Lorg/apache/commons/cli/Option$Builder;
    //"commons-cli" % "commons-cli" % "1.3"
    "commons-cli" % "commons-cli" % "1.2",
    //"org.scalatest" % "scalatest_2.10" % "2.2.4" % "test",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "net.sf.jopt-simple" % "jopt-simple" % "4.9"
)
