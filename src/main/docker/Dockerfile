FROM openjdk:8-jre-alpine

ENV SPRING_OUTPUT_ANSI_ENABLED ALWAYS
ENV JAVA_OPTS ""

ADD @project.build.finalName@.jar /app.jar

ADD log4j-cloudwatch.xml /

RUN sh -c 'touch /app.jar'

ENTRYPOINT ["/bin/sh", "-c", "exec /usr/bin/java $JAVA_OPTS -jar /app.jar $0 $@"]
CMD ["--help"]