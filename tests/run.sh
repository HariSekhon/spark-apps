#!/usr/bin/env bash
#  vim:ts=4:sts=4:sw=4:et
#
#  Author: Hari Sekhon
#  Date: 2015-06-14 00:38:47 +0100 (Sun, 14 Jun 2015)
#
#  https://github.com/harisekhon
#
#  License: see accompanying Hari Sekhon LICENSE file
#
#  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
#
#  http://www.linkedin.com/in/harisekhon
#

set -euo pipefail
srcdir="$(dirname $0)"

cd "$srcdir/.."

mkdir -p elasticsearch-data
cat > elasticsearch-data/file1 <<EOF
one
two
three
EOF
mkdir -p elasticsearch-data/dir2
cat > elasticsearch-data/dir2/file2 <<EOF
four
five
six
EOF

SPARK_VERSION=1.4.0
BIN="bin-hadoop2.6"

[ -e "spark-$SPARK_VERSION-$BIN.tgz" ] ||
wget "http://www.us.apache.org/dist/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-$BIN.tgz"

[ -d "spark-$SPARK_VERSION-$BIN" ] ||
tar zxvf "spark-$SPARK_VERSION-$BIN.tgz"

"spark-$SPARK_VERSION-$BIN/bin/spark-submit" --master local[2] \
                                            --class HariSekhon.Spark.TextToElasticsearch \
                                            target/scala-*/spark-to-elasticsearch-assembly-*.jar \
                                            --path 'elasticsearch-data' \
                                            --index 'testindex' --type 'testtype' \
                                            --es-nodes localhost:9200

curl 'localhost:9200/testindex/_search?q=path:file2&pretty' | tee /dev/stderr | grep -q six
