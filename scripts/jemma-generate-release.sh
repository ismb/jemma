#!/bin/bash

# This scripts must be executed in a top level folder including all jemma sub-projects
# A good place is the ismb folder that you can find inside envfolder when you use the jemma-generate-devenv.sh script

# Note: this is just a quick and dirty script for internal use - OSGi has more standard ways to do this

# typically launched as
#	-  ./jemma/scripts/jemma-generate-release.sh 

# with all the source code directories available in the current folder.

CMD='mvn clean package'

for d in  `ls`;
do
	if [ -d "$d" ]
	then
		cd $d
		if [ -e "pom.xml" ]
			then
			echo -e '\n\n****************************************'
			echo running [$CMD] in [$d]
			echo -e '****************************************\n'
			$CMD
		else
			echo -e '\n\n****************************************'
			echo "Skipping [$d] (probably this must be exported manually)"
			echo -e '****************************************\n'		
		fi
		cd ..
	fi

done

rm -rf target
mkdir target
mkdir target/plugins
cp jemma/Distribution/target/jemma.Distribution-*-bin/modules/dependencies/*.jar target/plugins/
cp jemma/Distribution/target/jemma.Distribution-*-bin/modules/dependencies/org.eclipse.osgi-3.10.0.jar target/
cp jemma/scripts/start*.sh target/
chmod +x target/*.sh
cp jemma/scripts/start*.bat target/
chmod +x target/*.bat


cd target
mkdir configuration
bash ../jemma/scripts/generate_configini.sh | tee configuration/config.ini

cd ..


for d in  `ls`;
do
	if [ -d "$d" ]
	then
		if [ -e "$d/pom.xml" ]
			then

			echo -e '\n\n****************************************'
			echo copying jar from [$d] 
			echo -e '****************************************\n'
			cp $d/target/*.jar target/plugins
		else
			echo -e '\n\n****************************************'
			echo "Skipping [$d] (probably this must be exported manually)"
			echo -e '****************************************\n'		
		fi
	fi

done



echo  -e "\n\nYour distribution is now available in the target folder, ready to be zipped."

echo -e '\n\n****************************************'
echo "The following bundles must probably be exported manually"
echo -e '****************************************\n'

for d in  `ls`;
do
	if [ -d "$d" ]
	then
		cd $d
		if [ ! -e "pom.xml" ]
			then
			echo "- $d"
		fi
		cd ..
	fi

done

echo  -e "\n\n(after this is done remember to copy generated bundles in the target/plugins directory and run generate_configini.sh properly from the target directory)"
echo -e "example: bash ../jemma/scripts/generate_configini.sh | tee configuration/config.ini"




