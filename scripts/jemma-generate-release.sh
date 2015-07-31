#!/bin/bash

# This scripts must be executed inside the **jemma** folder resulting from a fresh clone of the *releases* branch .
# Note: this is just a quick and dirty script for internal use - OSGi has more standard ways to do this

# typically launched as
#	-  ./scripts/jemma-generate-release.sh 

mvn clean package

RELEASE_FOLDER_NAME=./release

rm -rf $RELEASE_FOLDER_NAME
mkdir $RELEASE_FOLDER_NAME
mkdir $RELEASE_FOLDER_NAME/plugins
cp jemma-equinox-runtime/target/dependency/*.jar $RELEASE_FOLDER_NAME/plugins/
cp jemma-bundles/target/dependency/* $RELEASE_FOLDER_NAME/plugins/
cp jemma-equinox-runtime/target/dependency/org.eclipse.osgi-3.10.0.jar $RELEASE_FOLDER_NAME/
cp scripts/start*.sh $RELEASE_FOLDER_NAME/
chmod +x $RELEASE_FOLDER_NAME/*.sh
cp scripts/start*.bat $RELEASE_FOLDER_NAME/
chmod +x release/*.bat

mkdir $RELEASE_FOLDER_NAME/osgi-instance-area
mkdir $RELEASE_FOLDER_NAME/configuration

bash ../scripts/generate_configini.sh ../scripts/config.ini.template | tee $RELEASE_FOLDER_NAME/configuration/config.ini

echo  -e "\n\nYour distribution is now available in the $RELEASE_FOLDER_NAME folder, ready to be zipped."




