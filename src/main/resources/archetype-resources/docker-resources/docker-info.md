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
docker system prune
docker volume rm $(docker volume ls -qf dangling=true)
``` 

Build Services
``` 
 docker-compose build --no-cache --build-arg DB_CONTEXT=./docker-resources/db
``` 
DB_CONTEXT is set to allow starting the db container standalone (in folder db, e.g. with docker build --tag my-db .) to test it.

Builds db, populates it with (hard coded data and prepares application.

Start all services
``` 
docker-compose up
``` 
Jetty is run and exposes the webapp to http://localhost:80801/app


#Test Database

## start service

```
docker-compose up db
docker-compose run db /bin/sh --build-arg DB_CONTEXT=./docker-resources/db
```
Extract data
```
 docker-compose exec db mysql -u root --password=... -e "show databases;" --build-arg DB_CONTEXT=./docker-resources/db
 docker-compose exec db sh -c 'exec mysqldump --all-databases -uroot -p...' --build-arg DB_CONTEXT=./docker-resources/db > dump.sql
 // check mysql in service container db
# mysql -u root -h db -P 3306 -p

```

# Windows

- Replace backslashes to slashes in docker-compose.yml in localRepository

- check COMPOSE_CONVERT_WINDOWS_PATHS, https://docs.docker.com/compose/reference/envvars/#compose_convert_windows_paths


- If image download fails, try Docker->Network->Update DNS to 8.8.8.8

- ERROR: for db  Cannot start service db: driver failed programming external connectivity on endpoint docker-resources_d

  - -> https://github.com/docker/for-win/issues/573 Best answer:

	1. docker rm $(docker ps -a -q)
	2. Stop the Docker on your machine & restart it.


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
