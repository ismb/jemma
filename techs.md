Technologies
<!-- Remember: the first line always goes with the title-->
<!-- Please use h3 headers (###) inside these files -->



### Home Devices

JEMMA currently supports the <a href="http://www.zigbee.org/Standards/ZigBeeHomeAutomation/Overview.aspx" target="_parent">ZigBee Home Automation 1.2</a> and the <a href="http://www.zigbee.org/Standards/ZigbeeTelecomServices/Features.aspx" target="_parent">ZigBee Gateway Device</a> standards resulting from of a collaboration between the <a href="http://www.energy-home.it/" target="_parent">Energy@home Association</a> and the <a href="http://www.zigbee.org/" target="_parent">ZigBee Alliance</a>.

### Software Architecture

The JEMMA software architecture is based on <a href="http://www.osgi.org/" target="_parent">OSGi</a>. 

3rd-party applications can be plugged into JEMMA either locally (via OSGi declarative services) or on the Cloud, through its web-based RESTful APIs.

### Toolchain

JEMMA uses a *pom-first* approach and leverages <a href="http://maven.apache.org/" target="_parent">Apache Maven</a> to handle dependencies, automate builds, etc.

 
###Java-GAL

The Java-GAL is an OSGi based ZigBee Gateway Abstraction Layer written in Java language. It implements the *ZigBee Gateway Device* specifications.
Java-GAL is a core component for any OSGi and REST Home Automation application that requires to operate with a ZigBee hardware. 

With a properly configured Java-GAL, the client developers don't need to be aware of the model of ZigBee hardware that will be used because the main feature of the Java-GAL is to translate the ZigBee product dependant low level APIs into a common J-GAL interface. Moreover, Java-GAL adds all the required features to perform active nodes discovery in order to keep a constantly updated image of the current ZigBee network. 
The structure of Java-GAL source code, allows the easy development of the required software modules for new Zigbee hardware interfaces, relying on a proper internal low-level interface.

The Java-GAL is a project copyright of Telecom Italia under the financial support of EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030), the author of this software is Marco Nieddu of Consoft Sistemi. 

**Key Features:**

- Implemented in Java. JDK 1.5
- Equinox OSGi framework compliant
- Based on standard Zigbee Gateway Device specification
- Modular structure for different hardware support; the Freescale MC13226V ZigBeePRO – USB module is actually developed
- J-GAL interface for OSGi client packages 
- REST interface for IP client applications
- Discovery ad Freshness procedures for guarantee the consistency of the managed ZigBee network.
- Suitable for ZigBee Coordinator, Router and End Device HA1.2 client applications

