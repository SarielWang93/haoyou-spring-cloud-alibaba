#!/usr/bin/env bash

xxl_url=${HOME}/Desktop/fsdownload/xxl

cd haoyou-spring-cloud-alibaba-$1
x='';
if [ "$1" == 'redis' ];then
    x='myredis'
 else
    x="$1"
fi
mvn  clean install -DskipTests
mv target/haoyou-spring-cloud-alibaba-$1-1.0.0-SNAPSHOT.jar ${xxl_url}/${x}
cd ../