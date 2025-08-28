FROM maven:3.9.9-eclipse-temurin-21-alpine

RUN apk --no-cache add zip

CMD ["mvn"]