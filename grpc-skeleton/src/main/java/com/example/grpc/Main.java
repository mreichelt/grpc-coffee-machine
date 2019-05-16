package com.example.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("I want coffee!");

        String host = "localhost";
        int port = 50051;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        // TODO: call coffee machine via grpc (create stub and call ping)

        channel.shutdown().awaitTermination(10, SECONDS);

        // tip: run `./gradlew generateProto` to generate Java classes from .proto

        // tip: grpc quick start guide for Java is available here: https://grpc.io/docs/quickstart/java/
        //    the server is already up and running, so you just need to add the client code :-)

        // grpc basics (Java): https://grpc.io/docs/tutorials/basic/java/
    }

}
