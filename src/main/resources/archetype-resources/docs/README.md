#*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.
*#
 

# Quick Guide to using the new Turbine 5.1 maven archetype for skeleton application generation


## About this archetype 

Turbine Version: Turbine 5.1.

Follow either Local database Setup or Docker Setup

### Local database Setup

First, you should have a local database installed and configured prior to 
beginning the application setup.

As we are using MySQL by default you need to create the database in MySQL (server version should be at least 5.5, because of new sql driver), e.g. with

mysql -u <user> -p
mysql> create database helloWorld;
mysql> \q

or other tools. The database should have been started and the database user granted enough rights.


### Docker integration (short intro)

Run this command

    mvn clean install -Pdocker

    
to immediately enable docker setup, when generating the archetype. 

Currently only port 3306 is supported, if you do not want ot change the port seetings for the db container in docker-compose.yml

You then should change to the generated docker resources: 

    cd <project>/target/docker-resources
    
and follow the instructions in [DOCKER README] (docs/DOCKER-README.md). In short you may do the following (you may have to start docker desktop on windows):
 
    docker compose configurable
    docker compose build --no-cache
    docker compose up
    
 - First time Building might take a couple of minutes. 
    
 - Now you can launch your new Turbine application by default [http://localhost:8081/app] 
 

- Read [DOCKER README] (DOCKER-README.md).
 
You then need not to follow the following chapters, but could immediately go to 

    cd <project>/target/docker-resources
    
and then follow the instructions in DOCKER-README.md. In short you may do the following:
 
    docker compose configurable
    docker compose build --no-cache
    docker compose up
    
 - Now you can launch your new Turbine application by default [http://localhost:8081/app] 
 

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
- running mysql

If running from integration test, check/update
- in pom.xml turbine.core property,
- target/test-classes/projects/first/project/integrationtest/src/test/conf/torque/TorqueTest.properties or
  META-INF/maven/archetype-metadata.xml

The security test is by default skipped as it requires a running mysql. It tests many of the Fulcrum Torque Turbine security aspects, 
activate it by calling

```sh
mvn test -DskipTests=false
```

CAVEAT: If initialization fails, double check your database credentials! If invalid the error might be somewhat hidden behind a
 Torque exception!

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

