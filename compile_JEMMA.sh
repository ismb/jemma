#!/bin/bash

sudo apt-get update

if [ ! -d /usr/share/java ]
then
  echo "Java does not exist" 
  sleep 2
  echo "Starting download  Java 7"
  sleep 1
  sudo apt-get purge openjdk*
  sudo add-apt-repository ppa:webupd8team/java
  sudo apt-get install oracle-java7-installer
  echo "Download finished"
fi
VER=`java -version 2>&1 | grep "java version" | awk '{print $3}' | tr -d \" | awk '{split($0, array, ".")} END{print array[2]}'`
if [ ! $VER > 6 ]
then 
  echo "Java is outdated. Starting download Java 7"
  sleep 1
  sudo apt-get purge openjdk*
  sudo add-apt-repository ppa:webupd8team/java
  sudo apt-get install oracle-java7-installer
  echo "Download finished"
fi

if [ ! -d /usr/share/maven ]
then	
  echo "Maven is not installed."
  sleep 4
  echo "Start Maven download"
  sleep 2
  sudo apt-get install maven
  sudo "Finished installation"
  
fi  
echo "Start compiling JEMMA"
sleep 1
mvn clean package eclipse:eclipse -D eclipse.pde install
echo "Finished."
sleep 1
echo "Now you can import JEMMA modules into your IDE. If you are using Eclipse please check the option 'Check for nested project'.\n For more information visit https://github.com/ismb/jemma/wiki"
