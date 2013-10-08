#!/bin/bash
DATE=`date +%Y%m%d%H%M`
echo Generating site - version - $DATE

echo "Auto-generated site: $DATE" > site-generation.log
echo "" >> site-generation.log
echo "##########################" >> site-generation.log
echo "Git status" >> site-generation.log
echo "##########################" >> site-generation.log
echo "" >> site-generation.log
git status >> site-generation.log
echo "" >> site-generation.log
echo "##########################" >> site-generation.log
echo "Git log (first 30 lines)" >> site-generation.log
echo "##########################" >> site-generation.log
echo "" >> site-generation.log
git log| head -n 30 >> site-generation.log



for mdfile in  `ls -1 *.md`;
do
	noextfile="${mdfile%%.*}"
	title=$(head -n 1 $mdfile)
	htmlfile="$noextfile.html"
	echo -e "\\t Template $mdfile -> $noextfile -> generating $htmlfile -> title $title"
	#echo "<!-- Auto-generated site - version $DATE-->" > $htmlfile
	cat header.template >> $htmlfile
	sed -i "s/PAGETITLETOBERENAMED3194810948ALKFJALKFJLKJFAIE1OIJ3/$title/g" $htmlfile
	tail -n +2 "$mdfile" |pandoc >> $htmlfile
	cat footer.template >> $htmlfile 
done



