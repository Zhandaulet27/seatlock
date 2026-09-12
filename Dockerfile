# ---- Build stage ----
# Uses a full Maven+JDK image just to compile and package the jar; this
# stage's layers never end up in the final image.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy only the pom first so Docker can cache the dependency-download layer -
# it's only invalidated when pom.xml itself changes, not on every source edit.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Run stage ----
# JRE-only (no compiler, no Maven) - much smaller than the build image.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Run as a non-root user rather than the container default root.
RUN useradd --create-home --shell /usr/sbin/nologin seatlock
USER seatlock

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
