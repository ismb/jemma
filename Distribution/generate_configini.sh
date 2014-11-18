#!/bin/bash

echo "#Configuration file"
echo ""
echo -n "osgi.bundles="

for x in `ls plugins`;
do
        echo "plugins/$x@start,\\"
done

echo ""
echo "osgi.bundles.defaultStartLevel=4"
echo "eclipse.ignoreApp=true"
echo "osgi.noShutdown=true"
echo "org.osgi.framework.bootdelegation = *"
echo "equinox.ds.error=true"
echo "equinox.ds.print=true"
echo "org.osgi.service.http.port=1024"


echo "it.telecomitalia.ah.driver.autoinstall=true"

echo "zgd.dongle.uri=/dev/ttyUSB0"
echo "zgd.dongle.speed=115200"
echo "zgd.dongle.type=freescale"

echo "org.osgi.service.http.port=8080"
echo "org.eclipse.equinox.http.jetty.http.port=8080"
echo "org.eclipse.equinox.http.jetty.https.enabled=false"
echo "org.eclipse.equinox.http.jetty.https.port=443"

echo "org.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog"
echo "org.apache.commons.logging.simplelog.defaultlog=debug"
echo "org.apache.commons.logging.simplelog.showlogname=true"
echo "org.apache.commons.logging.simplelog.showShortLogname=true"
echo "org.apache.commons.logging.simplelog.showdatetime=false"
echo "org.apache.commons.logging.simplelog.log.it.telecomitalia.internal.ah.m2m=INFO"
echo "org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient=ERROR"
echo "org.apache.commons.logging.simplelog.log.httpclient.wire.header=ERROR"
echo "org.apache.commons.logging.simplelog.log.org.apache.http=ERROR"
echo "org.apache.commons.logging.simplelog.log.org.apache.http.wire=ERROR"
echo "org.apache.commons.logging.simplelog.log.org.energy_home.jemma.javagal=DEBUG"

echo "felix.webconsole.username=admin"
echo "felix.webconsole.password=admin"

echo "org.energy_home.jemma.javagal.username=admin"
echo "org.energy_home.jemma.javagal.password=password"

echo "org.energy_home.jemma.ah.configuration.file=EmptyConfig"

echo "org.ops4j.pax.logging.DefaultServiceLog.level=DEBUG"
