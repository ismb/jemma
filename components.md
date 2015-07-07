Components
<!-- Remember: the first line always goes with the title-->
<!-- Please use h3 headers (###) inside these files -->

Starting from version 0.9, JEMMA is maintaned as a set of OSGi modules/projects, as listed in the following table:

<div class="jemmahwtablesfather">

| Project | Description | Version |
| :------:|:------------|:-----:|
| [ismb/jemma](https://github.com/ismb/jemma) | The core set of JEMMA components | 0.9.3 |
| [ismb/it.ismb.pert.osgi.dal](https://github.com/ismb/it.ismb.pert.osgi.dal) | OSGi Device Abstraction Layer Implementation | 1.0.0 |
| [ismb/it.ismb.pert.osgi.dal.functions](https://github.com/ismb/it.ismb.pert.osgi.dal.functions) | OSGi Device Abstraction Layer Generic Functions | 1.0.0 |
| [ismb/it.ismb.pert.osgi.dal.functions.eh](https://github.com/ismb/it.ismb.pert.osgi.dal.functions.eh) | OSGi Device Abstraction Layer Energy@Home-specific Functions | 1.0.0 |
| [ismb/jemma.osgi.dal](https://github.com/ismb/jemma.osgi.dal) | JEMMA adapter for Device Abstraction Layer | 1.1.0 |
| [ismb/it.ismb.pert.osgi.dal.web-apis](https://github.com/ismb/it.ismb.pert.osgi.dal.web-apis) | REST and WebSocket access to Devices, Functions and Events available in OSGi Device Abstraction Layer | 2.0.0 |
| [ismb/jemma.osgi.javagal](https://github.com/ismb/jemma.osgi.javagal) | Java-based ZigBee Gateway Abstraction Layer Implementation | 2.0.7 |
| [ismb/jemma-maven-repository](https://github.com/ismb/jemma-maven-repository) | Support project used to store binary dependencies for JEMMA. | 1.0.0 |
| [ismb/jemma.osgi.ah.greenathome](https://github.com/ismb/jemma.osgi.ah.greenathome) | Greenathome Appliance | 2.1.20 |
| [ismb/jemma.osgi.ah.felix.console.web](https://github.com/ismb/jemma.osgi.ah.felix.console.web) | Apache web console plugin for Jemma | 1.0.9 |
| [ismb/jemma.osgi.ah.demo.fakevalues](https://github.com/ismb/jemma.osgi.ah.demo.fakevalues) | Fake devices for JEMMA testing/tutorials | 0.0 |
| [ismb/jemma.osgi.ah.energyathome](https://github.com/ismb/jemma.osgi.ah.energyathome) | Energy@Home Home Automation bundle | 0.1.1 |
| [ismb/jemma.osgi.ah.configurator](https://github.com/ismb/jemma.osgi.ah.configurator) | jemma.osgi.ah.configurator | 1.0.7 |
| [ismb/jemma.osgi.ah.adapter.http](https://github.com/ismb/jemma.osgi.ah.adapter.http) | JEMMA HTTP AH Adapter | 2.0.3 |

</div>

Note: the table reflects the current status of components. 
To ease code maintanance, targeting milestone v1.0.0, we are currently externalizing several bundles which are still in the main ismb/jemma project as independent project.





