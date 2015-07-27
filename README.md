# JEMMA - Java Energy ManageMent Application framework

**IMPORTANT NOTE:**: Refactoring in progress, please refer to [0.9.3 tag] (https://github.com/ismb/jemma/tree/0.9.3) while the process is being completed

JEMMA is a open-source (LGPL) framework which implements the [Energy@home specifications](http://www.energy-home.it/Documents/Technical%20Specifications/E@H_specification_ver0.95.pdf) for energy monitoring and management application. JEMMA currently supports the [ZigBee Home Automation 1.2](http://www.zigbee.org/Standards/ZigBeeHomeAutomation/Overview.aspx) and the [ZigBee Gateway Device](http://www.zigbee.org/Standards/ZigbeeTelecomServices/Features.aspx) standards resulting from of a collaboration between the [Energy@home Association](http://www.energy-home.it/) and the [ZigBee Alliance](http://www.zigbee.org/).

More information are available on the [JEMMA Website](http://jemma.energy-home.org) and the [JEMMA GitHub page](https://github.com/ismb/jemma).

## Full component list

JEMMA is a modular framework. To better handle development of each module, bundle developments are spread in a number of separate GitHub projects.
The following table summarizes the current official JEMMA bundles.

| Project | Description | Version |
| :------:|:------------|:-----:|
| [ismb/jemma](https://github.com/ismb/jemma) | The core set of JEMMA components | 1.0.0-rc0 |
| [ismb/jemma.osgi.dal](https://github.com/ismb/jemma.osgi.dal) | JEMMA adapter for Device Abstraction Layer | 1.1.0 |
| [ismb/it.ismb.pert.osgi.dal](https://github.com/ismb/it.ismb.pert.osgi.dal) | OSGi Device Abstraction Layer Implementation | 1.0.0 |
| [ismb/it.ismb.pert.osgi.dal.functions](https://github.com/ismb/it.ismb.pert.osgi.dal.functions) | OSGi Device Abstraction Layer Generic Functions | 1.0.0 |
| [ismb/it.ismb.pert.osgi.dal.functions.eh](https://github.com/ismb/it.ismb.pert.osgi.dal.functions.eh) | OSGi Device Abstraction Layer Energy@Home-specific Functions | 1.0.0 |
| [ismb/it.ismb.pert.osgi.dal.web-apis](https://github.com/ismb/it.ismb.pert.osgi.dal.web-apis) | REST and WebSocket access to Devices, Functions and Events available in OSGi Device Abstraction Layer | 2.0.0 |
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



## Get Started

Tutorials, how-tos and other resources are available on [the project wiki-based documentation](https://github.com/ismb/jemma/wiki/Developers-Documentation).

JEMMA can also be controlled through REST/WebSocket APIs relying on OSGi Device Abstraction Layer. To start interacting with it you need also these bundles:

* [OSGi Device Abstraction Layer](https://github.com/ismb/it.ismb.pert.osgi.dal)
* [OSGi Device Abstraction Layer functions](https://github.com/ismb/it.ismb.pert.osgi.dal.functions) 
* [Energy@Home specific OSGi Device Abstraction Layer functions](https://github.com/ismb/it.ismb.pert.osgi.dal.functions.eh) 
* [The DAL adapter for JEMMA](https://github.com/ismb/jemma.osgi.dal): it registers DAL services for JEMMA devices
* [The DAL web APIs](https://github.com/ismb/it.ismb.pert.osgi.dal.web-apis)

Take a look at [this wiki page](https://github.com/ismb/jemma/wiki/JEMMA-DAL-APIs-functions) to discover functionalities that can be exposed by JEMMA devices

## License

The JEMMA code-base has been developed since 2010 by [Telecom Italia](http://www.telecomitalia.it/) which holds the Copyright on the original code base.

The full JEMMA source code, unless specified otherwise in specific files, have been released under the GNU Lesser General Public License (**LGPL**) version 3. LGPL conditions can be found on the [GNU website](https://www.gnu.org/licenses/lgpl.html).

## Contribute

Willing to contribute ? Check our [project tracker](https://github.com/ismb/jemma/issues).
Information on how to contribute can be found [here](https://github.com/ismb/jemma/wiki/Contribute)

## Linked projects


- [ZCL cluster generator](https://github.com/nport/jemma.ah.zigbee.zcl.compiler/) - A code generator which creates jemma zcl proxy classes from xml descriptions.
- [Simple command provider](https://github.com/ivangrimaldi/jemma.osgi.commandprovider) - A simple OSGi CommandProvider demonstrating JEMMA device APIs usage
