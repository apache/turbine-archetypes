# Introduction

This is to help developers to get fast a running development environment for debugging.

To use it in production you may need to carefully review all configurations and adjust.

This Docker environment is to test/develop a Turbine app using a docker test database. 

The build should take place outside the docker container.

It is based on one docker-compose.yml file and two Dockerfiles.

Docker compose uses currently two services: **app** (maven:3-jdk-8) and **db** (mysql:latest). 

## Note
- Mysql should easily be replacable by Mariadb. 
- Instead of using maven as the app service a Jetty container might be the better choice as currently console reloading might not work.

# Prepare

To run the build with maven do this outside of the container using following mvn command:

    mvn install -Pdocker

    
- Then check in directory  target/docker-resources the file docker-compose.yml e.g. with

    docker compose config

## Optional Integration Test (not tested)

    mvn integration-test 

N.B.: This builds the integrationtest project in target/test-classes/projects/first/project/integrationtest with docker enabled configuration. 
Running the build inside the container is not required and may be problematic, unless you use only public available dependencies.


# Installation (running the app)

- Change into the projects target/docker resource folder

```sh
cd  target/docker-resources
``` 

## Check Docker Compose file (optional)

- Check services in docker-compose.yml (volumes) 
``` 
docker-compose config
```

N.B. You may use the command *docker compose* or *docker-compose*, but will slightly different results.

- Check database service call in ** target/<projectname>/WEB-INF/jetty-env.xml**. It should reference the service name (db), not localhost - as it is also set in maven docker profile.


```xml
<Set name="url">jdbc:mysql://db:3306/turbine</Set>
```


- To change velocity templates check webapp in ** src/main/webapp**.  Ohter resources might depend on https://www.eclipse.org/jetty/documentation/jetty-9/index.html#jars-scanned-for-annotations.

## Cleanup or restart (optional)

- Optionally Check system or cleanup, e.g. with docker-compose down, docker-compose down -v or docker sytem prune (removes any container on system).

- If services are already installed, activate/start by 
    docker-compose up
    
 Example Logs:
  
    [Note] [Entrypoint]: Entrypoint script for MySQL Server 8.0.25-1debian10 started.
    [Note] [Entrypoint]: Switching to dedicated user 'mysql'
    [Note] [Entrypoint]: Entrypoint script for MySQL Server 8.0.25-1debian10 started.

    [System] [MY-010116] [Server] /usr/sbin/mysqld (mysqld 8.0.25) starting as process 1
    [System] [MY-013576] [InnoDB] InnoDB initialization has started.
    [System] [MY-013577] [InnoDB] InnoDB initialization has ended.
     [System] [MY-011323] [Server] X Plugin ready for connections. Bind-address: '::' port: 33060, socket: /var/run/mysqld/mysqlx.sock
    [Warning] [MY-010068] [Server] CA certificate ca.pem is self signed.
    [System] [MY-013602] [Server] Channel mysql_main configured to support TLS. Encrypted connections are now supported for this channel.
    [Warning] [MY-011810] [Server] Insecure configuration for --pid-file: Location '/var/run/mysqld' in the path is accessible to all OS users. Consider choosing a different directory.
    [System] [MY-010931] [Server] /usr/sbin/mysqld: ready for connections. Version: '8.0.25'  socket: '/var/run/mysqld/mysqld.sock'  port: 3306  MySQL Community Server - GPL.
    
    Listening for transport dt_socket at address: 9000

    [INFO] Scanning for projects...
    [INFO]
    [INFO] ------< org.apache.turbine.test.integrationtest:integrationtest >-------
    [INFO] Building My Turbine Web Application 1.0.0-SNAPSHOT
    [INFO] --------------------------------[ war ]---------------------------------
    [INFO]
    [INFO] >>> jetty-maven-plugin:9.4.43.v20210629:run (default-cli) > test-compile @ integrationtest >>>
    [INFO]
    [INFO] --- torque-maven-plugin:5.1-SNAPSHOT:generate (torque-om) @ integrationtest ---
    [main] INFO | org.apache.torque.generator.control.Controller - readConfiguration() : Starting to read configuration files


- Starting the app service will build the app and start jetty with Maven on port 8081. 
This command is set as a **command** in the app service in docker compose. 

Currently the docker-compose is generated once more, if starting the containers, this will overwrite m2repo and may result in errors.

If not yet done, build on the host with mvn clean install -f ../pom.xml -Pdocker.

### Build Services

The app service uses later a volume, which maps to the local maven repository, which you may need/not need.
The db service uses mysql-latest (currently 8.x), you may replace it with a fixed release tag.

If previously build, you may want to delete all volumes and containers

    docker-compose down -v

 - Build it
 
 
```sh
    docker-compose build --no-cache
```

 .. optionally build it separately
    docker-compose build --no-cache --build-arg DB_CONTEXT=./docker-resources/db db
    
 .. building app service first/only requires removing the dependency in docker-compose.yml(comment depends_on: db)
    docker-compose build --no-cache app

DB_CONTEXT is set to allow starting the db container standalone (in folder db, e.g. with docker build --tag my-db .)
to test it.  CAVEAT: The db service is build and populated until now with hard coded data. 
It is a dependency for the service app (see app/Dockerfile).


### Starting Services

Start both services in one step
``` 
docker-compose up
```    
.. or doing it in background, requires second start command
``` 
docker-compose -d up
docker-compose start
``` 

This will start first the db service, then the app service. Jetty is run exposing the webapp to **http://localhost:8081/app**.
By default remote debugging is activated (port 9000), which could be removed/commented in docker-compose.yml.
You could follow the logs with docker-compose logs -f app or docker-compose logs -f db.

#### Lifecycle (developers only)

- If you generate the archetype itself, you might have to stop the services before cleaning up (docker-compose stop).

## Debugging Services: Db, App

### Db Service 
``` 
docker-compose run --rm db /bin/sh --build-arg DB_CONTEXT=./docker-resources/db
``` 
Extract data in db service

```sh
 mysql -u root -h db -P 3306 -p
```
.. or 

    docker-compose exec db mysql -u root --password=... -e "show databases;" --build-arg DB_CONTEXT=./docker-resources/db
    docker-compose exec db sh -c 'exec mysqldump --all-databases -uroot -p...' --build-arg DB_CONTEXT=./docker-resources/db > dump.sql

### App Service

This will start app and db (as it depends on app):
```sh
docker-compose run --rm app /bin/sh 
``` 
In the container, check:
```sh
ls -la /myapp // should list pom.xml ...
```

# System Specific Informations

## Windows

### Powershell

- Use Powershell

- Replace in volume mapping for host repo path (maven localRepository) backslashes with slashes "/" in docker-compose.yml.

- check COMPOSE_CONVERT_WINDOWS_PATHS, https://docs.docker.com/compose/reference/envvars/#compose_convert_windows_paths

- If a image download fails, try Docker->Network->Update DNS to 8.8.8.8

- ERROR, when starting docker-compose up/start:
 "for db  Cannot start service db: driver failed programming external connectivity on endpoint docker-resources_db_1 or
ERROR: for docker-resources_db_1  Cannot start service db: driver failed programming external connectivity on endpoint docker-resources_db_1 ... 
Error starting userland proxy: mkdir /port/tcp:0.0.0.0:13306:tcp:...:3306: input/output error"

  - Check if containers not already running.
  - Remove containers (if any): docker rm $(docker ps -a -q)
  - If error still there, restart Docker Desktop on your machine.

### Windows Subsystem for Linux (WSL)

- Check file permissions of archetype generated files (chmod -R a+rw docker-resources, chmod -R a+rw src .

- https://nickjanetakis.com/blog/setting-up-docker-for-windows-and-wsl-to-work-flawlessly


## More Internals, Helpful Docker commands

### Resetting / Preparation (optional)

    docker-compose rm -v

### Delete all images

    docker rmi $(docker images -q)

### Still more docker commands ...

  docker ps  
 
  // delete intermediate images, volumes
  docker rmi $(docker images --filter "dangling=true" -q)
  docker volume rm $(docker volume ls -qf dangling=true)
  
  # or delete while building
  docker build --rm
  
  # cleans all containers
  docker system prune
  
  # stops all running containers  
  docker stop $(docker ps -a -q)