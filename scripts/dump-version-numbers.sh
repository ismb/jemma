#!/bin/bash 

for d in  `ls`;
do
	if [ -d "$d" ]
	then
		cd $d
		if [ -e "pom.xml" ]
			then
			echo "- Maven bundle: $d"
			VER=`grep -m 1 "<version>" pom.xml`
			VER=${VER/<version>/}  
			VER=${VER/<\/version>/}  
			VER=${VER/	/}  
			echo -e "\t$VER"
		elif [ -e "META-INF/MANIFEST.MF" ]
			then
			echo "- Eclipse bundle: $d"
			VER=`grep "Bundle-Version" META-INF/MANIFEST.MF`
			VER=${VER/Bundle-Version: /}  
			echo -e "\t$VER"
		else
			echo -e "- Not a bundle: $d\n"
		fi
		cd ..
	fi

done
