#!/usr/bin/env bash
#  vim:ts=4:sts=4:sw=4:et
#
#  Author: Hari Sekhon
#  Date: 2015-06-14 00:38:47 +0100 (Sun, 14 Jun 2015)
#
#  https://github.com/harisekhon/spark-apps
#
#  License: see accompanying Hari Sekhon LICENSE file
#
#  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
#
#  https://www.linkedin.com/in/harisekhon
#

set -euo pipefail
srcdir="$(cd $(dirname $0) && pwd)"

cd "$srcdir"

SAMPLE_DATA="$srcdir/elasticsearch-data"

echo "creating sample data at '$SAMPLE_DATA'"
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

#SPARK_VERSION=1.4.1
SPARK_VERSION=1.6.1
BIN="bin-hadoop2.6"
SPARK="spark-$SPARK_VERSION-$BIN"
TAR="$SPARK.tgz"

ELASTICSEARCH_HOST="${ELASTICSEARCH_HOST:-localhost}"
ELASTICSEARCH_PORT="${ELASTICSEARCH_PORT:-9200}"
# This is intentionally different to $ELASTICSEARCH_INDEX and $ELASTICSEARCH_TYPE to ensure --index/--type are taking precedence
INDEX="spark-to-elasticsearch-index"
TYPE="spark-to-elasticsearch-type"

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
                          --class com.linkedin.harisekhon.spark.TextToElasticsearch \
                          target/scala-*/spark-apps-assembly-*.jar \
                          --path "file://$SAMPLE_DATA" \
                          --index "$INDEX" --type "$TYPE" \
                          --es-nodes "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT"
echo
echo

results="$(curl -s "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/$INDEX/_search?pretty")"
echo "$results"
echo

die(){ echo -e "$@"; exit 1; }

# This would be better with jq but doubt it's available
echo "Checking 6 documents were indexed to Elasticsearch"
grep -q '"total" : 6,' <<< "$results" &&
    echo "Found 6 Elasticsearch documents indexed as expected" ||
        die "Didn't find 6 Elasticsearch documents as expected"
echo

echo "Checking we indexed to the right index and type"
grep -q '"_index" : "spark-to-elasticsearch-index",' <<< "$results" &&
    echo "indexed to correct index" ||
        die "indexed to the wrong index!"
grep -q '"_type" : "spark-to-elasticsearch-type",' <<< "$results" &&
    echo "indexed to correct type" ||
        die "indexed to the wrong type!"

echo "Checking we can find document number 6 indexed as expected"
grep -q '"_source":{"path":"[^:]\+/elasticsearch-data/dir2/file2","offset":10,"line":"six"}' <<< "$results" &&
    echo "Found document 'six' with path and offset" ||
        die"Failed to find document 'six' contents with path and offset"
echo
echo 'SUCCESS!'
