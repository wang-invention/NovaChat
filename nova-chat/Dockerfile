FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/nova-chat.jar app.jar

EXPOSE 8002

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
