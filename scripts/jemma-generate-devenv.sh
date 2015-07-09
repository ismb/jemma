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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma
	exit
fi
# Subproject ismb/it.ismb.pert.osgi.dal

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/it.ismb.pert.osgi.dal.git ismb/it.ismb.pert.osgi.dal
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/it.ismb.pert.osgi.dal.git ismb/it.ismb.pert.osgi.dal
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/it.ismb.pert.osgi.dal
	exit
fi
cd ismb/it.ismb.pert.osgi.dal
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/it.ismb.pert.osgi.dal
	exit
fi
# Subproject ismb/it.ismb.pert.osgi.dal.functions

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/it.ismb.pert.osgi.dal.functions.git ismb/it.ismb.pert.osgi.dal.functions
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/it.ismb.pert.osgi.dal.functions.git ismb/it.ismb.pert.osgi.dal.functions
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/it.ismb.pert.osgi.dal.functions
	exit
fi
cd ismb/it.ismb.pert.osgi.dal.functions
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/it.ismb.pert.osgi.dal.functions
	exit
fi
# Subproject ismb/it.ismb.pert.osgi.dal.functions.eh

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/it.ismb.pert.osgi.dal.functions.eh.git ismb/it.ismb.pert.osgi.dal.functions.eh
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/it.ismb.pert.osgi.dal.functions.eh.git ismb/it.ismb.pert.osgi.dal.functions.eh
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/it.ismb.pert.osgi.dal.functions.eh
	exit
fi
cd ismb/it.ismb.pert.osgi.dal.functions.eh
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/it.ismb.pert.osgi.dal.functions.eh
	exit
fi
# Subproject ismb/jemma.osgi.dal

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/jemma.osgi.dal.git ismb/jemma.osgi.dal
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/jemma.osgi.dal.git ismb/jemma.osgi.dal
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/jemma.osgi.dal
	exit
fi
cd ismb/jemma.osgi.dal
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.dal
	exit
fi
# Subproject ismb/it.ismb.pert.osgi.dal.web-apis

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/ismb/it.ismb.pert.osgi.dal.web-apis.git ismb/it.ismb.pert.osgi.dal.web-apis
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:ismb/it.ismb.pert.osgi.dal.web-apis.git ismb/it.ismb.pert.osgi.dal.web-apis
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project ismb/it.ismb.pert.osgi.dal.web-apis
	exit
fi
cd ismb/it.ismb.pert.osgi.dal.web-apis
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/it.ismb.pert.osgi.dal.web-apis
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.javagal
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
cd ismb/jemma-maven-repository
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma-maven-repository
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.greenathome
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.felix.console.web
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
mvn clean package eclipse:eclipse -Declipse.pde install

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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.energyathome
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.configurator
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.adapter.http
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.hac.lib
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.hac
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.javagal.rest
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.javagal.json
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.javagal.gui
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.utils
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
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project ismb/jemma.osgi.ah.greenathome
	exit
fi
# Subproject jemma.osgi.ah.webui.energyathome.base

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/jemma.osgi.ah.webui.energyathome.base.git jemma.osgi.ah.webui.energyathome.base
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:jemma.osgi.ah.webui.energyathome.base.git jemma.osgi.ah.webui.energyathome.base
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project jemma.osgi.ah.webui.energyathome.base
	exit
fi
cd jemma.osgi.ah.webui.energyathome.base
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project jemma.osgi.ah.webui.energyathome.base
	exit
fi
# Subproject jemma.osgi.ah.webui.energyathome

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/jemma.osgi.ah.webui.energyathome.git jemma.osgi.ah.webui.energyathome
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:jemma.osgi.ah.webui.energyathome.git jemma.osgi.ah.webui.energyathome
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project jemma.osgi.ah.webui.energyathome
	exit
fi
cd jemma.osgi.ah.webui.energyathome
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project jemma.osgi.ah.webui.energyathome
	exit
fi
# Subproject jemma.osgi.ah.webui.energyathome.demo

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/jemma.osgi.ah.webui.energyathome.demo.git jemma.osgi.ah.webui.energyathome.demo
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:jemma.osgi.ah.webui.energyathome.demo.git jemma.osgi.ah.webui.energyathome.demo
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project jemma.osgi.ah.webui.energyathome.demo
	exit
fi
cd jemma.osgi.ah.webui.energyathome.demo
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project jemma.osgi.ah.webui.energyathome.demo
	exit
fi
# Subproject jemma.osgi.ah.zigbee

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/jemma.osgi.ah.zigbee.git jemma.osgi.ah.zigbee
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:jemma.osgi.ah.zigbee.git jemma.osgi.ah.zigbee
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project jemma.osgi.ah.zigbee
	exit
fi
cd jemma.osgi.ah.zigbee
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project jemma.osgi.ah.zigbee
	exit
fi
# Subproject jemma.osgi.ah.zigbee.appliances

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/jemma.osgi.ah.zigbee.appliances.git jemma.osgi.ah.zigbee.appliances
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:jemma.osgi.ah.zigbee.appliances.git jemma.osgi.ah.zigbee.appliances
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project jemma.osgi.ah.zigbee.appliances
	exit
fi
cd jemma.osgi.ah.zigbee.appliances
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project jemma.osgi.ah.zigbee.appliances
	exit
fi
# Subproject jemma.osgi.ah.zigbee.appliances.generic

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/jemma.osgi.ah.zigbee.appliances.generic.git jemma.osgi.ah.zigbee.appliances.generic
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:jemma.osgi.ah.zigbee.appliances.generic.git jemma.osgi.ah.zigbee.appliances.generic
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project jemma.osgi.ah.zigbee.appliances.generic
	exit
fi
cd jemma.osgi.ah.zigbee.appliances.generic
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project jemma.osgi.ah.zigbee.appliances.generic
	exit
fi
# Subproject jemma.osgi.ah.io

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/jemma.osgi.ah.io.git jemma.osgi.ah.io
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:jemma.osgi.ah.io.git jemma.osgi.ah.io
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project jemma.osgi.ah.io
	exit
fi
cd jemma.osgi.ah.io
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project jemma.osgi.ah.io
	exit
fi
# Subproject jemma.osgi.ah.hap.client

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/jemma.osgi.ah.hap.client.git jemma.osgi.ah.hap.client
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:jemma.osgi.ah.hap.client.git jemma.osgi.ah.hap.client
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project jemma.osgi.ah.hap.client
	exit
fi
cd jemma.osgi.ah.hap.client
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project jemma.osgi.ah.hap.client
	exit
fi
# Subproject jemma.osgi.ah.m2m.device

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/jemma.osgi.ah.m2m.device.git jemma.osgi.ah.m2m.device
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:jemma.osgi.ah.m2m.device.git jemma.osgi.ah.m2m.device
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project jemma.osgi.ah.m2m.device
	exit
fi
cd jemma.osgi.ah.m2m.device
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project jemma.osgi.ah.m2m.device
	exit
fi
# Subproject jemma.osgi.ah.upnp.energyathome

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/jemma.osgi.ah.upnp.energyathome.git jemma.osgi.ah.upnp.energyathome
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:jemma.osgi.ah.upnp.energyathome.git jemma.osgi.ah.upnp.energyathome
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project jemma.osgi.ah.upnp.energyathome
	exit
fi
cd jemma.osgi.ah.upnp.energyathome
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project jemma.osgi.ah.upnp.energyathome
	exit
fi
# Subproject jemma.osgi.ah.app

cd $ABSENVFOLDER
if  [ $MODE == 'https' ]; then
	git clone https://github.com/jemma.osgi.ah.app.git jemma.osgi.ah.app
elif [ $MODE == 'ssh' ]; then
	git clone git@github.com:jemma.osgi.ah.app.git jemma.osgi.ah.app
else
	echo unknown mode, exiting
	exit
fi
if [ $? -ne 0 ]; then
	echo Checkout failed for project jemma.osgi.ah.app
	exit
fi
cd jemma.osgi.ah.app
mvn clean package eclipse:eclipse -Declipse.pde install

if [ $? -ne 0 ]; then
	echo Maven build failed for project jemma.osgi.ah.app
	exit
fi

echo Your new environment is available in folder $ENVFOLDER
