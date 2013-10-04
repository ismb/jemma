# a simple collection of scripts to be used to generate the templates http://template-toolkit.org/docs/index.html


#PAGETITLETOBERENAMED3194810948ALKFJALKFJLKJFAIE1OIJ3

all: *.html
	
%.html : *.tt
	echo header.template $^ footer.template $<
	
clean:
	@rm -rf *~ *.html	
	
