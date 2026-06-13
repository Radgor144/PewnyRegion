FROM eclipse-temurin:21-jdk AS jlink-stage

WORKDIR /app

COPY region-analytics-service/build/libs/*.jar app.jar

RUN jar -xf app.jar

RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release 21 \
    --print-module-deps \
    --class-path 'BOOT-INF/lib/*' \
    app.jar > deps.info

RUN jlink \
    --add-modules $(cat deps.info),jdk.crypto.ec,java.management,java.naming \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --output /customjre

FROM cgr.dev/chainguard/wolfi-base:latest

WORKDIR /app

ENV JAVA_HOME=/opt/java/runtime
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY --from=jlink-stage /customjre $JAVA_HOME
COPY --from=jlink-stage /app/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]