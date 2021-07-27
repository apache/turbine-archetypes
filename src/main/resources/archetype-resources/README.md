# Quick Guide to using the new Turbine 5.1 maven archetype for skeleton application generation

## About this archetype 

Turbine Version: Turbine 5.1.

You may skip Docker and Local Database Setup and read it later.

### Docker Setup

- If docker is available you could use the Docker build setup. Follow instructions here: [DOCKER README] (docs/DOCKER-README.md).

#### Local Database Setup

First, you should have a local database installed and configured prior to 
beginning the application setup below.

- Find more information about Logs and application structur here: [README] (docs/README.md).


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
    
to immediately enable docker setup, when generating the archetype. 

Currently only port 3306 is supported, if you do not want ot change the port seetings for the db container in docker-compose.yml

### Development

You may use 

```sh
mvn archetype:generate -DarchetypeCatalog=local
```

to avoid declaring the *archetype* variables.

This requires you provide a local catalog in $HOME\.m2\archetype-catalog.xml. Find further information here: https://maven.apache.org/archetype/archetype-models/archetype-catalog/archetype-catalog.html.

##### Example
<archetype-catalog ...>
 <archetype>
      <groupId>org.apache.turbine</groupId>
      <artifactId>turbine-webapp-5.1</artifactId>
      <version>2.0.0-SNAPSHOT</version>
      <description>This archetype sets up a web application project based on Apache Turbine 5.0</description>
    </archetype>
  </archetypes>
</archetype-catalog>

### Notes (local Database Setup only)

When invoking archetype:generate like above, you already have set Turbine goals generate-sources,integration-test 
and you can then skip them later.

Be aware, when you set both mvn commands goals (which are maven phases actually), i.e  with

-Dgoals=generate-sources, integration-test 

you have to create the database (see above) before finishing the (interactive) archetype commands. 
Otherwise you could catch up doing this later and after that is done calling the phases afterwards as mentioned below.

#### turbine_database_url

Note that the database URL (turbine_database_url) 
will be appended with your database name
in the final pom.xml, so you do not need to specify that in 
the configuration. Be sure that it ends with a slash ('/')!

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

- Find more information about Logs and application structur here: [README] (docs/README.md).
