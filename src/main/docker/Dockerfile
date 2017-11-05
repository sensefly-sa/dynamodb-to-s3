FROM openjdk:8-jre-alpine

ENV SPRING_OUTPUT_ANSI_ENABLED ALWAYS
ENV JAVA_OPTS ""

ADD app.jar /app.jar
ADD log4j-cloudwatch.xml /
ADD entrypoint.sh /

RUN sh -c 'touch /app.jar'

ENTRYPOINT ["/entrypoint.sh"]
CMD ["--help"]