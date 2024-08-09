package com.bamsecoin.voffcloud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import voffcloud.composeapp.generated.resources.Res
import voffcloud.composeapp.generated.resources.compose_multiplatform

@Serializable
data class Product(
    val productId: String,
    val count: Int,
    val productLabel: String,
    val unitValue: Double
)

@Serializable
data class Transaction(
    val transactionId: String,
    val userId: String,
    val timestamp: Long,
    val subtotal: Double,
    val products: List<Product>,
    val type: String,
    val receiptUrl: String? = null,  // This field is optional
    val reversalId: String? = null,  // This field is optional
    val reversalType: String? = null  // This field is optional
)

suspend fun getTransactions() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    // Retrieve the environment variables
    val serverPath = "https://fridge-api.babel.as/api/pos/allTransactions"  // Fallback URL if env is missing

    // Make the request with the environment variables
    val response: HttpResponse = client.get("https://fridge-api.babel.as/api/pos/allTransactions") {
        // TODO: Move to env before commit
        header("Authorization", "Bearer ${Keys().bamseCoinApi}")
    }
    val body = response.bodyAsText()

    try {
        // Deserialize the response into a list of Transaction objects
        val transactions: List<Transaction> = client.get("https://fridge-api.babel.as/api/pos/allTransactions") {
            header("Authorization", "Bearer ${Keys().bamseCoinApi}")
        }.body()

        // Process each transaction as needed
        transactions.forEach { transaction ->
            println(transaction)
        }
    } catch (e: Exception) {
        println("Error occurred: ${e.message}")
    } finally {
        client.close()
    }


    // Process the response as needed
    println(body)
    println(response.status)

    /*

    val body = response.body<List<Transaction>>()
    body.forEach { transaction ->
        println(transaction)
    }
     */
}

@Composable
@Preview
fun App() {

    GlobalScope.launch(Dispatchers.Main) {
        getTransactions()
    }

    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}