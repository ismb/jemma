# a simple collection of scripts to be used to generate the templates http://template-toolkit.org/docs/index.html


#PAGETITLETOBERENAMED3194810948ALKFJALKFJLKJFAIE1OIJ3

all: 
	@bash generate-site.sh
	
clean:
	@rm -rf *~ *.html	

todo:
	@grep -n TODO *.md
	
archive:
	@bash archive.sh
