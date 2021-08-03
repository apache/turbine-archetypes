# Quick Guide to using the new Turbine 5.x maven archetype for skeleton application generation

Maven Archetype to generate a webapp utilizing Turbine 5.x

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

* Java 8 JDK or later 
* [MySQL](https://www.mysql.com/) - Database Server or [Docker] (https://docs.docker.com/get-docker/)
* [Maven](https://maven.apache.org/) - Dependency Management

You should have Java 8 or later installed.  The archetype sets up a new application using MySQL as the default database store.  However, you can adjust this to use any database supported by Apache Torque 5.x. If not using Docker, you should therefore be at least have a database instance where you have access rights to create a new database schema and populate it with the tables the application generates.  Finally, this is a maven archetype, so of course you should install a local version of Maven (tested with 3.5.4 and 3.8.1).  

## About this archetype 

Turbine Version: Turbine 5.1 Turbine-webapp-5.x

### Integration Test (if you checked out this [Archetype Generate Repository](https://github.com/apache/turbine-archetypes.git))

    mvn integration-test 

N.B.: This builds an integrationtest project in target/test-classes/projects/first/project/integrationtest (if you provide -Ddocker=true with docker enabled configuration, otherwise you need a hosted mysql database). You could start testing this archetype here..

- Follow either Local database Setup or Docker Setup

### Docker Setup

- If docker is available you could use the Docker build setup. Follow instructions here: [DOCKER README] (DOCKER-README.md).

### Local Database Setup

First, you should have a local database installed and configured prior to 
beginning the application setup below.

As we are using MySQL by default you need to create the database in MySQL (server version should be at least 5.5, because of new sql driver), e.g. with

```sh
mysql -u <user> -p
mysql> create database helloWorld;
mysql> \q
```

or other tools. The database should have been started and the database user granted enough rights.

### Maven archetype 

Next, you can invoke the Maven archetype for turbine-webapp-5.0 from 
the command line as shown below - please update values starting 
with 'groupId' as appropriate.

```sh
mvn archetype:generate \
  -DarchetypeGroupId=org.apache.turbine \
  -DarchetypeArtifactId=turbine-webapp-5.1 \
  -DarchetypeVersion=2.0.0-SNAPSHOT \
  -DgroupId=com.mycompany.webapp \
  -DartifactId=myhelloworld \
  -Dversion=1.0 \
  -Dturbine_app_name=HelloWorld \
  -Dturbine_database_adapter=mysql \
  -Dturbine_database_user=db_username \
  -Dturbine_database_password=db_password \
  -Dturbine_database_name=helloworld \
  -Dturbine_database_timezone=UTC \
  -Dturbine_database_url=jdbc:mysql://localhost:3306/ \
  -Dgoals=generate-sources,integration-test
```

#### Docker integration

N.B. Set docker variable to true to enable Docker setup in building the artifact: 

    -Ddocker=true
    
to immediately enable docker setup, when generating the archetype. If 

Currently only port 3306 is supported, if you do not want ot change the port seetings for the db container in docker-compose.yml

### Development

You may use 

```sh
mvn archetype:generate -DarchetypeCatalog=local
```

to avoid declaring the *archetype* variables.

This requires you provide a local catalog in $HOME\.m2\archetype-catalog.xml. Find further information here: https://maven.apache.org/archetype/archetype-models/archetype-catalog/archetype-catalog.html.

##### Example

```xml
<archetype-catalog ...>
 <archetype>
      <groupId>org.apache.turbine</groupId>
      <artifactId>turbine-webapp-5.1</artifactId>
      <version>2.0.0-SNAPSHOT</version>
      <description>This archetype sets up a web application project based on Apache Turbine 5.x</description>
    </archetype>
  </archetypes>
</archetype-catalog>
```

## Project Start and Usage

Next, change into the newly generated project folder, in our case

```sh
cd myhelloworld
```

Skip next two steps, if the build was successfull.

```sh
mvn generate-sources
```
This will generate the OM layer and SQL code for creating the corresponding database tables.


```sh
mvn integration-test
```
This executes the SQL code to create the application schema defined  in src/main/torque-schema.

You should now check the database tables and if some data is missing
insert the sample data file in sample-mysql-data (Torque 4.0 has disabled the datasql task).

```sh
mvn clean install 
```

If you get an error like *"The driver has not received any packets"* probably the database is not up and running or the port may be another one.

Last step on the command line is run the server by invoking 
mvn -Pjetty

- Now you can launch your new Turbine application by default [http://localhost:8081/app] (check port in pom.xml, if needed).

### Logs 

Find the Logs in src/main/webapp/logs and

### Application

Open a web browser to [http://localhost:8081/app]

Login should work with user admin/password or user/password.

## Background

By default Intake is used as an validation mechanism for authentication. You can change to the default login by setting

action.login=LoginUser in TurbineResources.properties and changing Login.vm appropriately (commented form)

## Tests

Prerequisites
- ignored tests require at least Turbine version 4.0.1 
- database was build successfully e.g. with archetype.
- running mysql or docker service db

If running from integration test, check/update
- in pom.xml turbine.core property,
- target/test-classes/projects/first/project/integrationtest/src/test/conf/torque/TorqueTest.properties or
  META-INF/maven/archetype-metadata.xml

The security test is by default skipped as it requires a running mysql. It tests many of the Fulcrum Torque Turbine security aspects, you may activate it by calling

```sh
mvn test -DskipTests=false
```

CAVEAT: If initialization fails, double check your database credentials! If invalid the error might be somewhat hidden behind a
 Torque exception! Docker: If you have not initialized docker when creating the archetype instance, you have to edit TorqueTest.properties and adapt the url from localhost to db. You may then run the tests from inside the container service app.

## IDE Integration, Eclipse

To enable application development in Eclipse, run the following command 
and then import the project into Eclipse.

```sh
mvn eclipse:eclipse
```

Once imported, update your project to be managed by Maven 
-> Right click on the project name
-> Configure 
-> Convert to Maven project

To test the application can be deployed by Eclipse, select the run
configuration "Run On Server" if you have a container configured with
your eclipse environment.

### Eclipse Debugging

You even could debug the app by setting the environment variable to something like this

```
set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n
```

before running the jetty server.

## Starting developing

Be aware of settings and some smaller restrictions, which mostly will be fixed in the upcoming releases.

- Keep groups/roles lower case (which should be fixed in current Fulcrum Security 2.x/Turbine 5.x)
- abstract classes and managers are included (because of some minor bugs in Fulcrum Security 1.1.0, same as above)
- LogoutUser action is included as an custom example 
- LoginUser action is included (to check for anonymous user, may be fixed in future release)
- OM (Torque Object Mapper) stub classes are included (until configurable in schema with Torque version 2.1)
- TurbineConfiguration returns a Commons configuration object, even if field is not assignable (fixed in Turbine 5.x, you can then assign e.g. to String instead, cft. SecureScreen)
- Database connection is done initially by default with JNDI. If you want to change it, check Torque.properties and (1) for Tomcat, META-INF/context.xml or (2) for Jetty, WEB-INF/jetty-env.xml.  

## License

This project is licensed under the Apache Software License 2.0

