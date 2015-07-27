#!/bin/bash

# FIXME this should be generalized using system variable
CURDIR=/home/pi/jemma-bin-0.9.4
cd $CURDIR
java -jar org.eclipse.osgi-3.10.0.jar -console
