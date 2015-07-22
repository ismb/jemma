#!/bin/bash
CURDIR=$(pwd)
echo launching - screen -d -m bash $CURDIR/start.sh
screen -d -m bash $CURDIR/start.sh

#useful for raspberry pi's rc.local:
#sudo su pi -c 'screen -d -m bash /home/pi/jemma/start.sh'
