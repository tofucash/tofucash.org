# Tofucoin V1
# Author eom-moe
# Makefile
SOURCE=./V1/**/*.java
CLASS=./V1/**/*.class
CLASS_PATH=../lib/mysql-connector-java-5.1.42-bin.jar:../lib/jsonic-1.1.3.jar

.PHONY: all
all: clean comp 

.PHONY: comp
comp:
	javac -encoding UTF-8 -cp ${CLASS_PATH} ${SOURCE}

		
.PHONY: clean
clean:
	rm -rf ${CLASS}


