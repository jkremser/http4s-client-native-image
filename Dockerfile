FROM jkremser/mini-jre:8.1
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
ADD target/scala-2.13/http4s-native-image-assembly-*.jar /app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app.jar"]