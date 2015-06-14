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

echo "creating sample data"
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
echo

SPARK_VERSION=1.4.0
BIN="bin-hadoop2.6"
SPARK="spark-$SPARK_VERSION-$BIN"
TAR="$SPARK.tgz"

ELASTICSEARCH_HOST="${ELASTICSEARCH_HOST:-localhost}"
ELASTICSEARCH_PORT="${ELASTICSEARCH_PORT:-9200}"
INDEX="spark-to-elasticsearch-test"

curl -XDELETE "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/$INDEX"
echo

if ! [ -e "$TAR" ]; then
    echo "fetching Spark tarball '$TAR'"
    wget "http://www.us.apache.org/dist/spark/spark-$SPARK_VERSION/$TAR"
fi

if ! [ -d "$SPARK" ]; then
    echo "unpacking Spark"
    tar zxf "$TAR"
fi

"$SPARK/bin/spark-submit" --master local[2] \
                          --class HariSekhon.Spark.TextToElasticsearch \
                          target/scala-*/spark-to-elasticsearch-assembly-*.jar \
                          --path 'elasticsearch-data' \
                          --index "$INDEX" --type 'testtype' \
                          --es-nodes "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT"

curl -s "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/$INDEX/_search?pretty" |
    tee /dev/stderr |
        grep -q '"total" : 6,' &&
            echo -e "\n\nFound 6 Elasticsearch documents indexed as expected" ||
            { echo -e "\n\nDidn't find 6 Elasticsearch documents as expected"; exit 1; }
curl -s "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/$INDEX/_search?pretty" |
    grep -q '"_source":{"path":"file:.*/elasticsearch-data/dir2/file2","line":"six","offset":"10"}' &&
        echo -e "\nFound document 'six'" ||
        { echo -e "\nFailed to find document 'six'"; exit 1; }
