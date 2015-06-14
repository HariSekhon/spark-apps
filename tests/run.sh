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

cd "$srcdir"

SAMPLE_DATA="$srcdir/elasticsearch-data"

echo "creating sample data"
mkdir -p "$SAMPLE_DATA"
cat > "$SAMPLE_DATA/file1" <<EOF
one
two
three
EOF
mkdir -p "$SAMPLE_DATA/dir2"
cat > "$SAMPLE_DATA/dir2/file2" <<EOF
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

echo "deleting existing index '$INDEX' if it exists"
curl -XDELETE "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/$INDEX"
echo
echo

if ! [ -e "$TAR" ]; then
    echo "fetching Spark tarball '$TAR'"
    wget "http://www.us.apache.org/dist/spark/spark-$SPARK_VERSION/$TAR"
    echo
fi

if ! [ -d "$SPARK" ]; then
    echo "unpacking Spark"
    tar zxf "$TAR"
    echo
fi

cd ..
echo "running Spark job to index sample data files to Elasticsearch"
"$srcdir/$SPARK/bin/spark-submit" --master local[2] \
                          --class HariSekhon.Spark.TextToElasticsearch \
                          target/scala-*/spark-to-elasticsearch-assembly-*.jar \
                          --path "$SAMPLE_DATA" \
                          --index "$INDEX" --type 'testtype' \
                          --es-nodes "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT"
echo
echo

echo "Checking 6 documents were indexed to Elasticsearch"
curl -s "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/$INDEX/_search?pretty" |
    tee /dev/stderr |
        grep -q '"total" : 6,' &&
            echo -e "\nFound 6 Elasticsearch documents indexed as expected" ||
            { echo -e "\nDidn't find 6 Elasticsearch documents as expected"; exit 1; }
echo

echo "Checking we can find document 6 indexed as expected"
curl -s "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/$INDEX/_search?pretty" |
    grep -q '"_source":{"path":".*/elasticsearch-data/dir2/file2","offset":10,"line":"six"}' &&
        echo "Found document 'six' with path and offset" ||
        { echo "Failed to find document 'six' contents with path and offset"; exit 1; }
echo
echo 'SUCCESS!'
