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

Notice

About this archetype 
 
Turbine Version: Turbine 4.0-M2. 

Quick Guide to using the new Turbine 4.0-M2 maven archetype 
for skeleton application generation

You should have a local database installed and configured prior to 
beginning the application setup below.


You can invoke the Maven archetype for turbine-webapp-4.0 from 
the command line as shown below - please update values starting 
with 'groupId' as appropriate.

mvn archetype:generate \
    -DarchetypeGroupId=org.apache.turbine \
    -DarchetypeArtifactId=turbine-webapp-4.0 \
    -DarchetypeVersion=1.0.1-SNAPSHOT \
    -DgroupId=com.mycompany.webapp \
    -DartifactId=helloWorld \
    -Dversion=1.0 \
    -Dturbine_app_name=HelloWorld \
    -Dturbine_database_adapter=mysql \
    -Dturbine_database_user=db_username \
    -Dturbine_database_password=db_password \
    -Dturbine_database_name=helloWorld \
    -Dturbine_database_url=jdbc:mysql://localhost:3306/ \
    -Dgoals=generate-sources,sql:execute

Notes

When invoking archetype:generate, you can already set mvn commands and you can then skip them later.
Be aware, when you set both mvn commands goals (-Dgoals=generate-sources, sql:execute), 
you have to create the database (see below) BEFORE finishing the (interactive) archetype commands.

Note that the database URL (turbine_database_url) 
will be appended with your database name
in the final pom.xml, so you do not need to specify that in 
the configuration.

Next, you need to create the database in MySQL

 mysql -u <user> -p
 mysql> create database helloWorld;
 mysql> \q


cd helloWorld

# if not already done 
mvn generate-sources  ## This will generate the OM layer and SQL 
					  ## code for creating the corresponding
					  ## database tables
	
# if not already done   
mvn sql:execute       ## This executes the SQL code to create 
					  ## the application schema defined 
					  ## in src/main/torque-schema

You should now check the database tables and if some data is missing
insert the sample data file provided 
as Torque 4.0 has disabled the datasql task.

mvn jetty:run         ## Now you can launch your new Turbine application

Open a web browser to http://localhost:8081/app

Login should work with user admin/password or user/password.

Background:

By default Intake is used as an validation mechanism for authentication. You can change to the default login by settings

action.login=LoginUser in TurbineResources.properties and changing Login.vm appropriately (commented form)

To enable application development in Eclipse, run the following command 
and then import the project into Eclipse.

mvn eclipse:eclipse

Once imported, update your project to be managed by Maven 
  -> Right click on the proejct name
  -> Configure 
  -> Convert to Maven project

To test the application can be deployed by Eclipse, select the run
configuration "Run On Server" if you have a container configured with
your eclipse environment.

Starting developing

Be aware of settings and some smaller restrictions, which mostly will be fixed in the upcoming releases.

- Keep groups/roles lower case (which should be fixed in Fulcrum Security 1.1.1/Turbine 4.0)
- abstract classes and managers are included (because of some minor bugs in Fulcrum Security 1.1.0, same as above)
- LogoutUser action is included (fix in Turbine 4.0, getUserFromSession)
- LoginUser action is included (to check for anonymous user, may be fixed in future release)
- OM (Torque Object Mapper) stub classes are included (until configurable in schema with Torque version 2.1)
- TurbineConfiguration returns a Commons configuration object, even if field is not assignable (will be fixed in Turbine 4.0, you can then assign e.g. to String instead, cft. SecureScreen)
- Database connection is done initially by default with JNDI. If you want to change it, check Torque.properties and jetty-env.xml.  

