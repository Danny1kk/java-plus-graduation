FROM amazoncorretto:21-alpine-jdk
COPY stats-service/server/target/stats-server-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]