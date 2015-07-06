# JEMMA - Java Energy ManageMent Application framework

JEMMA is a open-source (LGPL) framework which implements the [Energy@home specifications](http://www.energy-home.it/Documents/Technical%20Specifications/E@H_specification_ver0.95.pdf) for energy monitoring and management application. JEMMA currently supports the [ZigBee Home Automation 1.2](http://www.zigbee.org/Standards/ZigBeeHomeAutomation/Overview.aspx) and the [ZigBee Gateway Device](http://www.zigbee.org/Standards/ZigbeeTelecomServices/Features.aspx) standards resulting from of a collaboration between the [Energy@home Association](http://www.energy-home.it/) and the [ZigBee Alliance](http://www.zigbee.org/).

More information are available on the [JEMMA Website](http://jemma.energy-home.org) and the [JEMMA GitHub page](https://github.com/ismb/jemma).

## Full component list

JEMMA is a modular framework. To better handle development of each module, bundle developments are spread in a number of separate GitHub projects.
The following table summarizes the current official JEMMA bundles.

| Project | Description | | :------:|:------------|
| [ismb/jemma](https://github.com/ismb/jemma) | The core set of JEMMA components|
| [ismb/it.ismb.pert.osgi.dal](https://github.com/ismb/it.ismb.pert.osgi.dal) | OSGi Device Abstraction Layer Implementation|
| [ismb/it.ismb.pert.osgi.dal.functions](https://github.com/ismb/it.ismb.pert.osgi.dal.functions) | OSGi Device Abstraction Layer Generic Functions|
| [ismb/it.ismb.pert.osgi.dal.functions.eh](https://github.com/ismb/it.ismb.pert.osgi.dal.functions.eh) | OSGi Device Abstraction Layer Energy@Home-specific Functions|
| [ismb/jemma.osgi.dal](https://github.com/ismb/jemma.osgi.dal) | JEMMA adapter for Device Abstraction Layer|
| [ismb/it.ismb.pert.osgi.dal.web-apis](https://github.com/ismb/it.ismb.pert.osgi.dal.web-apis) | REST and WebSocket access to Devices, Functions and Events available in OSGi Device Abstraction Layer|
| [ismb/jemma.osgi.javagal](https://github.com/ismb/jemma.osgi.javagal) | Java-based ZigBee Gateway Abstraction Layer Implementation|
| [ismb/jemma-maven-repository](https://github.com/ismb/jemma-maven-repository) | Support project used to store binary dependencies for JEMMA.|
| [ismb/jemma.osgi.ah.greenathome](https://github.com/ismb/jemma.osgi.ah.greenathome) | Greenathome Appliance|
| [ismb/jemma.osgi.ah.felix.console.web](https://github.com/ismb/jemma.osgi.ah.felix.console.web) | Apache web console plugin for Jemma|
| [ismb/jemma.osgi.ah.demo.fakevalues](https://github.com/ismb/jemma.osgi.ah.demo.fakevalues) | Fake devices for JEMMA testing/tutorials|
| [ismb/jemma.osgi.ah.energyathome](https://github.com/ismb/jemma.osgi.ah.energyathome) | Energy@Home Home Automation bundle|
| [ismb/jemma.osgi.ah.configurator](https://github.com/ismb/jemma.osgi.ah.configurator) | jemma.osgi.ah.configurator|
| [ismb/jemma.osgi.ah.adapter.http](https://github.com/ismb/jemma.osgi.ah.adapter.http) | JEMMA HTTP AH Adapter|

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
