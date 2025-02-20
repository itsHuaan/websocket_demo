FROM oraclelinux:9

# Cài đặt JDK 23 từ Oracle
RUN dnf install -y wget \
    && wget -O /tmp/jdk-23_linux-x64_bin.tar.gz \
       "https://download.oracle.com/java/23/latest/jdk-23_linux-x64_bin.tar.gz" \
    && mkdir -p /usr/java \
    && tar -xzf /tmp/jdk-23_linux-x64_bin.tar.gz --strip-components=1 -C /usr/java \
    && rm /tmp/jdk-23_linux-x64_bin.tar.gz

# Kiểm tra thư mục JDK
RUN ls -la /usr/java && ls -la /usr/java/bin

# Thiết lập JAVA_HOME và PATH
ENV JAVA_HOME=/usr/java
ENV PATH="$JAVA_HOME/bin:$PATH"

WORKDIR /app

COPY . .

# Đảm bảo Gradle có quyền chạy
RUN chmod +x gradlew \
    && ./gradlew build -x test

EXPOSE 8080

CMD ["java", "-jar", "build/libs/websocket_demo-0.0.1-SNAPSHOT.jar"]
