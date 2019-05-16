package com.example.grpc_coffee

import com.google.protobuf.Timestamp
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.random.Random

private class CoffeeMachineService : CoffeeMachineGrpc.CoffeeMachineImplBase() {

    private val productHistory: MutableList<ProductHistoryItem> =
        randomProductHistorySequence().take(100000).toMutableList()

    private fun randomProductHistorySequence() = sequence {
        val allProducts = Product.values().filterNot { it == Product.UNRECOGNIZED }
        var createdAt = LocalDate.of(2018, 12, 1).atStartOfDay(ZoneId.of("Europe/Berlin"))
        while (true) {
            yield(ProductHistoryItem(allProducts.random(), createdAt))
            createdAt += Duration.ofSeconds(Random.nextLong(10, 60))
        }
    }

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

    override fun getHistory(
        request: GetHistoryRequest,
        responseObserver: StreamObserver<GetHistoryResponse>
    ) {
        println("Sending my product history…")
        GlobalScope.launch {
            val productHistoryCopy = productHistory.toList()
            productHistoryCopy.chunked(50).forEach { chunkOfHistoryItems ->
                chunkOfHistoryItems.forEach { historyItem ->
                    val createdAt = historyItem.createdAt
                    val reply = GetHistoryResponse.newBuilder()
                        .setProduct(historyItem.product)
                        .setCreatedAt(Timestamp.newBuilder().setSeconds(createdAt.toEpochSecond()).setNanos(createdAt.nano).build())
                        .build()
                    responseObserver.onNext(reply)
                }
                // artificial delay that simulates the history being read from somewhere (i.e. a database)
                delay(10)
            }
            responseObserver.onCompleted()
        }
    }

    override fun getStatusUpdates(
        request: GetStatusUpdatesRequest,
        responseObserver: StreamObserver<GetStatusUpdatesResponse>
    ) {
        println("Sending status updates for ${request.name} until end of call…")
        GlobalScope.launch {
            var counter = 0
            while (true) {
                responseObserver as ServerCallStreamObserver<GetStatusUpdatesResponse>
                counter++
                delay(1000)
                if (responseObserver.isCancelled) {
                    println("status update cancelled by client")
                    return@launch
                }
                val response = GetStatusUpdatesResponse
                    .newBuilder()
                    .setMessage("status update $counter for ${request.name}")
                    .build()
                println(response.message)
                responseObserver.onNext(response)
            }
        }
    }

}

private data class ProductHistoryItem(val product: Product, val createdAt: ZonedDateTime)

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
