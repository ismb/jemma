An administrative GUI for java GAL
==================================

##Changing root context

To change the root context (the path) of the GUI application, you need to change the file jemma.osgi.javagal.gui/OSGI-INF/javagalwebgui.xml by specifying a a value for the *rootContext* property.

An example:

	<property name="rootContext" type="String" value="Zigbee"/>

In this case the web application will be available at the address http://address:port/Zigbee
