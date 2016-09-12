# Use a small Java-enabled Unix Image.
#FROM openjdk:alpine
# OR a rabbit unix image
FROM frolvlad/alpine-oraclejdk8:slim
MAINTAINER = Matt Grigsby <heyahem@hotmail.com>
# Set a directory location and copy in necessary files
VOLUME /tmp
ADD rtapmessaging-0.1.0.jar rtapmessaging-0.1.0.jar
# Resources have to be in the Docker context
ADD countrycodes.txt countrycodes.txt
RUN sh -c 'touch /rtapmessaging-0.1.0.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","rtapmessaging-0.1.0.jar"]