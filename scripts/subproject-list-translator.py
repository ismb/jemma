#!/usr/bin/python

# This script has been developed for core JEMMA developers
# For help, type:
# ./subprojects-list.xml --help 
#
# Tested with Linux only"
# Made by: Riccardo <tomasi@ismb.it>"

import sys, getopt
import os.path
import xml.etree.ElementTree as ET
import signal

def signal_handler(signal, frame):
        print('Aborted.')
        sys.exit(0)


def main(argv):
	signal.signal(signal.SIGINT, signal_handler)

	ERRTEXT = "For help, type: ./subproject-list-translator.py --help\n"
	
	HELPTEXT="\nThis script has been developed for core JEMMA developers.\n\n"
	HELPTEXT+="It helps the information included in the subproject-list.xml file into:\n"
	HELPTEXT+="- bash scripts to checkout the entire project (with all its sub-modules) and generate an environment usable with Eclipse\n"
	HELPTEXT+="- markdown file list suitable e.g. for the website or the main ReadMe file\n"
	HELPTEXT+="- pom dependencies\n"
	HELPTEXT+="\n"
	HELPTEXT+="Syntax:\n"
	HELPTEXT+="\n"
	HELPTEXT+="./subproject-list-translator.py ../subprojects-list.xml -i <inputfile> -c <command>\n"
	HELPTEXT+="\n"
	HELPTEXT+="Examples:\n"
	HELPTEXT+="\n"
	HELPTEXT+="	Generate bash script to checkout the overall project:\n"
	HELPTEXT+="	./subproject-list-translator.py -i ../subprojects-list.xml -c git | tee jemma-generate-devenv.sh; chmod +x jemma-generate-devenv.sh\n"
	HELPTEXT+="\n"
	HELPTEXT+="	Generate markdown table:\n"
	HELPTEXT+="	./subproject-list-translator.py -i ../subprojects-list.xml -c md\n"
	HELPTEXT+="\n"
	HELPTEXT+="	Generate pom-style dependencies:\n"
	HELPTEXT+="	./subproject-list-translator.py - ../subprojects-list.xml -c pom\n"
	HELPTEXT+="\n"
	HELPTEXT+="Options:\n"
	HELPTEXT+="	-i, --input : the imput subproject list file\n"
	HELPTEXT+="	-c, --cmd	: command (git for git scripts, md for markdown, pom for pom dependencies)\n"
	HELPTEXT+=""

	inputfile = ''
	cmd=''

	try:
		opts, args = getopt.getopt(argv,":h:i:c:",["help","input","cmd"])
	except getopt.GetoptError:
		print ERRTEXT
		sys.exit(2)
		
	for opt, arg in opts:
		if opt in ("-h", "--help"):
			print HELPTEXT
			sys.exit()
		elif opt in ("-i", "--input"):
			inputfile = arg
		elif opt in ("-c", "--cmd"):
			cmd = arg
		else:
			print "unexpected empty argument"
			print ERRTEXT
			sys.exit(2)

	if (inputfile == ''):
		print "Missing input file"
		print ERRTEXT
		sys.exit()

	if (cmd == ''):
		print "Missing cmd"
		print ERRTEXT
		sys.exit()
	
	if (cmd != 'md') & (cmd != 'pom') & (cmd != 'git'):
		print "unexpected [cmd="+cmd+"]" 
		print ERRTEXT
		sys.exit()		
		
	if not(os.path.exists(inputfile)):
		print "list file ["+inputfile+"] does not exisit"
		print ERRTEXT
		sys.exit()		
	
	# example: https://github.com/ismb/jemma.git
	GITPREFIXHTTPS= "https://github.com/"

	# example: git@github.com:ismb/jemma.git
	GITPREFIXSSH= "git@github.com:"

	GITPOSTFIX='.git'	
	GITENVFOLDER="../envfolder"

	tree = ET.parse(inputfile)
	root = tree.getroot()
	
	# Header-position text
		
	if (cmd == 'md'):
		print "| Project | Description | Version |"
		print "| :------:|:------------|:-----:|"		
	elif (cmd == 'pom'):
		print "<dependencies>"
	elif (cmd == 'git'):
		print "#!/bin/bash"
		print ""
		print "echo -------------------------------"
		print "echo Starting github checkout script"
		print "echo -------------------------------"
		print ""
		print "ENVFOLDER=../envfolder"
		print ""
		print "# Uncomment ssh to use ssh checkout mode instead of https"
		print "MODE=https"
		print "#MODE=ssh"
		print ""
		print "#checking if ENVFOLDER $ENVFOLDER is empty"
		print "if [ -d \"$ENVFOLDER\" ]; then"
		print "\techo target folder $ENVFOLDER already exisit. Aborting."
		print "\t\texit"
		print "fi"
		print ""
		print "mkdir $ENVFOLDER"
		print "cd $ENVFOLDER"
		print "ABSENVFOLDER=`pwd`"
		print ""
		print "# ------------------------"
		print "# PROJECTS SECTION"
		print "# ------------------------"
		print ""
	else:
		print "unreachable code"
		
	# Loop text	
		
	for child in root:
		name = child.find('name').text
		description = child.find('description').text
		checkout = child.find('checkout').text
		mavenbuild = child.find('mavenbuild').text
		version = child.find('version').text
		myurl= "https://github.com/" + name
		#print "--------------------------------"
		#print "module: " + name + " [checkout="+checkout+"] [mavenbuild="+mavenbuild+"]"
		#print "--------------------------------"
		
		if (cmd == 'md'):
			print "| ["+name+"]("+myurl+") | "+description+" | " + version + " |" 
		elif (cmd == 'pom'):
			artifact=name.replace("ismb/","")
			print "\t<dependency>\n\t\t<groupId>org.energy-home</groupId>\n\t\t<artifactId>"+artifact+"</artifactId>\n\t\t<version>"+version+"</version>\n\t</dependency>"
		elif (cmd == 'git'):
			CLONECMDHTTPS= "git clone " + GITPREFIXHTTPS + name + GITPOSTFIX + " " + name
			CLONECMDSSH= "git clone " + GITPREFIXSSH + name + GITPOSTFIX + " " + name
			MAVENBUILDCMD = "mvn clean package eclipse:eclipse -Declipse.pde install"
			print "# Subproject " + name
			print ""
			print "cd $ABSENVFOLDER"
			print "if  [ $MODE == 'https' ]; then"
			print "\t" + CLONECMDHTTPS
			print "elif [ $MODE == 'ssh' ]; then"
			print "\t" + CLONECMDSSH
			print "else"
			print "\techo unknown mode, exiting"
			print "\texit"
			print "fi"
			print "if [ $? -ne 0 ]; then"
			print "\techo Checkout failed for project " + name
			print "\texit"
			print "fi"			
			if (mavenbuild == 'yes'):
				print "cd " + name
				print MAVENBUILDCMD
				print ""
				print "if [ $? -ne 0 ]; then"
				print "\techo Maven build failed for project " + name
				print "\texit"
				print "fi"			
		else:
			print "unreachable code"
		
	# Footer-position text
		
	if (cmd == 'md'):
		pass
	elif (cmd == 'pom'):
		print "</dependencies>"
	elif (cmd == 'git'):
		print ""
		print "echo Your new environment is available in folder $ENVFOLDER"
	else:
		print "unreachable code"

if __name__ == "__main__":
	main(sys.argv[1:])
   





