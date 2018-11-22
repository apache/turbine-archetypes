#using multi-stage

# mariadb use docker-entrypoint 
FROM mysql:latest  as builder

# That file does the DB initialization but also runs mysql daemon, by removing the last line it will only init
RUN ["/bin/sed", "-i", "s/exec \"$@\"/echo \"not running $@\"/", "/usr/local/bin/docker-entrypoint.sh"]

#
# check where and what configurations are read with e.g.
# docker-compose run db /usr/sbin/mysqld --verbose --help | grep -A 1 "Default options"
# docker run <image> /usr/sbin/mysqld --verbose --help | grep -A 1 "Default options"

ARG DB_CONTEXT
# mount in docker-compose
ADD #var("DB_CONTEXT:-.")/mysql/conf/my.cnf /etc/

# for logs
RUN mkdir /mysql
RUN chmod a+w /mysql

# for sql
RUN mkdir /data
RUN chmod a+w /data

# copy 
ADD #var("DB_CONTEXT:-.")/mysql/init/*.sql /docker-entrypoint-initdb.d/

# to check what is needed
ADD ./target/generated-sql/torque/mysql/*.sql ./data/
ADD ./docs/sample-mysql-data/*.sql ./data/

# required, to create
ENV DB_CONTEXT #var("DB_CONTEXT")

ENV MYSQL_DATABASE=${turbine_database_name}
ENV MYSQL_ROOT=root
ENV MYSQL_ROOT_PASSWORD=default
ENV MYSQL_USER=${turbine_database_user}
ENV MYSQL_PASSWORD=${turbine_database_password}
ENV MYSQL_HOST=
#ENV MYSQL_RANDOM_ROOT_PASSWORD yes

# Need to change the datadir to something else that /var/lib/mysql because the parent docker file defines it as a volume.
# https://docs.docker.com/engine/reference/builder/#volume :
#       Changing the volume from within the Dockerfile: If any build steps change the data within the volume after
#       it has been declared, those changes will be discarded.

RUN ["/usr/local/bin/docker-entrypoint.sh", "mysqld", "--datadir", "/initialized-db"]
#RUN /docker-entrypoint.sh mysqld --datadir=/initialized-db && sleep 10 

FROM mysql:latest

# my.cnf setting data-dir 
COPY --from=builder /initialized-db /var/lib/mysql


