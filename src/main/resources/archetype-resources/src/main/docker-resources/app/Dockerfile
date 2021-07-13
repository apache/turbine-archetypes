FROM maven:3-jdk-8 

# cft hub.docker.com/r/library/maven
# not used, as in docker-compose m2repo is set
ADD ./docker-resources/app/settings.xml /usr/share/maven/ref/settings.xml

# Set our working directory inside the image

#ADD pom.xml /myapp/pom.xml
#ADD src/ /myapp/src/

WORKDIR /myapp

# build app outside docker
# no build stage volumes: https://github.com/moby/moby/issues/38366
# run build process outside, requires having maven and java installed in host system.
# uncomment only, if you have NO snapshots and you a reason to build it inside the container
# RUN mvn -f /myapp/pom.xml clean package
