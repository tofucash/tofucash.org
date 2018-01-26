#! /bin/bash

java -Dfile.encoding=UTF-8 -cp .:../lib/jsonic-1.1.3.jar:../lib/mysql-connector-java-5.1.42-bin.jar:backend V1/Experiment/PseudoClient $1
