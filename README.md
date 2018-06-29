# Apache Turbine Archetypes,  Turbine 4.0 maven archetype

About this archetype

* This is a maven archetype to generate a skeleton Turbine application.

* The base Turbine version is **[Turbine 4.0](http://turbine.apache.org/turbine/turbine-4.0/)**.

## Quick Guide: How to use the new Turbine 4.0 maven archetype for skeleton application generation

### Maven archetype 

#### Requirements

- Java 6+ up to version Turbine 4.0.x, cft. [Getting Started with Turbine 4.0](http://turbine.apache.org/turbine/turbine-4.0/getting-started.html).
- Database (Mysql 5.x like)

#### Installation

##### First step: Catalog setting

First, find the remote arcehtype or generate a local one.

###### Remote catalog
```
mvn archetype:generate
```
Find **turbine-webapp-4.0** in remote archetype catalog and select current version.

###### Local catalog

Checkout Git master/trunk and install into locale repository
```
git clone https://gitbox.apache.org/repos/asf/turbine-archetypes.git
cd turbine-archetypes
mvn install
```
Outlook: After the snapshot archetype is installed and the database is set up (see below), just add **-DarchetypeCatalog=local** as argument (see below), when generating the Turbine application.
```
mvn archetype:generate -DarchetypeCatalog=local -Dturbine_database_name=db_name ...
```

### Second step: Local database Setup

Second, you should have a local database installed and configured prior to beginning the application setup below.

As we are (currently) using MySQL by default you need to create the database in MySQL, e.g. with
```
mysql -u <privileged_user> -p
mysql> create database <db_name>;
mysql> \q
```
or other tools. The database should have been started and the database user granted enough rights.

### Third step: Customization

Next, you can invoke the Maven archetype for turbine-webapp-4.0 from 
the command line as shown below - please update values starting with 'groupId' as appropriate.
```
mvn archetype:generate \
  -DarchetypeGroupId=org.apache.turbine \
  -DarchetypeArtifactId=turbine-webapp-4.0 \
  -DarchetypeVersion=1.0.2-SNAPSHOT \
  -DgroupId=com.mycompany.webapp \
  -DartifactId=myhelloworld \
  -Dversion=1.0 \
  -Dturbine_app_name=HelloWorld \
  -Dturbine_database_adapter=mysql \
  -Dturbine_database_user=db_username \
  -Dturbine_database_password=db_password \
  -Dturbine_database_name=db_name \
  -Dturbine_database_url=jdbc:mysql://localhost:3306/ \
  -Dgoals=generate-sources,pre-integration-test
```
#### Notes

When invoking *archetype:generate* like above, you already have set Turbine *Maven goals* **generate-sources**, **pre-integration-test** 
and you can then skip them later.

**Be aware, when you set both mvn commands goals (which are maven phases actually), i.e  with**
```
-Dgoals=generate-sources, pre-integration-test 
```

 **you have to create the database (see above) before finishing the (interactive) archetype commands.**
Otherwise you could catch up doing this later (cft. below, Project Start, which requires the database setup is finished).

##### turbine_database_url

Note that the database URL (*-Dturbine_database_url=<turbine_database_url>*) 
will be appended with your database name (*-Dturbine_database_name=db_name*) in the final pom.xml, so you do not need to specify that in 
the configuration.

### Last Step: Project Start and Usage

Next, change into the newly generated project folder, in our case
```
cd myhelloworld
```
Skip next two steps, if the build was successfull (requires database setup is finished)
```
mvn generate-sources
```
This will generate the OM layer and SQL code for creating the corresponding database tables.

```
mvn integration-test
```
This executes the SQL code to create the application schema defined in *src/main/torque-schema*.

You should now check the database tables and if some data is missing
insert the sample data file in **sample-mysql-data** (Torque 4.0 has disabled the datasql task).
```
mvn clean install 
```
If you get an error like **_"The driver has not received any packets"_** probably the database is not up and running or the port may be another one.

Last step on the command line is run the server by invoking 
```
mvn jetty:run
```
Now you can launch your new Turbine application! Have fun!

#### Logs 

Find the Logs in *src/main/webapp/logs*.

#### Application

Open a web browser to *http://localhost:8081/app*

Hint: find the port in the configuration of the jetty-maven-plugin [Archetype pom template](src/main/resources/archetype-resources/pom.xml).

Login should work with user admin/password or user/password.

### Background

By default [Intake](http://turbine.apache.org/fulcrum/fulcrum-intake/ "Fulcrum Intake") is used as an validation mechanism for authentication. You can change to the default login by setting

*action.login=LoginUser* in **TurbineResources.properties** and changing *Login.vm* appropriately (commented form).

### Tests

Prerequisites
- Ignored tests require at least Turbine version 4.0.1 
- Database was build successfully e.g. with archetype.
- Running mysql

If running from integration test, check/update
- in **pom.xml** _turbine.core_ property,
- **target/test-classes/projects/first/project/integrationtest/src/test/conf/torque/TorqueTest.properties** or
  **META-INF/maven/archetype-metadata.xml**

The security test is by default skipped as it requires a running mysql. It tests many of the [Fulcrum Torque Turbine security aspects](http://turbine.apache.org/fulcrum/fulcrum-security/), 
activate it by calling
```
mvn test -DskipTests=false
```
CAVEAT: If initialization fails, double check your database credentials! If invalid the error might be somewhat hidden behind a
 Torque exception!

### Eclipse

To enable application development in Eclipse, run the following command 
and then import the project into Eclipse.
```
mvn eclipse:eclipse
```
Once imported, update your project to be managed by Maven 
-> Right click on the project name
-> Configure 
-> Convert to Maven project

To test the application can be deployed by Eclipse, select the run
configuration "Run On Server" if you have a container configured with
your eclipse environment.

#### Eclipse Debugging

You could debug the app by setting the environment variable to something like this
```
set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n
```
before running the jetty server.

### Starting developing

Be aware of settings and some smaller restrictions, which mostly will be fixed in the upcoming releases.

- Keep groups/roles lower case (which should be fixed in Fulcrum Security 1.1.1/Turbine 4.0)
- abstract classes and managers are included (because of some minor bugs in Fulcrum Security 1.1.0, same as above)
- LogoutUser action is included (fix in Turbine 4.0, getUserFromSession)
- LoginUser action is included (to check for anonymous user, may be fixed in future release)
- OM (Torque Object Mapper) stub classes are included (until configurable in schema with Torque version 2.1)
- TurbineConfiguration returns a Commons configuration object, even if field is not assignable (will be fixed in Turbine 4.0, you can then assign e.g. to String instead, cft. SecureScreen)
- Database connection is done initially by default with JNDI. If you want to change it, check Torque.properties and (1) for Tomcat, META-INF/context.xml or (2) for Jetty, WEB-INF/jetty-env.xml.  

## Docker

We are in the process t provide a Docker example, which will simplify the installation process, if properly done. Contact us!


## Changes
[Changes.xml](src/changes/changes.xml)

## License
[Apache License Version 2.0](LICENSE)

# General Issues

## Maven Hint (Windows)
If you get that goal integration-test of maven-archetype-plugin fails with 

"Cannot run additions goals." cft. https://issues.apache.org/jira/browse/ARCHETYPE-488.

Fastest workaround (Dez 2016) ist to copy  mvn.cmd to mvn.bat.

## More Information

[Archetype Blog Entry](https://blogs.apache.org/turbine/entry/maven_archetypes_for_apache_turbine)
