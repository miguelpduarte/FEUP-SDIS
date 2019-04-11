#!/usr/bin/env sh

COMPILE_DIR=out/production/proj1

mkdir -p $COMPILE_DIR
# Build Main class of "RMI Side"
javac -cp src src/base/PeerMain.java -d $COMPILE_DIR