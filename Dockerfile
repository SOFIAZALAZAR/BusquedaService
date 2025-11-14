# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copiamos lo mínimo para cachear dependencias
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q -B -Dmaven.test.skip=true dependency:go-offline

# Copiamos el código y construimos el jar
COPY src src
RUN ./mvnw -q -B -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Usuario no-root
RUN useradd -r -u 10001 spring

# Copiamos el JAR
COPY --from=build /app/target/*.jar /app/app.jar

# Variables útiles
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError" \
    TZ=America/Argentina/Buenos_Aires

# Render suele usar 10000+, pero exponemos 8080 por convención
EXPOSE 8080
USER spring

# Respetar $PORT que provee Render
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=${PORT:-8080} -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]

