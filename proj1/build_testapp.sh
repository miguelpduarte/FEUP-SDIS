#!/usr/bin/env sh

COMPILE_DIR=out/production/proj1

mkdir -p $COMPILE_DIR
# Build Main class of "TestApp side"
javac -cp src src/base/TestApp.java -d $COMPILE_DIR
