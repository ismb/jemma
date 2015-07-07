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
cd ismb/jemma
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/it.ismb.pert.osgi.dal
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/it.ismb.pert.osgi.dal.functions
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/it.ismb.pert.osgi.dal.functions.eh
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/jemma.osgi.dal
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/it.ismb.pert.osgi.dal.web-apis
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/jemma.osgi.javagal
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/jemma-maven-repository
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/jemma.osgi.ah.greenathome
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/jemma.osgi.ah.felix.console.web
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/jemma.osgi.ah.demo.fakevalues
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/jemma.osgi.ah.energyathome
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/jemma.osgi.ah.configurator
mvn clean package eclipse:eclipse -Declipse.pde install

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
cd ismb/jemma.osgi.ah.adapter.http
mvn clean package eclipse:eclipse -Declipse.pde install


echo Your new environment is available in folder $ENVFOLDER
