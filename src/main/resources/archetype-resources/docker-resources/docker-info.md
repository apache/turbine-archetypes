# Installation

- If not yet done, run build
```
$ mvn clean install -Pdocker
```

Change into docker resource folder
``` 
cd docker-resources
``` 

Optional Cleanup
``` 
docker-compose down
docker-compose down -v

docker volume rm $(docker volume ls -qf dangling=true)
docker system prune
``` 

Build Services
``` 
 docker-compose build --no-cache --build-arg DB_CONTEXT=./docker-resources/db
 
 // build only app
 // docker-compose build --no-cache app
 // docker-compose build --no-cache --build-arg DB_CONTEXT=./docker-resources/db db
 
``` 
DB_CONTEXT is set to allow starting the db container standalone (in folder db, e.g. with docker build --tag my-db .) to test it.

Builds db, populates it with (hard coded data and prepares application.

Start all services
``` 
docker-compose up

// docker-compose up db
``` 
Jetty is run and exposes the webapp to http://localhost:80801/app

#Test Services: App, Database

## Start service

```
docker-compose run --rm db /bin/sh --build-arg DB_CONTEXT=./docker-resources/db
```
Extract data in db-Service
```
 // check mysql in service container db
 // # mysql -u root -h db -P 3306 -p
 // docker-compose exec db mysql -u root --password=... -e "show databases;" --build-arg DB_CONTEXT=./docker-resources/db
 // docker-compose exec db sh -c 'exec mysqldump --all-databases -uroot -p...' --build-arg DB_CONTEXT=./docker-resources/db > dump.sql

```
- Service app
```
docker-compose up app 
docker-compose run --rm app /bin/sh 
// ls -la /myapp // should list pom.xml ...
```

# Windows

## Powershell

- Replace backslashes to slashes in docker-compose.yml in localRepository

- check COMPOSE_CONVERT_WINDOWS_PATHS, https://docs.docker.com/compose/reference/envvars/#compose_convert_windows_paths


- If image download fails, try Docker->Network->Update DNS to 8.8.8.8

- ERROR: for db  Cannot start service db: driver failed programming external connectivity on endpoint docker-resources_d

  - -> https://github.com/docker/for-win/issues/573 Best answer:

	1. docker rm $(docker ps -a -q)
	2. Stop the Docker on your machine & restart it.

## Windows Subsystem for Linux (WSL)

- Check file permissions of archetype generated files (chmod -R a+rw docker-resources, chmod -R a+rw src .

- https://nickjanetakis.com/blog/setting-up-docker-for-windows-and-wsl-to-work-flawlessly



Resetting / Preparation (optional)

```  
  $ docker-compose rm -v
```  

```

Delete all images
$ docker rmi $(docker images -q)
```
  
Check if something is there ..
```
  docker-compose ps
``` 

first build, this requires /generated-sql :
 ```
 $ mvn clean package
 $ docker-compose build
```
or
$ docker-compose build --no-cache
 
Then run
```
$ docker-compose up
```

Internals

```
  docker-machine ls
  docker ps  
  docker-compose ps
  docker-compose up --build
  
  delete intermediate / show images
  docker rmi $(docker images --filter "dangling=true" -q)
  # or delete while building
  docker build --rm
  # cleans all containers
  docker system prune
  
  # stops all running containers  
  docker stop $(docker ps -a -q)
```  
