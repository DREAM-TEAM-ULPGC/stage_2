FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/ingestion-service-1.0.0.jar app.jar
ENV PORT=7001
ENV DATALAKE_DIR=/data/datalake
ENV INGESTION_LOG_FILE=/data/datalake/ingestions.log
VOLUME ["/data"]
EXPOSE 7001
ENTRYPOINT ["java","-jar","/app/app.jar"]
