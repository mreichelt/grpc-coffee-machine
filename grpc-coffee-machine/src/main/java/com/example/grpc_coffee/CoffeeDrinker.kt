package com.example.grpc_coffee

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit.SECONDS

private class CoffeeDrinker(channel: ManagedChannel) {

    private val coffeeMachineStub = CoffeeMachineGrpc.newBlockingStub(channel)

    fun ping() {
        println("Pinging coffee machine now…")
        val pingResponse = coffeeMachineStub.ping(PingRequest.getDefaultInstance())
        println("Coffee machine is alive=${pingResponse.alive} and replied with: '${pingResponse.message}'")
    }

    fun giveMeMy(product: Product) {
        println("Asking coffee machine to make me ${product.name.toLowerCase()}…")
        val request = ProduceProductRequest.newBuilder().setProduct(product).build()
        val response = coffeeMachineStub.getProduct(request)
        println("Coffee machine made me ${response.product.name.toLowerCase()} :-)")
    }

}

fun main() {
    val channelToCoffeeMachine = ManagedChannelBuilder.forAddress("localhost", 50051)
        .usePlaintext()
        .build()

    val coffeeDrinker = CoffeeDrinker(channelToCoffeeMachine)
    coffeeDrinker.ping()
    println()
    coffeeDrinker.giveMeMy(Product.CAPPUCCINO)

    channelToCoffeeMachine.shutdown().awaitTermination(1, SECONDS)
}
