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

# EUID /  UID not exported in Make
# USER not populated in Docker
ifeq '$(shell id -u)' '0'
	SUDO =
else
	SUDO = sudo
endif

.PHONY: make
make:
	if [ -x /usr/bin/apt-get ]; then make apt-packages; fi
	if [ -x /usr/bin/yum ];     then make yum-packages; fi

	git submodule init
	git submodule update --remote --recursive
	make lib
	sbt clean assembly

.PHONY: lib
lib:
	git submodule update --init --remote --recursive
	cd lib && mvn clean package
	sbt eclipse || :

.PHONY: clean
clean:
	cd lib && mvn clean
	sbt clean

.PHONY: apt-packages
apt-packages:
	$(SUDO) apt-get update
	# needed to fetch library submodules
	$(SUDO) apt-get install -y git
	# needed to fetch Spark for tests
	$(SUDO) apt-get install -y wget
	$(SUDO) apt-get install -y tar

.PHONY: yum-packages
yum-packages:
	# needed to fetch the library submodules
	rpm -q git || $(SUDO) yum install -y git
	# needed to fetch Spark for tests
	rpm -q wget || $(SUDO) yum install -y wget
	rpm -q tar  || $(SUDO) yum install -y tar

.PHONY: update
update:
	git pull
	git submodule update --init --remote --recursive
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
