FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/www
COPY ./build condation-server
WORKDIR /opt/www/condation-server
ADD ./docker/server.toml /opt/www/condation-server/
ADD ./docker/site.toml /opt/www/condation-server/hosts/demo/
CMD ["java", "-jar", "cms-server-7.8.0.jar", "server", "start"]