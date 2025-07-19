FROM oraclelinux:9

RUN dnf install -y curl tar gzip

RUN curl -L -o /tmp/jdk.tar.gz \
    https://github.com/adoptium/temurin23-binaries/releases/download/jdk-23.0.2%2B7/OpenJDK23U-jdk_x64_linux_hotspot_23.0.2_7.tar.gz \
    && mkdir -p /usr/java \
    && tar -xzf /tmp/jdk.tar.gz -C /usr/java --strip-components=1 \
    && rm /tmp/jdk.tar.gz

ENV JAVA_HOME=/usr/java
ENV PATH="$JAVA_HOME/bin:$PATH"

WORKDIR /app

COPY . .

RUN chmod +x gradlew && ./gradlew build -x test \
    && mv build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
