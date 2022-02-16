FROM openjdk:11.0.14.1-jdk-slim-bullseye as builder
ARG BUILD_CMD="./mvnw clean package -DskipTests -Dmaven.test.skip=true"
ARG RM_MVN_ARCHIVE="rm /root/.m2/wrapper/dists/apache-maven-3.8.4-bin/52ccbt68d252mdldqsfsn03jlf/apache-maven-3.8.4-bin.zip"
COPY .mvn .mvn
COPY mvnw .
#download mvn in a separate layer
RUN ./mvnw ./mvnw --version \
    && ${RM_MVN_ARCHIVE}
COPY pom.xml .
#download all dependencies in a separate layer. We do not want to download them everytime source changes
RUN ${BUILD_CMD} -Dspring-boot.repackage.skip=true \
    && ./mvnw clean \
    && ${RM_MVN_ARCHIVE}
COPY src/ src/
RUN ${BUILD_CMD} \
    && jar -xf target/*.jar \
    && ./mvnw clean \
    && ${RM_MVN_ARCHIVE}

FROM openjdk:11.0.14.1-jre-slim-bullseye
WORKDIR /app
COPY wait-for-it.sh /
RUN chmod +x /wait-for-it.sh
COPY --from=builder /BOOT-INF/lib lib
COPY --from=builder /BOOT-INF/classes .
CMD ["/wait-for-it.sh", "redis:6379", "--", "java", "-cp", "/app:/app/lib/*", "pl.lrozek.redis.stream.tracing.producer.RedisStreamTracingProducerApplication"]