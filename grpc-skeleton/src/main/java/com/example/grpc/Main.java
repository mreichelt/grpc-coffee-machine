package com.example.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Main {

    public static void main(String[] args) throws Exception {
        // TODO: call coffee machine via grpc
        System.out.println("I want coffee!");

        String host = "localhost";
        int port = 50051;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        // tip: run `./gradlew generateProto` to generate Java classes from .proto

        // tip: grpc quick start guide for Java is available here: https://grpc.io/docs/quickstart/java/
        //    the server is already up and running, so you just need to add the client code :-)

        // grpc basics (Java): https://grpc.io/docs/tutorials/basic/java/
    }

}
