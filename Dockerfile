FROM openjdk:8-jdk

RUN mkdir /root/.m2
COPY ./maven-settings.xml /root/.m2/settings.xml

COPY . /root/iam
WORKDIR /root/iam

RUN ./mvnw package && ./mvnw install

CMD ./mvnw spring-boot:run

