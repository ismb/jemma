# JEMMA - Java Energy ManageMent Application framework

JEMMA is a open-source (LGPL) framework which implements the [Energy@home specifications](http://www.energy-home.it/Documents/Technical%20Specifications/E@H_specification_ver0.95.pdf) for energy monitoring and management application. JEMMA currently supports the [ZigBee Home Automation 1.2](http://www.zigbee.org/Standards/ZigBeeHomeAutomation/Overview.aspx) and the [ZigBee Gateway Device](http://www.zigbee.org/Standards/ZigbeeTelecomServices/Features.aspx) standards resulting from of a collaboration between the [Energy@home Association](http://www.energy-home.it/) and the [ZigBee Alliance](http://www.zigbee.org/).

More information are available on the [JEMMA Website](http://jemma.energy-home.org) and the [JEMMA GitHub page](https://github.com/ismb/jemma).

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
