package com.example.grpc_coffee

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit.SECONDS

private class CoffeeDrinker(channel: ManagedChannel) {

    private val blockingStub = CoffeeMachineGrpc.newBlockingStub(channel)
    private val nonBlockingStub = CoffeeMachineGrpc.newStub(channel)

    fun ping() {
        println("Pinging coffee machine now…")
        val pingResponse = blockingStub.ping(PingRequest.getDefaultInstance())
        println("Coffee machine is alive=${pingResponse.alive} and replied with: '${pingResponse.message}'")
    }

    fun giveMeMy(product: Product) {
        println("Asking coffee machine to make me ${product.name.toLowerCase()}…")
        val request = ProduceProductRequest.newBuilder().setProduct(product).build()
        val response = blockingStub.getProduct(request)
        println("Coffee machine made me ${response.product.name.toLowerCase()} :-)")
    }

    fun printOnlyCappuccinosFromHistory() {
        runBlocking {
            println("Getting only cappuccinos…")
            val job = Job()

            val start = Instant.now()
            val responseObserver = object : StreamObserver<GetHistoryResponse> {
                var cappuccinos = 0
                override fun onNext(value: GetHistoryResponse) {
                    if (value.product == Product.CAPPUCCINO) {
                        cappuccinos++
                        println("Got $cappuccinos cappuccinos so far :-)")
                    }
                }

                override fun onError(throwable: Throwable) {
                    println("error $throwable")
                    job.cancel()
                }

                override fun onCompleted() {
                    val duration = ChronoUnit.SECONDS.between(start, Instant.now())
                    println("Getting history took $duration seconds")
                    job.cancel()
                }

            }

            nonBlockingStub.getHistory(GetHistoryRequest.getDefaultInstance(), responseObserver)
            job.join()
        }
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
    println()
    coffeeDrinker.printOnlyCappuccinosFromHistory()

    channelToCoffeeMachine.shutdown().awaitTermination(10, SECONDS)
}
