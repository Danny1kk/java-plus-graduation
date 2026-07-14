FROM amazoncorretto:21-alpine-jdk
COPY stats-service/server/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]