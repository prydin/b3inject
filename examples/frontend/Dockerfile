FROM openjdk:9

COPY target/frontend-0.0.1.jar /app/frontend-0.0.1.jar
COPY tmp/b3inject-1.0-SNAPSHOT-jar-with-dependencies.jar /app/b3inject.jar

COPY app.yaml /app/app.yaml

WORKDIR /app
ENTRYPOINT /usr/bin/java -javaagent:/app/b3inject.jar -Db3inject.debug=true -jar /app/frontend-0.0.1.jar server /app/app.yaml

