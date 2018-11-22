FROM maven:3-jdk-8

ENV APP_ROOT /myapp

# Set our working directory inside the image

RUN mkdir /myapp

WORKDIR /myapp

# cft hub.docker.com/r/library/maven
COPY ./docker-resources/app/settings.xml /usr/share/maven/ref/

COPY pom.xml /myapp/pom.xml
COPY src/main/torque-schema/ /myapp/src/main/torque-schema/
COPY src/main/webapp/ /myapp/src/main/webapp/

# builds app and runs commands connecting to mysql port 13306
# CMD ["mvn clean install"] 
# RUN mvn clean install & sleep 10

# copy generated target et al.
COPY . /myapp/

RUN sleep 5

# do this not in build
#  mvn jetty:run


