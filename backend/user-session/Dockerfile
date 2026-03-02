FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace/app

COPY pom.xml .
COPY src src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

EXPOSE 8083

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.foodsecuritynet.usersession.UserSessionApp"]
