# Introduction

This docker environment is to test/develop a Turbine app using a docker test database. 

It is based on one docker-compose.yml file and two Dockerfiles.

Docker compose uses two services: **app** (maven:3-jdk-8) and **db** (mysql:latest). 

## Note
- Mysql should easily be replacable by Mariadb. 
- Instead of using maven as the app service a Jetty container might be the better choice as currently console reloading might not work.

# Installation

- Change into docker resource folder

```sh
cd docker-resources
``` 

## Check Docker Compose file (optional)

- Check services in docker-compose.yml (volumes) 
``` 
docker-compose config
```   
- Check **src/main/webapp/WEB-INF/jetty-env.xml**. It should reference the service name (db) not localhost.

```xml
<Set name="url">jdbc:mysql://db:3306/turbine</Set>
```

## Cleanup or restart (optional)

- Optionally Check system, Cleanup

    docker-compose ps
    docker-compose down
    docker-compose down -v

- If services are already installed, activate/start by 
    docker-compose up
  
- or remove by
    docker-compose down and docker-compose down -v

- Starting the app service will build the app and start jetty as a Maven install + Jetty start. 
This command is set as a **command** in the app service in docker compose.
The docker profile should be activated by default (if not add -Pdocker). 

Optionally test the build process on the host with mvn clean install -f ../pom.xml.

### Build Services

 - Set DB_CONTEXT=./docker-resources/db if not yet done for the db service on docker-compose.yml or provide as cli argument:

    docker-compose build --no-cache --build-arg DB_CONTEXT=./docker-resources/db
     .. or just build it
 
```sh
    docker-compose build --no-cache
```

 .. optionally build it separately
    docker-compose build --no-cache --build-arg DB_CONTEXT=./docker-resources/db db
    
 .. building app service first/only requires removing the dependency in docker-compose.yml(comment depends_on: db)
    docker-compose build --no-cache app

DB_CONTEXT is set to allow starting the db container standalone (in folder db, e.g. with docker build --tag my-db .)
to test it. 
CAVEAT: The db service is build and populated until now with hard coded data. 
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

This will start first the db service, then the app service. Jetty is run and exposes the webapp to **http://localhost:8081/app**.
By default remote debugging is activated (port 9000), which could be removed/commented in docker-compose.yml.
You could follow the logs with docker-compose logs -f app or docker-compose -logs -f db.

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

- Replace in volume path backslashes to slashes "/" in docker-compose.yml in localRepository

- check COMPOSE_CONVERT_WINDOWS_PATHS, https://docs.docker.com/compose/reference/envvars/#compose_convert_windows_paths

- If a image download fails, try Docker->Network->Update DNS to 8.8.8.8

- ERROR: for db  Cannot start service db: driver failed programming external connectivity on endpoint docker-resources_db_1 ..

  - Check if containers not already running.
  - Remove containers (if any): docker rm $(docker ps -a -q)
  - If error still there, restart Docker on your machine & restart it.

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
