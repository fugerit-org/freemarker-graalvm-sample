# native-image-helidon

## Build and run

With JDK21 JIT

```shell
mvn package
java -jar target/native-image-helidon.jar
```

With JDK21 AOT

```shell
mvn clean package -Pnative-image
java -jar target/native-image-helidon.jar
```