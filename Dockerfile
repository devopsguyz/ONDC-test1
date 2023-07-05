FROM openjdk:22-jdk
RUN microdnf update -y
RUN microdnf install unzip
RUN mkdir -p /opt/ssl/ && mkdir -p /app
ARG DEPENDENCY=.
#RUN unzip ./target/beckn-adaptor-jpa-*.jar
COPY ./beckn-adaptor-jpa/beckn-adaptor-jpa-*.jar .
RUN unzip beckn-adaptor-jpa-*.jar
VOLUME /tmp
RUN  cp -r ${DEPENDANCY}/BOOT-INF/lib /app/lib && cp -r ${DEPENDANCY}/META-INF /app/META-INF && cp -r ${DEPENDANCY}/BOOT-INF/classes /app
#COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
#COPY ${DEPENDENCY}/META-INF /app/META-INF
#COPY ${DEPENDENCY}/BOOT-INF/classes /app
#RUN mkdir -p /opt/ssl/
COPY ./beckn-adaptor-jpa/prod/* /opt/ssl/
ENTRYPOINT ["java","-cp","app:app/lib/*"]
