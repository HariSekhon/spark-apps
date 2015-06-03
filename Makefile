#
#  Author: Hari Sekhon
#  Date: 2015-05-30 13:11:10 +0100 (Sat, 30 May 2015)
#
#  vim:ts=4:sts=4:sw=4:noet
#
#  https://github.com/harisekhon/spark-to-elasticsearch
#
#  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
#
#  http://www.linkedin.com/in/harisekhon
#

make:
	git submodule update --init
	cd lib && mvn package
	sbt assembly

clean:
	cd lib && mvn clean
	echo sbt clean

update:
	git pull
	git submodule update --init
	make clean make
