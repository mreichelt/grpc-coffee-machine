package com.example.grpc;

import com.example.grpc_coffee.CoffeeMachineGrpc;
import com.example.grpc_coffee.PingRequest;
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

        CoffeeMachineGrpc.CoffeeMachineBlockingStub stub = CoffeeMachineGrpc.newBlockingStub(channel);
        String message = stub.ping(PingRequest.getDefaultInstance()).getMessage();
        System.out.println("server responds with: " + message);

        channel.shutdown().awaitTermination(10, SECONDS);

        // tip: run `./gradlew generateProto` to generate Java classes from .proto

        // tip: grpc quick start guide for Java is available here: https://grpc.io/docs/quickstart/java/
        //    the server is already up and running, so you just need to add the client code :-)

        // grpc basics (Java): https://grpc.io/docs/tutorials/basic/java/
    }

}
