FROM adoptopenjdk/openjdk11
COPY target/upo-wire-transfer-0.0.1-SNAPSHOT.jar upo-wire-transfer-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/upo-wire-transfer-0.0.1-SNAPSHOT.jar"]