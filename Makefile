#
#  Author: Hari Sekhon
#  Date: 2015-05-30 13:11:10 +0100 (Sat, 30 May 2015)
#
#  vim:ts=4:sts=4:sw=4:noet
#
#  https://github.com/harisekhon/spark-apps
#
#  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
#
#  http://www.linkedin.com/in/harisekhon
#

.PHONY: make
make:
	make lib
	sbt clean assembly

.PHONY: lib
lib:
	git submodule update --init
	cd lib && mvn clean package
	sbt eclipse || :

.PHONY: clean
clean:
	cd lib && mvn clean
	sbt clean

.PHONY: update
update:
	git pull
	git submodule update --init
	make

.PHONY: test
test:
	sbt test
	tests/all.sh
	bash-tools/all.sh

# useful for quicker compile testing but not deploying to Spark
.PHONY: p
p:
	make package
.PHONY: package
package:
	git submodule update --init --remote --recursive
	cd lib && mvn clean package
	sbt package 
