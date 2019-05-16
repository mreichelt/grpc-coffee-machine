package com.example.grpc_coffee

import io.grpc.*
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit.SECONDS

private class CoffeeDrinker(private val channel: ManagedChannel) {

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

    fun printStatusUpdates(name: String, numUpdates: Int = 5) {
        runBlocking {
            println("Getting some status updates…")
            val job = Job()

            val call = channel.newCall(CoffeeMachineGrpc.getGetStatusUpdatesMethod(), CallOptions.DEFAULT)

            val listener = object : ClientCall.Listener<GetStatusUpdatesResponse>() {

                private var counter = 0

                override fun onReady() {
                    println("onReady")
                }

                override fun onMessage(message: GetStatusUpdatesResponse) {
                    println("Got status update: ${message.message}")
                    counter++
                    if (counter >= numUpdates) {
                        closeEverything()
                    } else {
                        call.request(1)
                    }
                }

                override fun onHeaders(headers: Metadata) {
                    println("onReady")
                }

                override fun onClose(status: Status, trailers: Metadata) {
                    println("onClose, status = $status")
                }

                private fun closeEverything() {
                    call.cancel("", null)
                    job.cancel()
                }
            }

            call.start(listener, Metadata())
            call.sendMessage(GetStatusUpdatesRequest.newBuilder().setName(name).build())
            call.halfClose()
            call.request(1)
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
    println()
    coffeeDrinker.printStatusUpdates("Max Mustermann")

    channelToCoffeeMachine.shutdown().awaitTermination(10, SECONDS)
}
