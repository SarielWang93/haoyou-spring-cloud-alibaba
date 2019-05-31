#!/usr/bin/env bash

xxl_url=${HOME}/Desktop/fsdownload/xxl

cd haoyou-spring-cloud-alibaba-dependencies
mvn  clean install -DskipTests
cd ../

cd haoyou-spring-cloud-alibaba-commons
mvn  clean install -DskipTests
cd ../

cd haoyou-spring-cloud-alibaba-commons-domain
mvn  clean install -DskipTests
cd ../

cd haoyou-spring-cloud-alibaba-commons-mapper
mvn  clean install -DskipTests
cd ../

cd haoyou-spring-cloud-alibaba-dubbo-core
mvn  clean install -DskipTests
cd ../

cd haoyou-spring-cloud-alibaba-commons-service
mvn  clean install -DskipTests
cd ../

cd haoyou-spring-cloud-alibaba-cultivate
mv target/haoyou-spring-cloud-alibaba-cultivate-1.0.0-SNAPSHOT.jar ${xxl_url}/cultivate
mvn  clean install -DskipTests
cd ../

cd haoyou-spring-cloud-alibaba-fighting
mv target/haoyou-spring-cloud-alibaba-fighting-1.0.0-SNAPSHOT.jar ${xxl_url}/fighting
mvn  clean install -DskipTests
cd ../

cd haoyou-spring-cloud-alibaba-login
mv target/haoyou-spring-cloud-alibaba-login-1.0.0-SNAPSHOT.jar ${xxl_url}/login
mvn  clean install -DskipTests
cd ../

cd haoyou-spring-cloud-alibaba-manager
mv target/haoyou-spring-cloud-alibaba-manager-1.0.0-SNAPSHOT.jar ${xxl_url}/manager
mvn  clean install -DskipTests
cd ../

cd haoyou-spring-cloud-alibaba-match
mv target/haoyou-spring-cloud-alibaba-match-1.0.0-SNAPSHOT.jar ${xxl_url}/match
mvn  clean install -DskipTests
cd ../

cd haoyou-spring-cloud-alibaba-redis
mvn  clean install -DskipTests
mv target/haoyou-spring-cloud-alibaba-redis-1.0.0-SNAPSHOT.jar ${xxl_url}/redis
cd ../

cd haoyou-spring-cloud-alibaba-sofabolt
mv target/haoyou-spring-cloud-alibaba-sofabolt-1.0.0-SNAPSHOT.jar ${xxl_url}/sofabolt
mvn  clean install -DskipTests
cd ../


