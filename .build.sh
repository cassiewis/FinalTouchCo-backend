#!/usr/bin/env bash
# Install OpenJDK 17 (or your required version)
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Now build your project
./gradlew build