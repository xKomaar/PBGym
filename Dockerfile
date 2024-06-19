FROM jelastic/maven:3.9.5-openjdk-21 AS build
COPY . .
RUN mvn clean verify

FROM openjdk:21-jdk-slim
COPY --from=build /target/PBGym-0.0.1-SNAPSHOT.jar pbgym.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","pbgym.jar"]