#!/usr/bin/python

# This script is meant for developers willing to checkout the entire project (with all its sub-modules) and open it into eclipse
# It uses the information included in the subproject-list.xml file
# by defaults, it checks out all projects in the dev-env subfolder.
# 
# Tested with Linux only
# Made by: Riccardo <tomasi@ismb.it>

import sys, getopt
import os.path
import xml.etree.ElementTree as ET
import signal


def signal_handler(signal, frame):
        print('Aborted.')
        sys.exit(0)


def main(argv):
	signal.signal(signal.SIGINT, signal_handler)

	HELPTEXT = "\nUsage: ./generate-dev-environment.py -l ../subprojects-list.xml -m ssh\n"

	# Based on example from http://www.tutorialspoint.com/python/python_command_line_arguments.htm

	listfile = ''
	mode = ''
	envfolder = ''
	
	try:
		opts, args = getopt.getopt(argv,"l:m:h:e:",["list=","mode=","help","env"])
	except getopt.GetoptError:
		print HELPTEXT
		sys.exit(2)
	for opt, arg in opts:
		if opt in ("-h", "--help"):
			print HELPTEXT
			sys.exit()
		elif opt in ("-l", "--list"):
			listfile = arg
		elif opt in ("-m", "--mode"):
			mode = arg
		elif opt in ("-e", "--env"):
			envfolder = arg			

	if (listfile == ''):
		print "missing listfile"
		print HELPTEXT
		sys.exit()
		
	if not(os.path.exists(listfile)):
		print "list file ["+listfile+"] does not exisit"
		print HELPTEXT
		sys.exit()		
		
	if (mode == ''):
		print "missing mode"
		print HELPTEXT
		sys.exit()
	
	GITPREFIX=''
	GITPOSTFIX='.git'	
	if (mode=='ssh'):
		# example: git@github.com:ismb/jemma.git
		GITPREFIX= "git@github.com:"
	elif (mode=='https'):
		# example: https://github.com/ismb/jemma.git
		GITPREFIX= "https://github.com/"
	else:
		print "invalid mode [" + mode + "] - should be ssh or https"
		print HELPTEXT
		sys.exit()
		
	if (envfolder == ''):
		print "missing env folder - using \"../envfolder\" as default"
		envfolder="../envfolder"

	print "running [listfile="+listfile+"] [mode="+mode+"] [envfolder="+envfolder+"]"
	print "Note: the ["+envfolder+"] folder, if existing, will be deleted - press any key to continue or CTRL-C to abort"
	raw_input()
	
	os.system("rm -rf " + envfolder);
	os.system("mkdir " + envfolder);
	os.chdir(envfolder)
	#this is to absolute path
	envfolderabs = os.getcwd()
	
	tree = ET.parse(listfile)
	root = tree.getroot()
	for child in root:
		
		name = child.find('name').text
		description = child.find('description').text
		checkout = child.find('checkout').text
		mavenbuild = child.find('mavenbuild').text
		print "--------------------------------"
		print "module: " + name + "[checkout="+checkout+"] [mavenbuild="+mavenbuild+"]"
		print "--------------------------------"
		
		if (checkout=="yes"):
			CLONECMD= "git clone " + GITPREFIX + name + GITPOSTFIX + " " + name
			print "cloning by running: " + CLONECMD
			os.system(CLONECMD)
			
			if (mavenbuild=="yes"):
				os.chdir(envfolderabs  + "/" + name)
				MAVENBUILDCMD = "mvn clean package eclipse:eclipse -Declipse.pde install"
				print "generating eclipse project by running: " + MAVENBUILDCMD
				os.system(MAVENBUILDCMD);
				os.chdir(envfolderabs)

if __name__ == "__main__":
   main(sys.argv[1:])
