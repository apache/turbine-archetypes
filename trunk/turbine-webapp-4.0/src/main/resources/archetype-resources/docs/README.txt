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
 
Turbine Version: Turbine 4.0-M2. Be aware and
  - keep groups/roles lower case
  - abstract classes and managers are included
  (because of some minor bugs in Fulcrum Security 1.1.0, which should be fixed in v 1.1.1/Turbine 4.0)
  - LogoutAction is included (fixed getUserFromSession)
  - om stub classes are included (until configurable in schema with Torque version 2.1)
  

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
    -Dturbine_database_name=helloWorld


Note that the database URL will be appended with your database name
in the final pom.xml, so you do not need to specify that in 
the configuration.

Next, you need to create the database in MySQL

 mysql -u <user> -p
 mysql> create database helloWorld;
 mysql> \q


cd helloWorld

mvn generate-sources  ## This will generate the OM layer and SQL 
					  ## code for creating the corresponding
					  ## database tables
					  
mvn sql:execute       ## This executes the SQL code to create 
					  ## the application schema defined 
					  ## in src/main/torque-schema

You should now insert the sample data file provided as Torque 4.0 
has disabled the datasql task.

mvn jetty:run         ## Now you can launch your new Turbine application

Open a web browser to http://localhost:8081/app

Login should work with user admin/password.

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
