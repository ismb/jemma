An administrative GUI for java GAL
==================================

##Changing root context

To change the root context (the path) of the GUI application, you need to change the file jemma.osgi.javagal.gui/OSGI-INF/javagalwebgui.xml by specifying a a value for the *rootContext* property.

An example:

	<property name="rootContext" type="String" value="zigbee"/>

In this case the web application will be available at the address http://address:port/zigbee

##Setting administrative user/pass for java GAL administrative GUI

In order to get a local login/pass for the javaGAL GUI, you should add properties, e.g. in your launch configuration, as in the following example:

``````
-Dorg.energy_home.jemma.username=javagaladmin
-Dorg.energy_home.jemma.password=javagaladmin
``````



