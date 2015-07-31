#!/bin/bash

# This scripts must be executed in a top level folder including all jemma sub-projects
# A good place is the ismb folder that you can find inside envfolder when you use the jemma-generate-devenv.sh script

# Note: this is just a quick and dirty script for internal use - OSGi has more standard ways to do this

# typically launched as
# ./jemma/scripts/jemma-build-all.sh 

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
