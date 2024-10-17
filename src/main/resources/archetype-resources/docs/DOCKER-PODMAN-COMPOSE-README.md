# Introduction

This is to help developers to get fast a running development environment for debugging.

To use it in production you may need to carefully review all configurations and adjust.

The Container based environment is to test/develop a Turbine app using a container based test database. 

The build should take place outside the container.

It is based on one docker-compose.yml file and two Dockerfiles and could be run with podman-compose (after some checks are done, see below)..

Docker compose uses currently two customized services: **app** (maven:latest) and **db** (mariadb:10.10). 

## Podman und Podman-compose

- If using Podman (podman-compose) you have to add registry docker.io/library/ in etc/containers/registries.conf (or add a namespace before tag name in FROM of DOCKERFILE).
- To allow inter container communication you should enable in /etc/containers/container.conf  
 
    [network]
    network_backend = "netavark"
    
- In docker-compose.yml networks has to be enabled because of this.

 (tested for Debian bookworm, podman 4.3.1, running in root-less environment). You could check, that it is set by running 

    podman info | grep network
    
. And just replace below docker compose with podman-compose (most commands)!

## Note

- reference database is now Mariadb (mySQL may be used also after some adaptions in DOCKERFILE, but it is not tested in the first place). 
- Instead of using maven as the app service a Jetty container might be the better choice as currently console reloading might not work. 
To be able to use other maven tasks (in the container) in this case makes this nevertheless a reasonable choice.

# Prepare

To run the build from the archetypes root use the following mvn command:

    mvn install -Pdocker,mysql 
or
    mvn install -Pdocker,mariadb
    
IMPORTANT: You habe to enable the profile mariadb or mysql to define the backend.
    
### Build Note 

If you have already generated this with mvn archetype:generate within the same host environment this step could be omitted.

# Installation (running the app)
    
- Change into directory target/docker-resources and check the file docker-compose.yml, e.g. with

    cd <project>/target/docker-resources
    docker compose config

    
Important: Check that  /m2repo is properly mapped to your local maven repository in docker-compose.yml!

### Note if building from Archetypes Repo source  - Integration Test

This generates in target folder a structure like this:

    projects/first/project/integrationtest
    
    
If running from integrationtest, you find the docker files in integrationtest/target/docker-resources.

- check, do a fresh build and start the services

    docker compose config
    docker compose ps
    // optional docker compose down -v
    docker compose build --no-cache
    docker compose up --detach
    
or 

   podman-compose down
   podman-compose up -d
   podman-compose start 
    
** A first time build of the app service should not take more than a couple of minutes. **    
    
You might check the process with 

    podman-compose logs -f app
    docker compose logs -f db
    
The logs should show "mysqld: ready for connections" and "Started Jetty Server".

 - Now you can launch (in another terminal) your new Turbine application by default [http://localhost:8081/app] (http://localhost:8081/app).
 - Login with user / password or admin /password.
 
### Troubleshooting Notes

You may use the command *docker compose* or *docker-compose*, but will slightly different results.

- Double check database service call in ** target/<projectname>/WEB-INF/jetty-env.xml**. It should reference the service name (db), not localhost - this was set when using the maven docker profile.


```xml
<Set name="url">jdbc:mysql://db:3306/turbine</Set>
```

#### Build Services Details

The app service uses later a volume, which maps to the local maven repository, which you may need/not need.
The db service uses mysql-latest (currently 8.x), you may replace it with a fixed release tag.

Check the mysql uid/gid with

    docker run -it --rm --user mysql:mysql docker-resources_db id

If previously build, you may want to delete all volumes (this will delete all tables in /var/lib/mysql monted in volume db_data_<project>) and containers

    docker-compose down -v
   

- Build it

    docker-compose build --no-cache

 .. optionally build it separately
    docker-compose build --no-cache --build-arg DB_CONTEXT=./docker-resources/db db
    
 .. building app service first/only requires removing the dependency in docker-compose.yml(comment depends_on: db)
    docker-compose build --no-cache app

DB_CONTEXT is set to allow starting the db container standalone (in folder db, e.g. with docker build --tag my-db .)
to test it.  CAVEAT: The db service is build and populated until now with hard coded data. 
It is a dependency for the service app (see app/Dockerfile).


#### Starting Services Details

Start both services in one step (add -d for detached mode)

    docker-compose up
   
.. or doing it in background, requires to start the services explicitely

    docker-compose up -d
    docker-compose start

This will start first the db service, then the app service. Jetty is run exposing the webapp to **http://localhost:8081/app**.
By default remote debugging is activated (port 9000), which could be removed/commented in docker-compose.yml.
You could follow the logs with docker-compose logs -f app or docker-compose logs -f db.


- To change velocity templates check webapp in ** src/main/webapp** and run in another window *mvn package*.  Jetty should restart automatically. Other resources might depend on https://www.eclipse.org/jetty/documentation/jetty-9/index.html#jars-scanned-for-annotations.

#### Cleanup or restart (optional)

- Optionally Check system or cleanup, e.g. with docker-compose down, docker-compose down -v or docker sytem prune (removes any container on system).

- If services are already installed, activate/start by 
    docker-compose up
    
##### Example Logs for Docker:
  
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
    
##### Podman Example Results


    'podman', '--version', '']
    using podman version: 4.3.1
    ** excluding:  set()
    ['podman', 'inspect', '-t', 'image', '-f', '{{.Id}}', 'docker-resources_db']
    ['podman', 'inspect', '-t', 'image', '-f', '{{.Id}}', 'docker-resources_app']
    podman volume inspect docker-resources_db_data_turbine || podman volume create docker-resources_db_data_turbine
    ['podman', 'volume', 'inspect', 'docker-resources_db_data_turbine']
    ['podman', 'network', 'exists', 'docker-resources_back-tier']
    podman run --name=docker-resources_db_1 -d --label io.podman.compose.config-hash=123 --label io.podman.compose.project=docker-resources --label io.podman.compose.version=0.0.1 --label com.docker.compose.project=docker-resources --label com.docker.compose.project.working_dir=..pes/target/test-classes/projects/first/project/integrationtest/target/docker-resources --label com.docker.compose.project.config_files=docker-compose.yml --label com.docker.compose.container-number=1 --label com.docker.compose.service=db -v ..chetypes/target/test-classes/projects/first/project/integrationtest/target/docker-resources/db/mysql/data:/data -v docker-resources_db_data_turbine:/var/lib/mysql:rw --net docker-resources_back-tier --network-alias db -p 13306:3306 docker-resources_db mysqld --default-authentication-plugin=mysql_native_password
    a7a1baf00a9ba0ed9fae5727fe015b21350fcd90c92ea6676fed1f32e9387ceb
    exit code: 0
    ['podman', 'network', 'exists', 'docker-resources_back-tier']
    podman run --name=docker-resources_app_1 -d --label io.podman.compose.config-hash=123 --label io.podman.compose.project=docker-resources --label io.podman.compose.version=0.0.1 --label com.docker.compose.project=docker-resources --label com.docker.compose.project.working_dir=./target/test-classes/projects/first/project/integrationtest/target/docker-resources --label com.docker.compose.project.config_files=docker-compose.yml --label com.docker.compose.container-number=1 --label com.docker.compose.service=app -e MAVEN_OPTS= -v ...archetypes/target/test-classes/projects/first/project/integrationtest:/myapp -v /home/../m2Repo/repository:/m2repo --net docker-resources_back-tier --network-alias app -p 8081:8081 -p 9000:9000 --restart always docker-resources_app mvn jetty:run -Pdocker,mariadb
    4e9e9701de26a7033cd8c100184317ab63a57e477fa0a8ee3937a851ca0c3503
    exit code: 0


- Starting the app service will build the app and start jetty on port 8081. 
This command is set as a **command** in the app service in docker compose. 

Currently the docker-compose is generated once more, if starting the containers, this will overwrite m2repo and may result in errors.

If not yet done, build on the host with mvn clean install -f ../pom.xml -Pdocker,mariadb.

### Lifecycle (developers only)

- If you generate the archetype itself, you might have to stop the services before cleaning up (docker-compose stop).
    
# Tests

The security test is skipped by default as it requires a running mysql. It tests many of the Fulcrum Torque Turbine security aspects, 
you may activate it by calling in the root of the container in the shell (e.g. with docker-compose run --rm app /bin/sh):

    mvn test -DskipTests=false

- You may reset MAVEN_OPTS, which is by default set in settings.xml

    export MAVEN_OPTS=
    

## Prerequisites

- a running docker service db

- Check/update proper settings of Db connection URL in

    <project-path>/src/test/conf/torque/TorqueTest.properties 

 If you have not initialized docker, when creating the archetype instance, 
 you may have to edit TorqueTest.properties and adapt the url from localhost to db. 
 
 
 ### Inside the container 
 
If running tests inside container, URL setting in TorqueTest.properties should be:

    torque.dsfactory.turbine.connection.url = 
    jdbc:mysql://db:3306/turbine?serverTimeZone=UTC
    
Then run in target/docker-resources a docker compose run command:

    docker compose run --rm app /bin/sh 
    #mvn test -DskipTests=false    
    
Of course, if running inside the container, you should exit and you might have to restart the app service.

 ### In the host system 
 
On the other side, if running the tests outside the container
the setting should be :

    torque.dsfactory.turbine.connection.url = 
    jdbc:mysql://localhost:13306/turbine?serverTimeZone=UTC

This depends of course on the db ports settings in docker-compose.yml.

Run in project root

    mvn test -DskipTests=false
 
## Debugging Services: Db, App

### Db Service 

 
    podman-compose run --rm db /bin/bash 

Extract data in db service (check password in docker-compose.yml or elsewhere).

    mysql -u root -h db -P 3306 -p
    
.. or 

    docker-compose exec db mysql -u root --password=... -e "show databases;" --build-arg DB_CONTEXT=./docker-resources/db
    docker-compose exec db sh -c 'exec mysqldump --all-databases -uroot -p...' --build-arg DB_CONTEXT=./docker-resources/db > dump.sql

### App Service

This will start app (and db as app depends on db):

    docker-compose run app /bin/sh 

In the container, check:

    ls -la /myapp // should list pom.xml ...
 
# System Specific Informations

## Windows

- Start Docker Desktop, you might not get an error immediately if not, but only when shared volumes are created (like 

    Error response from daemon: error while creating mount source path '/run/desktop/mnt/host/wsl/docker-desktop-bind-mounts/..': mkdir /run/desktop/mnt/host/wsl/docker-desktop-bind-mounts/.. file exists

### Powershell

- You may have to replace in volume mapping for host repo path (maven localRepository) backslashes with slashes "/" in docker-compose.yml or better rerun the maven build.

- Check COMPOSE_CONVERT_WINDOWS_PATHS, https://docs.docker.com/compose/reference/envvars/#compose_convert_windows_paths

- If an image download fails, try Docker->Network->Update DNS to 8.8.8.8

- ERROR, when starting docker-compose up/start:

    ERROR: for docker-resources_db_1  Cannot start service db: driver failed programming external connectivity on endpoint docker-resources_db_1 ... 
    Error starting userland proxy: mkdir /port/tcp:0.0.0.0:13306:tcp:...:3306: input/output error"

  - Check if containers are not already running.
  - Remove containers (if any): docker rm $(docker ps -a -q)
  - Windows (including WSL subsystem): If the error is still there, restart Docker Desktop on your machine.

### Windows Subsystem for Linux (WSL)

- Check file permissions of archetype generated files (chmod -R a+rw docker-resources, chmod -R a+rw src).

- If you generated the project with windows shell, but run the docker form wsl you have to regenerate docker-compose.yml with unix pathes running this command again

    mvn install -Pdocker,mariab


### More Internals, Helpful Docker commands

If you want to run from Dockerfile ..

#### Resetting / Preparation (optional)

    docker-compose rm -v

#### Delete all images

    docker rmi $(docker images -q)

  
## License

This project is licensed under the Apache Software License 2.0
