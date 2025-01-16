FROM maven:3.9.8-eclipse-temurin-21 AS vpp-builder

WORKDIR /opt/vpp

COPY ./pom.xml .
COPY ./src ./src

RUN mvn clean install -DskipTests=true

FROM eclipse-temurin:21-jre-alpine AS vpp

WORKDIR /opt/vpp

COPY --from=vpp-builder /opt/vpp/target/vpp.jar .

RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup
USER appuser

EXPOSE 8080

CMD ["java", "-jar", "-Dserver.port=8080", "vpp.jar"]
