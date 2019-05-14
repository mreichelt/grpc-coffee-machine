package com.example.grpc_coffee

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit.SECONDS

private class CoffeeDrinker(channel: ManagedChannel) {

    private val coffeeMachineStub = CoffeeMachineGrpc.newBlockingStub(channel)

    fun ping() {
        println("Pinging coffee machine now…")
        val pingResponse = coffeeMachineStub.ping(PingRequest.getDefaultInstance())
        println("Coffee machine is alive=${pingResponse.alive} and replies with: '${pingResponse.message}'")
    }

}

fun main() {
    val channelToCoffeeMachine = ManagedChannelBuilder.forAddress("localhost", 50051)
        .usePlaintext()
        .build()

    val coffeeDrinker = CoffeeDrinker(channelToCoffeeMachine)
    coffeeDrinker.ping()

    channelToCoffeeMachine.shutdown().awaitTermination(1, SECONDS)
}
