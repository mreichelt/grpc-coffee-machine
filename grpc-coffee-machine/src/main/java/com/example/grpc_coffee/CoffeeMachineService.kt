package com.example.grpc_coffee

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver

private class CoffeeMachineService : CoffeeMachineGrpc.CoffeeMachineImplBase() {

    override fun ping(
        request: PingRequest,
        responseObserver: StreamObserver<PingResponse>
    ) {
        println("I'm alive")

        val reply = PingResponse.newBuilder()
            .setAlive(true)
            .setMessage("I'm alive - how about some coffee?")
            .build()
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
    }

    override fun getProduct(
        request: ProduceProductRequest,
        responseObserver: StreamObserver<ProduceProductResponse>
    ) {
        val product = request.product
        println("Creating product ${product.name.toLowerCase()}")

        val reply = ProduceProductResponse.newBuilder()
            .setProduct(product)
            .build()
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
    }

}

fun main() {
    val port = 50051
    val server: Server = ServerBuilder.forPort(port)
        .addService(CoffeeMachineService())
        .build()
        .start()

    println("Coffee machine running on port ${server.port}")

    Runtime.getRuntime().addShutdownHook(Thread {
        println("shutting down coffee machine server because JVM shuts down")
        server.shutdown()
    })

    server.awaitTermination()
    println("Coffee machine terminated. Do you want tea instead?")
}
