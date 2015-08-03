#!/bin/bash

# This scripts must be executed in a top level folder including all jemma sub-projects
# A good place is the ismb folder that you can find inside envfolder when you use the jemma-generate-devenv.sh script

# Note: this is just a quick and dirty script for internal use - OSGi has more standard ways to do this

# typically launched as
# ./jemma/scripts/jemma-deploy.sh BUNDLENAME VERSION

# Example:

# ./jemma/scripts/jemma-deploy.sh jemma.drafts.org.osgi.osgi.service.dal.functions 1.0.1

# with all the source code directories available in the current folder.

if [ "$#" -ne 2 ]; then
    echo -e "\nThis script must be called with 2 arguments."
    echo -e "\tSyntax: ./jemma/scripts/jemma-deploy.sh BUNDLENAME VERSION"
    echo -e "\tExample: ./jemma/scripts/jemma-deploy.sh jemma.drafts.org.osgi.osgi.service.dal.functions 1.0.1"
    echo -e "\n"
    exit
fi

cd jemma-maven-repository
git checkout gh-pages
git pull
cd ..

#CMD='mvn clean package'

BUNDLENAME=$1
VERSION=$2

echo "deploying bundle $1 version $2 to local jemma-maven-repository"

mvn deploy:deploy-file -Durl=file:///`pwd`/jemma-maven-repository/maven -Dfile=`pwd`/$BUNDLENAME/target/$BUNDLENAME-$VERSION.jar -DgroupId=org.energy-home -DartifactId=$BUNDLENAME -Dpackaging=jar -Dversion=$VERSION

echo -e "\n\nRemember to commit jemma-maven-repository after all bundles have been uploaded"
