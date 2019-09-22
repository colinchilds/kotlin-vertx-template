# Build with `docker build -t kvt .`
# Run with something like this: `docker run -m512M --cpus 4 -it -p 8080:8080 -e SERVICE_DB_HOST=192.168.1.9 --rm kvt`

FROM adoptopenjdk/openjdk12-openj9:alpine-slim

ENV APPLICATION_USER kvt
RUN adduser -D -g '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY ./build/libs/kvt.jar /app/kvt.jar
WORKDIR /app

EXPOSE 8080

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-Xtune:virtualized", "-jar", "kvt.jar"]