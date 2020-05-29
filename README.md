CDMI QoS plugin for EOS Storage Backend
=======================================

Description
-----------

This is a plugin for the [INDIGO-DataCloud CDMI Server][1].
The plugin exposes the QoS interface of the EOS storage endpoint
to the CDMI-Server and allows QoS interactions on EOS entries
using the CDMI protocol.

The plugin facilitates two main interactions:
  - discovery of QoS capabilities
  - manipulation of QoS properties of a given entry

Build and install
-----------------

### Requirements

- JDK 1.8+
- Maven 3.1+

### Dependencies

- [INDIGO-DataCloud CDMI SPI][2]

### Compilation

Make sure to enable the following Maven repository: http://cdmi-qos.data.kit.edu/maven/

To compile the `cdmi-eos-qos` plugin, execute the following:

```
$ git clone https://github.com/cern-eos/cdmi-eos-qos.git
$ cd cdmi-eos-qos
$ mvn install
```

The final product will be the `cdmi-eos-qos-<version>.jar` artifact.

### Installation

The plugin must be used as a component for the INDIGO-DataCloud CDMI Server.
To enable this, add **cdmi-eos-qos** as a dependency to the server's `pom.xml` file 
and repackage the server 

```xml
<dependency>
    <groupId>org.dcache.spi</groupId>
    <artifactId>cdmi-eos-qos</artifactId>
    <version>1.0</version>
</dependency>
```

### Configuration

The plugin will try to load the following properties:
- eos.server
- eos.server.port
- eos.server.scheme

Make sure you provide these values either via a `config/eos.config` file accessible by the CDMI server 
or via command line. 

Interaction
-----------

- Query available CDMI capabilities for a given entry type

```
davix-http -X GET -H 'Authorization: "Bearer: $OIDC"' -H 'Accept: "application/cdmi-capability"' \
    https://cdmi-server.cern.ch:8443/cdmi_capabilities/dataobject/
```

- Query description of a given CDMI capability

```
davix-http -X GET -H 'Authorization: "Bearer: $OIDC"' -H 'Accept: "application/cdmi-capability"' \
    https://cdmi-server.cern.ch:8443/cdmi_capabilities/dataobject/disk_replica
```

- Get CDMI capability of an entry

```
davix-http -X GET -H 'Authorization: "Bearer: $OIDC"' -H 'Accept: "application/cdmi-object"' \
    https://cdmi-server.cern.ch:8443/<file-path>
```

- Set CDMI capability of an entry

```
davix-http -X PUT -H 'Authorization: "Bearer: $OIDC"' -H 'Content-Type: "application/cdmi-object"' \
    --data '{"capabilitiesURI": "/cdmi_capabilities/dataobject/<capability-type>"}' \
    https://cdmi-server.cern.ch:8443/<file-path>
```

[1]: https://github.com/indigo-dc/CDMI
[2]: https://github.com/indigo-dc/cdmi-spi
