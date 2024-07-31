FROM eclipse-temurin:17 AS builder
WORKDIR workspace
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} catalog-service.jar
#RUN java -Djarmode=layertools -jar catalog-service.jar extract
RUN java -Djarmode=tools -jar catalog-service.jar extract --layers --destination extracted

FROM eclipse-temurin:17
RUN useradd spring
USER spring
WORKDIR workspace
#COPY --from=builder workspace/dependencies/ ./
#COPY --from=builder workspace/spring-boot-loader/ ./
#COPY --from=builder workspace/snapshot-dependencies/ ./
#COPY --from=builder workspace/application/ ./
#ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
COPY --from=builder workspace/extracted/dependencies/ ./
COPY --from=builder workspace/extracted/spring-boot-loader/ ./
COPY --from=builder workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder workspace/extracted/application/ ./
ENTRYPOINT ["java", "-jar", "catalog-service.jar"]

