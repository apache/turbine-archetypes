# turbine-webapp-5.x

Maven Archetype to generate a webapp utilizing Turbine 5.x

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

* Java 8 JDK or later 
* [MySQL](https://www.mysql.com/) - Database Server or [Docker](https://docs.docker.com/get-docker/)
* [Maven](https://maven.apache.org/) - Dependency Management

You should have Java 8 or later installed.  The archetype sets up a new application using MySQL as the default database store.  However, you can adjust this to use any database supported by Apache Torque 5.x. If not using Docker, you should therefore be at least have a database instance where you have access rights to create a new database schema and populate it with the tables the application generates.  Finally, this is a maven archetype, so of course you should install a local version of Maven (tested with 3.5.4 and 3.8.1). 


### Installing

Eclipse integration:

You need to first add the Apache maven archetypes to your IDE so that you can take advantage of using the turbine-webapp-5.x plugin to generate a new web application.

The location of the remote catalog file is: http://repo.maven.apache.org/maven2/archetype-catalog.xml 

Step-by-step instructions on how to accomplish this can be found here: https://howtodoinjava.com/eclipse/how-to-import-maven-remote-archetype-catalogs-in-eclipse/

Follow further instructions here: [ARCHETYPE-README](src/main/resources/archetype-resources/docs/README.md) and/or  [ARCHETYPE-DOCKER-README](src/main/resources/archetype-resources/docs/DOCKER-README.md).


#### Development

Clone this repository and build it with maven. 


## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Contributing

Please feel free to contribute. We are always happy to encourage new committers to the project. 

The Apache Software foundation requires that any contributor has signed the ICLA (Individual Contributor License Agreement).

Find an overview, more information and how-tos [here](http://www.apache.org/licenses/contributor-agreements.html#clas).

## Authors

* **Georg Kallidis** 
* **Jeffery Painter** -  [Jivecast](https://jivecast.com)

## License

This project is licensed under the Apache Software License 2.0

## Acknowledgments

* Thanks to the long list of former Apache Turbine developers