#!/bin/bash 

# This scripts quickly dumps all bundle versions from any "envfolder" directory

GITVERSIONSFILE=gitversions.txt
POMVERSIONSFILE=pomversions.txt
JEMMAPOMFILE=jemma/pom.xml

echo -e "\nStoring versions of bundles from git repositories in $GITVERSIONSFILE"
echo -e "This script must be run as ./jemma/scripts/dump-version-numbers.sh i.e. inside a valid envfolder\n"
echo -e "WARNING: this process will checkout and pull all master branches from all projects) - this may create problems with any uncommitted changes\n"
echo "Press any key to continue or CTRL-C to exit"
read

rm -rf $GITVERSIONSFILE

for d in  `ls`;
do
	if [ -d "$d" ]
	then
		if [ -e "$d/pom.xml" ]
			then
			echo "- Maven bundle: $d"
			cd $d
			git checkout master
			git pull			
			cd ..
			VER=`grep -m 1 "<version>" $d/pom.xml`
			VER=${VER/<version>/}  
			VER=${VER/<\/version>/}  
			VER=${VER/	/}  
			echo "$d $VER" >> $GITVERSIONSFILE
		elif [ -e "META-INF/MANIFEST.MF" ]
			then
			:
			## Note: all commented out because we don't have these anymore in 1.0.0
			#
			# then
			# echo "- Eclipse bundle: $d"
			# VER=`grep "Bundle-Version" META-INF/MANIFEST.MF`
			# VER=${VER/Bundle-Version: /}  
			# echo -e "\t$VER"
		else
			:
			# echo -e "- Not a bundle: $d\n"
		fi
	fi

done

echo -e "\nVersions of bundles in git projects is now stored in $GITVERSIONSFILE as follows\n\n"
cat $GITVERSIONSFILE
echo -e "\n\n"

rm -rf $POMVERSIONSFILE

# Getting jemma project version

LINE=`grep -m 1 '<version>' $JEMMAPOMFILE`
LINE=${LINE/<version>/ }
LINE=${LINE/<\/version>/ }
LINE=${LINE// /}  
LINE=${LINE//	/}  
echo "jemma $LINE" >> $POMVERSIONSFILE

# getting all other versions numbers from jemma binary pom

while read LINE; do
	if printf -- '%s' "$LINE" | egrep -q -- "<org.energy-home."
	then
		LINE=${LINE//</ }  
		LINE=${LINE//>/ } 
		LINE=${LINE//org.energy-home./ }
		LINE=${LINE//.version/ }
		set -- $LINE
		echo $1 $2 >> $POMVERSIONSFILE
		#stringZ="$LINE"
		#ENDINDEX=`expr index "$stringZ" version`            
		#echo $ENDINDEX
	else
		:
	fi
done < $JEMMAPOMFILE

echo -e "\nVersions of bundles from JEMMA pom file is now stored in $POMVERSIONSFILE as follows\n\n"
cat $POMVERSIONSFILE
echo -e "\n\n"

echo -e "you can now compare by doing e.g. diff $GITVERSIONSFILE $POMVERSIONSFILE\n"





