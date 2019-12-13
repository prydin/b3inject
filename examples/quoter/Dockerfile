FROM openjdk:9

COPY target/quoter-0.0.1.jar /app/quoter-0.0.1.jar
COPY tmp/b3inject-1.0-SNAPSHOT-jar-with-dependencies.jar /app/b3inject.jar

COPY quoter.properties /app/quoter.properties

WORKDIR /app
ENTRYPOINT /usr/bin/java -Dspring.config.location=quoter.properties -javaagent:/app/b3inject.jar -Db3inject.debug=true -jar /app/quoter-0.0.1.jar

