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

RUN curl -L -o /tmp/maven.tar.gz \
    https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz \
    && mkdir -p /usr/maven \
    && tar -xzf /tmp/maven.tar.gz -C /usr/maven --strip-components=1 \
    && rm /tmp/maven.tar.gz

ENV PATH="/usr/maven/bin:$PATH"

RUN mvn clean package -DskipTests \
    && cp target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
