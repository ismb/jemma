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
