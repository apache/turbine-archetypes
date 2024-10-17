# mariadb uses docker-entrypoint
# podman requires docker.io/library in registries.conf
FROM mariadb:10.10 

# That file does the DB initialization but also runs mysql daemon, by removing the last line it will only init
#RUN ["/bin/sed", "-i", "s/exec \"$@\"/echo \"not running $@\"/", "/usr/local/bin/docker-entrypoint.sh"]

#
# check where and what configurations are read with e.g.
# docker-compose run db /usr/sbin/mysqld --verbose --help | grep -A 1 "Default options"
# docker run <image> /usr/sbin/mysqld --verbose --help | grep -A 1 "Default options"

ARG DB_CONTEXT
ARG MARIADB_ROOT
ARG MARIADB_ROOT_PASSWORD
ARG MARIADB_DATABASE
ARG MARIADB_USER
ARG MARIADB_PASSWORD

# required, to create
ENV DB_CONTEXT ${DB_CONTEXT}
ENV MARIADB_ROOT=${MARIADB_ROOT}
ENV MARIADB_ROOT_PASSWORD=${MARIADB_ROOT_PASSWORD}

ENV MARIADB_DATABASE=${MARIADB_DATABASE}
ENV MARIADB_USER=${MARIADB_USER}
ENV MARIADB_PASSWORD=${MARIADB_PASSWORD}

COPY generated-sql/torque/mysql /generated-sql-init
# dependent sql should be name sorted after init.sql
# add what you need, avoid duplicate keys
RUN cat /generated-sql-init/*.sql > /docker-entrypoint-initdb.d/init.sql

COPY data/sample-mysql-data /generated-sql-post-init
RUN cat /generated-sql-post-init/*.sql > /docker-entrypoint-initdb.d/post_init.sql

# --chomd=600 --chown=mysql:mysql throws error
COPY ${DB_CONTEXT}/mysql/conf/ /etc/mysql/conf.d

USER mysql
