FROM openjdk:latest as builder
LABEL maintainer="Paweł Breś"

RUN mkdir /tmp/app \
    && mkdir -p /var/run/app

COPY . /tmp/app

RUN cd /tmp/app \
    && ./gradlew distZip \
    && unzip build/distributions/app.zip -d /var/run/app

FROM openjdk:latest

WORKDIR /var/run/app

COPY --from=builder /var/run/app/app .

CMD ["sh", "-c", "bin/app"]