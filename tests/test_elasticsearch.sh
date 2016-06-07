#!/usr/bin/env bash
#  vim:ts=4:sts=4:sw=4:et
#
#  Author: Hari Sekhon
#  Date: 2015-05-25 01:38:24 +0100 (Mon, 25 May 2015)
#
#  https://github.com/harisekhon/spark-apps
#
#  License: see accompanying Hari Sekhon LICENSE file
#
#  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
#
#  https://www.linkedin.com/in/harisekhon
#

set -eu
[ -n "${DEBUG:-}" ] && set -x
srcdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd "$srcdir/..";

. ./tests/utils.sh

echo "
# ============================================================================ #
#                   S p a r k   =>   E l a s t i c s e a r c h
# ============================================================================ #
"

# container linking now
export ELASTICSEARCH_HOST="elasticsearch"
export HOST="${DOCKER_HOST:-${HOST:-localhost}}"
HOST="${HOST##*/}"
HOST="${HOST%%:*}"
export HOST
export ELASTICSEARCH_PORT="${ELASTICSEARCH_PORT:-9200}"
export ELASTICSEARCH_INDEX="${ELASTICSEARCH_INDEX:-testIndex}"
export ELASTICSEARCH_TYPE="${ELASTICSEARCH_TYPE:-testType}"

export DOCKER_IMAGE_SPARK="harisekhon/spark"
export DOCKER_IMAGE_ELASTICSEARCH="elasticsearch"
export DOCKER_CONTAINER_SPARK="spark-apps-spark-test"
export DOCKER_CONTAINER_ELASTICSEARCH="spark-apps-elasticsearch-test"

export SPARK_VERSIONS="${1:-1.3 1.4 1.5 1.6}"
export SPARK_VERSIONS="${1:-1.6}"

# 5.0 tag doesn't work yet
export ELASTICSEARCH_VERSIONS="${2:-1.4 1.5 1.6 1.7 2.0 2.2 2.3}"
export ELASTICSEARCH_VERSIONS="${2:-2.3}"
if is_travis; then
    export ELASTICSEARCH_VERSIONS="${2:-1.7 2.3}"
fi

export MNTDIR="/code"

SAMPLE_DATA_BASENAME="tests/elasticsearch-data"
SAMPLE_DATA="$srcdir/../$SAMPLE_DATA_BASENAME"

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

startupwait=5

test_spark_elasticsearch(){
    local spark_version="$1"
    local elasticsearch_version="$2"
    travis_sample || continue
    echo "Setting up Elasticsearch $elasticsearch_version test container"
    local DOCKER_OPTS=""
    launch_container "$DOCKER_IMAGE_ELASTICSEARCH:$elasticsearch_version" "$DOCKER_CONTAINER_ELASTICSEARCH" 9200 2> /dev/null

    echo "Setting up Spark $spark_version test container"
    local DOCKER_OPTS="--link $DOCKER_CONTAINER_ELASTICSEARCH:$ELASTICSEARCH_HOST -v $srcdir/..:$MNTDIR"
    docker run -ti --rm $DOCKER_OPTS --name "$DOCKER_CONTAINER_SPARK" \
                   "$DOCKER_IMAGE_SPARK:$spark_version" \
                              spark-submit --master local[2] \
                              --class com.linkedin.harisekhon.spark.TextToElasticsearch \
                              "$MNTDIR"/target/scala-*/spark-apps-assembly-*.jar \
                              --path "file://$MNTDIR/$SAMPLE_DATA_BASENAME" \
                              --index "$ELASTICSEARCH_INDEX" --type "$ELASTICSEARCH_TYPE" \
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
    echo "SUCCESS! (spark $spark_version, elasticsearch $elasticsearch_version)"
    hr
    delete_container "$DOCKER_CONTAINER_ELASTICSEARCH"
}

for spark_version in $SPARK_VERSIONS; do
    for elasticsearch_version in $ELASTICSEARCH_VERSIONS; do
        test_spark_elasticsearch $spark_version $elasticsearch_version
    done
done
