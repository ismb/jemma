#!/bin/bash

echo -------------------------------
echo Starting github checkout script
echo -------------------------------

ENVFOLDER=../envfolder

# Uncomment ssh to use ssh checkout mode instead of https
MODE=https
#MODE=ssh

#checking if ENVFOLDER $ENVFOLDER is empty
if [ -d "$ENVFOLDER" ]; then
	echo target folder $ENVFOLDER already exisit. Aborting.
		exit
fi

mkdir $ENVFOLDER
cd $ENVFOLDER
ABSENVFOLDER=`pwd`

# ------------------------
# PROJECTS SECTION
# ------------------------

# Subproject ismb/jemma

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.git ismb/jemma
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.git ismb/jemma
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma
	exit
fi
cd ismb/jemma


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma
	exit
fi
# Subproject ismb/jemma.osgi.dal.adapter

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.dal.adapter.git ismb/jemma.osgi.dal.adapter
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.dal.adapter.git ismb/jemma.osgi.dal.adapter
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.dal.adapter
	exit
fi
cd ismb/jemma.osgi.dal.adapter


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.dal.adapter
	exit
fi
# Subproject ismb/jemma.drafts.org.osgi.osgi.service.dal

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.drafts.org.osgi.osgi.service.dal.git ismb/jemma.drafts.org.osgi.osgi.service.dal
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.drafts.org.osgi.osgi.service.dal.git ismb/jemma.drafts.org.osgi.osgi.service.dal
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.drafts.org.osgi.osgi.service.dal
	exit
fi
cd ismb/jemma.drafts.org.osgi.osgi.service.dal


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.drafts.org.osgi.osgi.service.dal
	exit
fi
# Subproject ismb/jemma.drafts.org.osgi.osgi.service.dal.functions

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.drafts.org.osgi.osgi.service.dal.functions.git ismb/jemma.drafts.org.osgi.osgi.service.dal.functions
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.drafts.org.osgi.osgi.service.dal.functions.git ismb/jemma.drafts.org.osgi.osgi.service.dal.functions
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.drafts.org.osgi.osgi.service.dal.functions
	exit
fi
cd ismb/jemma.drafts.org.osgi.osgi.service.dal.functions


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.drafts.org.osgi.osgi.service.dal.functions
	exit
fi
# Subproject ismb/jemma.osgi.dal.functions.eh

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.dal.functions.eh.git ismb/jemma.osgi.dal.functions.eh
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.dal.functions.eh.git ismb/jemma.osgi.dal.functions.eh
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.dal.functions.eh
	exit
fi
cd ismb/jemma.osgi.dal.functions.eh


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.dal.functions.eh
	exit
fi
# Subproject ismb/jemma.osgi.dal.web-apis

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.dal.web-apis.git ismb/jemma.osgi.dal.web-apis
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.dal.web-apis.git ismb/jemma.osgi.dal.web-apis
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.dal.web-apis
	exit
fi
cd ismb/jemma.osgi.dal.web-apis


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.dal.web-apis
	exit
fi
# Subproject ismb/jemma.osgi.ah.adapter.http

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.adapter.http.git ismb/jemma.osgi.ah.adapter.http
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.adapter.http.git ismb/jemma.osgi.ah.adapter.http
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.adapter.http
	exit
fi
cd ismb/jemma.osgi.ah.adapter.http


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.adapter.http
	exit
fi
# Subproject ismb/jemma.osgi.ah.app

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.app.git ismb/jemma.osgi.ah.app
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.app.git ismb/jemma.osgi.ah.app
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.app
	exit
fi
cd ismb/jemma.osgi.ah.app


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.app
	exit
fi
# Subproject ismb/jemma.osgi.ah.configurator

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.configurator.git ismb/jemma.osgi.ah.configurator
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.configurator.git ismb/jemma.osgi.ah.configurator
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.configurator
	exit
fi
cd ismb/jemma.osgi.ah.configurator


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.configurator
	exit
fi
# Subproject ismb/jemma.osgi.ah.demo.fakevalues

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.demo.fakevalues.git ismb/jemma.osgi.ah.demo.fakevalues
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.demo.fakevalues.git ismb/jemma.osgi.ah.demo.fakevalues
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.demo.fakevalues
	exit
fi
cd ismb/jemma.osgi.ah.demo.fakevalues


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.demo.fakevalues
	exit
fi
# Subproject ismb/jemma.osgi.ah.energyathome

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.energyathome.git ismb/jemma.osgi.ah.energyathome
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.energyathome.git ismb/jemma.osgi.ah.energyathome
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.energyathome
	exit
fi
cd ismb/jemma.osgi.ah.energyathome


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.energyathome
	exit
fi
# Subproject ismb/jemma.osgi.ah.felix.console.web

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.felix.console.web.git ismb/jemma.osgi.ah.felix.console.web
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.felix.console.web.git ismb/jemma.osgi.ah.felix.console.web
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.felix.console.web
	exit
fi
cd ismb/jemma.osgi.ah.felix.console.web


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.felix.console.web
	exit
fi
# Subproject ismb/jemma.osgi.ah.greenathome

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.greenathome.git ismb/jemma.osgi.ah.greenathome
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.greenathome.git ismb/jemma.osgi.ah.greenathome
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.greenathome
	exit
fi
cd ismb/jemma.osgi.ah.greenathome


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.greenathome
	exit
fi
# Subproject ismb/jemma.osgi.ah.hac

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.hac.git ismb/jemma.osgi.ah.hac
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.hac.git ismb/jemma.osgi.ah.hac
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.hac
	exit
fi
cd ismb/jemma.osgi.ah.hac


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.hac
	exit
fi
# Subproject ismb/jemma.osgi.ah.hac.lib

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.hac.lib.git ismb/jemma.osgi.ah.hac.lib
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.hac.lib.git ismb/jemma.osgi.ah.hac.lib
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.hac.lib
	exit
fi
cd ismb/jemma.osgi.ah.hac.lib


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.hac.lib
	exit
fi
# Subproject ismb/jemma.osgi.ah.hap.client

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.hap.client.git ismb/jemma.osgi.ah.hap.client
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.hap.client.git ismb/jemma.osgi.ah.hap.client
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.hap.client
	exit
fi
cd ismb/jemma.osgi.ah.hap.client


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.hap.client
	exit
fi
# Subproject ismb/jemma.osgi.ah.io

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.io.git ismb/jemma.osgi.ah.io
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.io.git ismb/jemma.osgi.ah.io
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.io
	exit
fi
cd ismb/jemma.osgi.ah.io


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.io
	exit
fi
# Subproject ismb/jemma.osgi.ah.m2m.device

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.m2m.device.git ismb/jemma.osgi.ah.m2m.device
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.m2m.device.git ismb/jemma.osgi.ah.m2m.device
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.m2m.device
	exit
fi
cd ismb/jemma.osgi.ah.m2m.device


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.m2m.device
	exit
fi
# Subproject ismb/jemma.osgi.ah.upnp.energyathome

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.upnp.energyathome.git ismb/jemma.osgi.ah.upnp.energyathome
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.upnp.energyathome.git ismb/jemma.osgi.ah.upnp.energyathome
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.upnp.energyathome
	exit
fi
cd ismb/jemma.osgi.ah.upnp.energyathome


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.upnp.energyathome
	exit
fi
# Subproject ismb/jemma.osgi.ah.webui.base

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.webui.base.git ismb/jemma.osgi.ah.webui.base
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.webui.base.git ismb/jemma.osgi.ah.webui.base
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.webui.base
	exit
fi
cd ismb/jemma.osgi.ah.webui.base


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.webui.base
	exit
fi
# Subproject ismb/jemma.osgi.ah.webui.energyathome

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.webui.energyathome.git ismb/jemma.osgi.ah.webui.energyathome
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.webui.energyathome.git ismb/jemma.osgi.ah.webui.energyathome
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.webui.energyathome
	exit
fi
cd ismb/jemma.osgi.ah.webui.energyathome


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.webui.energyathome
	exit
fi
# Subproject ismb/jemma.osgi.ah.webui.energyathome.demo

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.webui.energyathome.demo.git ismb/jemma.osgi.ah.webui.energyathome.demo
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.webui.energyathome.demo.git ismb/jemma.osgi.ah.webui.energyathome.demo
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.webui.energyathome.demo
	exit
fi
cd ismb/jemma.osgi.ah.webui.energyathome.demo


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.webui.energyathome.demo
	exit
fi
# Subproject ismb/jemma.osgi.ah.zigbee

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.zigbee.git ismb/jemma.osgi.ah.zigbee
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.zigbee.git ismb/jemma.osgi.ah.zigbee
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.zigbee
	exit
fi
cd ismb/jemma.osgi.ah.zigbee


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.zigbee
	exit
fi
# Subproject ismb/jemma.osgi.ah.zigbee.appliances

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.zigbee.appliances.git ismb/jemma.osgi.ah.zigbee.appliances
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.zigbee.appliances.git ismb/jemma.osgi.ah.zigbee.appliances
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.zigbee.appliances
	exit
fi
cd ismb/jemma.osgi.ah.zigbee.appliances


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.zigbee.appliances
	exit
fi
# Subproject ismb/jemma.osgi.ah.zigbee.appliances.generic

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.ah.zigbee.appliances.generic.git ismb/jemma.osgi.ah.zigbee.appliances.generic
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.ah.zigbee.appliances.generic.git ismb/jemma.osgi.ah.zigbee.appliances.generic
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.ah.zigbee.appliances.generic
	exit
fi
cd ismb/jemma.osgi.ah.zigbee.appliances.generic


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.zigbee.appliances.generic
	exit
fi
# Subproject ismb/jemma.osgi.javagal

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.javagal.git ismb/jemma.osgi.javagal
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.javagal.git ismb/jemma.osgi.javagal
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.javagal
	exit
fi
cd ismb/jemma.osgi.javagal


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.javagal
	exit
fi
# Subproject ismb/jemma.osgi.javagal.gui

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.javagal.gui.git ismb/jemma.osgi.javagal.gui
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.javagal.gui.git ismb/jemma.osgi.javagal.gui
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.javagal.gui
	exit
fi
cd ismb/jemma.osgi.javagal.gui


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.javagal.gui
	exit
fi
# Subproject ismb/jemma.osgi.javagal.json

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.javagal.json.git ismb/jemma.osgi.javagal.json
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.javagal.json.git ismb/jemma.osgi.javagal.json
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.javagal.json
	exit
fi
cd ismb/jemma.osgi.javagal.json


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.javagal.json
	exit
fi
# Subproject ismb/jemma.osgi.javagal.rest

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.javagal.rest.git ismb/jemma.osgi.javagal.rest
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.javagal.rest.git ismb/jemma.osgi.javagal.rest
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.javagal.rest
	exit
fi
cd ismb/jemma.osgi.javagal.rest


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.javagal.rest
	exit
fi
# Subproject ismb/jemma.osgi.utils

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.utils.git ismb/jemma.osgi.utils
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.utils.git ismb/jemma.osgi.utils
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.utils
	exit
fi
cd ismb/jemma.osgi.utils


if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.utils
	exit
fi
# Subproject ismb/jemma-maven-repository

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma-maven-repository.git ismb/jemma-maven-repository
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma-maven-repository.git ismb/jemma-maven-repository
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma-maven-repository
	exit
fi

echo Your new environment is available in folder $ENVFOLDER
