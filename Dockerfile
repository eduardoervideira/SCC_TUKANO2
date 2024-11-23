FROM openjdk:17-jdk-slim

WORKDIR /app

RUN mkdir -p /app/blobs && chmod 777 /app/blobs

COPY target/tukano-1-jar-with-dependencies.jar app.jar
COPY hibernate.cfg.xml .

EXPOSE 8080

CMD ["java", "-cp", "app.jar", "tukano.impl.rest.TukanoRestServer", "-secret", "mysecret"]