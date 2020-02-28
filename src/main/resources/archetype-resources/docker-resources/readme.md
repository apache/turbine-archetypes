# Introduction

This is to help developers to get fast a running development environment for debugging.

This Docker environment is to test/develop a Turbine app using a docker test database. 

The build should take place outside the docker container.

It is based on one docker-compose.yml file and two Dockerfiles.

Docker compose uses two services: **app** (maven:3-jdk-8) and **db** (mysql:latest). 

## Note
- Mysql should easily be replacable by Mariadb. 
- Instead of using maven as the app service a Jetty container might be the better choice as currently console reloading might not work.

# Prepare

To run the build with maven do this outside of the container using following mvn command:

```
mvn clean install -Pdocker
```

N.B.: This builds the integrationtest project in target/test-classes/projects/first/project/integrationtest with docker enabled configuration. 
Running the build inside the container is not required and may be problematic, unless you use only public available dependencies.


# Installation (running the app)

- Change into the projects docker resource folder

```sh
cd docker-resources
``` 

## Check Docker Compose file (optional)

- Check services in docker-compose.yml (volumes). You might map your local maven repostory like this:

```sh
  volumes:
    - ../:/myapp
    - '<localpath>:/m2repo'
 ```yml

- Check format:

``` 
docker-compose config
```
- Check database service call in **src/main/webapp/WEB-INF/jetty-env.xml**. It should reference the service name (db), not localhost - as it is also set in maven docker profile.

```xml
<Set name="url">jdbc:mysql://db:3306/turbine</Set>
```

## Cleanup or restart (optional)

- Optionally Check system or cleanup, e.g. with docker-compose down, docker-compose down -v or docker sytem prune (removes any container on system).

## Start

- If services are already installed, activate/start by 

```sh
    docker-compose up
```

- Starting the app service will build the app and start jetty with Maven on port 8081. 
This command is set as a **command** in the app service in docker compose. 

If not yet done, build on the host with 

```sh
mvn clean install -f ../pom.xml -Pdocker
```

### Start and Stopp the service

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

Login should work with user admin/password or user/password.


By default remote debugging is activated (port 9000), which could be removed/commented in docker-compose.yml.

You could follow the logs  (in folder docker-resources) with 

``` sh
docker-compose logs -f app
``` 

 or 
 
``` sh
 docker-compose logs -f db.
 ``` 

Stop by Ctrl-C or
``` 
docker-compose down
``` 


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

### Build Services

The app service uses later a volume, which maps to the local maven repository, which you may need/not need.
The db service uses mysql (currently 8.0.x), you may replace it with a fixed release tag, e.g. 5.6. 

In any case, you need then to rebuild the image.


 - Build the image 
 
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
