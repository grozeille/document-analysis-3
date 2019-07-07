FROM openjdk:8

RUN mkdir /opt/api
COPY ./api/target/document-analysis-api.jar /opt/api
COPY ./docker/api/run.sh /opt/api
RUN chmod +x  /opt/api/run.sh
WORKDIR /opt/api

EXPOSE 9000

ENTRYPOINT /opt/api/run.sh
