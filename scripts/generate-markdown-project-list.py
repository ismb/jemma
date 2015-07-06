#!/usr/bin/python

# This script is meant for internal use by core JEMMA developers only.
# It generates a markdown-formatted list of projects suitable to be included in the main README.md file of the project and in the project website.
# It uses the information included in the subproject-list.xml file
# 
# Tested with Linux only
# Made by: Riccardo <tomasi@ismb.it>
#

import sys, getopt
import os.path
import xml.etree.ElementTree as ET

def main(argv):
	HELPTEXT = "\nUsage: ./generate-markdown-project-list.py -l ../subprojects-list.xml\n"

	# Based on example from http://www.tutorialspoint.com/python/python_command_line_arguments.htm

	listfile = ''
	
	try:
		opts, args = getopt.getopt(argv,"l:h:",["list=","mode=","help"])
	except getopt.GetoptError:
		print HELPTEXT
		sys.exit(2)
	for opt, arg in opts:
		if opt in ("-h", "--help"):
			print HELPTEXT
			sys.exit()
		elif opt in ("-l", "--list"):
			listfile = arg

	if (listfile == ''):
		print "missing listfile"
		print HELPTEXT
		sys.exit()
		
	if not(os.path.exists(listfile)):
		print "list file ["+listfile+"] does not exisit"
		print HELPTEXT
		sys.exit()			
		
	print "running [listfile="+listfile+"]"
	print "------------------------------------------"
	print "the following text can be pasted in the main readme.md or in the jemma website"
	print "------------------------------------------\n\n"
	
	print "| Project | Description |"
	print "| :------:|:------------|"
	tree = ET.parse(listfile)
	root = tree.getroot()
	for child in root:
		name = child.find('name').text
		description = child.find('description').text
		myurl= "https://github.com/" + name
		print "| ["+name+"]("+myurl+") | "+description+"|"
		

if __name__ == "__main__":
   main(sys.argv[1:])
   
   
   
   
   





   
