FROM openjdk:8u342-jdk

LABEL org.opencontainers.image.authors="chengjungao"
LABEL org.opencontainers.image.licenses="Apache-2.0"

ADD target/my-blog-4.0.0-SNAPSHOT.jar /opt/app/blog/app.jar

VOLUME /var/blog/data
EXPOSE 28083
WORKDIR /opt/app/blog

ENTRYPOINT ["java","-jar","app.jar"]