Components
<!-- Remember: the first line always goes with the title-->
<!-- Please use h3 headers (###) inside these files -->

Starting from version 0.9, JEMMA is maintaned as a set of OSGi modules/projects, as listed in the following table:

<div class="jemmahwtablesfather">

| Project | Description | Version |
| :------:|:------------|:-----:|
| [ismb/jemma](https://github.com/ismb/jemma) | The core set of JEMMA components | 1.0.0-rc0 |
| [ismb/jemma.osgi.dal.adapter](https://github.com/ismb/jemma.osgi.dal.adapter) | JEMMA Dal Adapter | 1.1.1 |
| [ismb/jemma.drafts.org.osgi.osgi.service.dal](https://github.com/ismb/jemma.drafts.org.osgi.osgi.service.dal) | Dal Base Classes (draft) | 1.0.1 |
| [ismb/jemma.drafts.org.osgi.osgi.service.dal.functions](https://github.com/ismb/jemma.drafts.org.osgi.osgi.service.dal.functions) | Dal Base Functions (draft) | 1.0.1 |
| [ismb/jemma.osgi.dal.functions.eh](https://github.com/ismb/jemma.osgi.dal.functions.eh) | DAL Classes Functions for Energy@home devicess | 1.0.1 |
| [ismb/jemma.osgi.dal.web-apis](https://github.com/ismb/jemma.osgi.dal.web-apis) | JEMMA REST and WebSocket API to access Devices, Functions and Events available in OSGi Device Abstraction Layer | 2.0.1 |
| [ismb/jemma.osgi.ah.adapter.http](https://github.com/ismb/jemma.osgi.ah.adapter.http) | JEMMA HTTP AH Adapter | 2.0.4 |
| [ismb/jemma.osgi.ah.app](https://github.com/ismb/jemma.osgi.ah.app) | This bundle is just a placeholder for the whole framework version | 3.3.1 |
| [ismb/jemma.osgi.ah.configurator](https://github.com/ismb/jemma.osgi.ah.configurator) | jemma.osgi.ah.configurator | 1.0.8 |
| [ismb/jemma.osgi.ah.demo.fakevalues](https://github.com/ismb/jemma.osgi.ah.demo.fakevalues) | Fake devices for JEMMA testing/tutorials | 0.1.2 |
| [ismb/jemma.osgi.ah.energyathome](https://github.com/ismb/jemma.osgi.ah.energyathome) | Energy@Home Home Automation bundle | 2.2.31 |
| [ismb/jemma.osgi.ah.felix.console.web](https://github.com/ismb/jemma.osgi.ah.felix.console.web) | Apache web console plugin for Jemma | 1.0.10 |
| [ismb/jemma.osgi.ah.greenathome](https://github.com/ismb/jemma.osgi.ah.greenathome) | Greenathome Appliance | 2.1.21 |
| [ismb/jemma.osgi.ah.hac](https://github.com/ismb/jemma.osgi.ah.hac) | JEMMA Home Automation Core services | 3.1.4 |
| [ismb/jemma.osgi.ah.hac.lib](https://github.com/ismb/jemma.osgi.ah.hac.lib) | JEMMA Home Automation Core libraries | 3.1.5 |
| [ismb/jemma.osgi.ah.hap.client](https://github.com/ismb/jemma.osgi.ah.hap.client) | A service adding a reliable layer to transfer data from the platform to the provisioning server | 1.2.16 |
| [ismb/jemma.osgi.ah.io](https://github.com/ismb/jemma.osgi.ah.io) | This bundles implements a state machine to properly handle FlexGateway's leds | 1.0.17 |
| [ismb/jemma.osgi.ah.m2m.device](https://github.com/ismb/jemma.osgi.ah.m2m.device) | Exposes stateless XML Rest API to send data and to perform queries. | 1.2.16 |
| [ismb/jemma.osgi.ah.upnp.energyathome](https://github.com/ismb/jemma.osgi.ah.upnp.energyathome) | This bundle allows UPnP discovery. | 1.0.2 |
| [ismb/jemma.osgi.ah.webui.base](https://github.com/ismb/jemma.osgi.ah.webui.base) | This bundles base common utilities to the web interfaces. | 2.0.13 |
| [ismb/jemma.osgi.ah.webui.energyathome](https://github.com/ismb/jemma.osgi.ah.webui.energyathome) | This bundle provides a web interface to configure appliances and the entire system. | 1.0.61 |
| [ismb/jemma.osgi.ah.webui.energyathome.demo](https://github.com/ismb/jemma.osgi.ah.webui.energyathome.demo) | This bundle provides a web interface to configure appliances and the entire system. | 1.0.46 |
| [ismb/jemma.osgi.ah.zigbee](https://github.com/ismb/jemma.osgi.ah.zigbee) | ZigBee Network Manager | 2.1.27 |
| [ismb/jemma.osgi.ah.zigbee.appliances](https://github.com/ismb/jemma.osgi.ah.zigbee.appliances) | This bundle is a generic driver for ZigBee Home Automation devices used by the driver locator when a device is discovered to support all its clusters. | 1.0.3 |
| [ismb/jemma.osgi.ah.zigbee.appliances.generic](https://github.com/ismb/jemma.osgi.ah.zigbee.appliances.generic) | This bundle is a generic driver for ZigBee Home Automation devices used by the driver locator when a device is discovered to support all its clusters. | 1.0.3 |
| [ismb/jemma.osgi.javagal](https://github.com/ismb/jemma.osgi.javagal) | Java-based ZigBee Gateway Abstraction Layer Implementation | 2.0.9 |
| [ismb/jemma.osgi.javagal.gui](https://github.com/ismb/jemma.osgi.javagal.gui) | JavaGAL Web GUI | 1.0.6 |
| [ismb/jemma.osgi.javagal.json](https://github.com/ismb/jemma.osgi.javagal.json) | JavaGAL JSON interfaces | 1.0.2 |
| [ismb/jemma.osgi.javagal.rest](https://github.com/ismb/jemma.osgi.javagal.rest) | JavaGAL REST interfaces | 1.0.1 |
| [ismb/jemma.osgi.utils](https://github.com/ismb/jemma.osgi.utils) | JEMMA utils for OSGi environemnt | 1.0.7 |
| [ismb/jemma-maven-repository](https://github.com/ismb/jemma-maven-repository) | Support project used to store binary dependencies for JEMMA. | 1.0.0 |




</div>

Note: the table reflects the current status of components. 
To ease code maintanance, targeting milestone v1.0.0, we are currently externalizing several bundles which are still in the main ismb/jemma project as independent project.





