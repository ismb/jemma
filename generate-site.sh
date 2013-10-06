#!/bin/bash
DATE=`date +%Y%m%d%H%M`
echo Generating site - version - $DATE


for ttfile in  `ls -1 *.tt`;
do
	noextfile="${ttfile%%.*}"
	title=$(head -n 1 $ttfile)
	htmlfile="$noextfile.html"
	echo -e "\\t Template $ttfile -> $noextfile -> generating $htmlfile -> title $title"
	echo "<!-- Auto-generated site - version $DATE-->" > $htmlfile
	cat header.template >> $htmlfile
	sed -i "s/PAGETITLETOBERENAMED3194810948ALKFJALKFJLKJFAIE1OIJ3/$title/g" $htmlfile
	tail -n +2 "$ttfile" >> $htmlfile
	cat footer.template >> $htmlfile 
done



