FROM maven:3.8.5-openjdk-17

# Set locale and encoding
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

WORKDIR /sports_platform
COPY . .
RUN mvn clean install -DskipTests -e

CMD mvn spring-boot:run