# Build with `docker build -t kvt .`
# Run with something like this: `docker run -m512M --cpus 2 -it -p 8080:8080 --rm kvt`

FROM adoptopenjdk/openjdk12-openj9:alpine-slim

ENV APPLICATION_USER kvt
RUN adduser -D -g '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY ./build/libs/kvt.jar /app/kvt.jar
WORKDIR /app

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-Xtune:virtualized", "-jar", "kvt.jar"]