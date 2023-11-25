# Agamis Fusion / Fusion File System

Fusion is a network appliance designed to serve as data layer for OGDT Ecosystem.
It is based on :
- JVM runtime
- Scala language
- sbt compiler
- Apache Pekko framework
- Apache Ignite Datagrid
- MongoDB

# Installation

## Installation for local development

Clone this repo and navigate to its root directory.

You should put it in a Scala/sbt environment for compiling jvm binaries.

Apache Ignite configuration is shipped with GridGain control-center-agent v2.9.0.1.

### Using your own Control Center instance

If you want to use it with your own control-center, you need to enter shell of your ignite container and use following in Ignite root directory (default is : ```/opt/ignite/apache-ignite```) :
```
./management.sh --uri http://<control-center-frontend-host>:<control-center-frontend-port>
```

It will give you a new token in container logs which you can use in your control center to add cluster

## Usage

Download latest release in releases page.

This software is made to be used with Apache Ignite : A docker configuration can be found for setting up Apache Ignite instance.

This software can be deployed both in a distributed way or as a monolithic application.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

Refer to [CONTRIBUTING.md](CONTRIBUTING.md) for detailed ways of contributing.

## License
This software is released under [Apache-2.0](https://choosealicense.com/licenses/apache-2.0/)

See [LICENSE](LICENSE) and [COPYRIGHT](COPYRIGHT)

## Used softwares
Apache Ignite : https://ignite.apache.org/

MongoDB : https://www.mongodb.com/

Docker : https://www.docker.com/