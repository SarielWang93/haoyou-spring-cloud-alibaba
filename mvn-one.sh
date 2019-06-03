#!/usr/bin/env bash

xxl_url=${HOME}/Desktop/fsdownload/xxl

cd haoyou-spring-cloud-alibaba-$1
mvn  clean install -DskipTests
mv target/haoyou-spring-cloud-alibaba-$1-1.0.0-SNAPSHOT.jar ${xxl_url}/$1
cd ../