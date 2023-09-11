#!/bin/bash

read -p "Enter port number: " user_port

if [ -z "$JAVA_HOME" ]; then
  read -p "Enter JDK path: " jdk_path
  export JAVA_HOME="$jdk_path"
fi

if [ -e "$JAVA_HOME/bin/java" ]; then
  java -jar target/api-app.jar --server.port="$user_port"
else
  echo "Error: JAVA_HOME is set to an invalid directory."
fi

read -p "Press Enter to exit"