ZigBee Network Manager
======================

This bundle is the network manager for the Zigbee network and interacts with the GAL. It receives devices notifications from the GAL and, when discovers a device for a first time, it starts the endpoints discovery phase. For each discovered endpoint, it registers a ZigBeeDevice OSGi service exposing APIs to interact with the device.