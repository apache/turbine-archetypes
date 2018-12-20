FROM maven:3-jdk-8 

# cft hub.docker.com/r/library/maven
ADD ./docker-resources/app/settings.xml /usr/share/maven/ref/settings.xml

# Set our working directory inside the image

ADD pom.xml /myapp/pom.xml
ADD src/ /myapp/src/

WORKDIR /myapp

# build app outside docker, if you use snapshots
# no build stage volumes: https://github.com/moby/moby/issues/38366
# RUN mvn -f /myapp/pom.xml clean package
