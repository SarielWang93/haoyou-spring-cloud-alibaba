#!/usr/bin/env bash
xxl_url=${HOME}/Desktop/fsdownload/xxl
cd haoyou-spring-cloud-alibaba-manager
mvn  clean install -DskipTests
mv target/haoyou-spring-cloud-alibaba-manager-1.0.0-SNAPSHOT.jar ${xxl_url}/manager
cd ../