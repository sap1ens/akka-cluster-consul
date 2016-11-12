#sbt stage && docker build -t "$(basename "$(pwd)")" . && docker run -it "$(basename "$(pwd)")"

FROM openjdk:8-jre

ADD run.sh /app/run.sh

ADD target/scala-2.11/akka-microservice-assembly-1.0.jar /app/app.jar

EXPOSE 80 81 82 2551 2552 2553

ENV SERVICE_NAME akka-cluster-demo

ENTRYPOINT ["app/run.sh"]