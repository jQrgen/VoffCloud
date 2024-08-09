package com.bamsecoin.voffcloud

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.ui.tooling.preview.Preview

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

val transactions = MutableStateFlow<List<Transaction>>(listOf())
fun getTransactions() = GlobalScope.launch(Dispatchers.Default){
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }
    try {
        // Deserialize the response into a list of Transaction objects
        val result: List<Transaction> = client.get("https://fridge-api.babel.as/api/pos/allTransactions") {
            header("Authorization", "Bearer ${Keys().bamseCoinApi}")
        }.body()

        // Process each transaction as needed
        result.forEach { transaction ->
            println(transaction)
        }

        transactions.value = result.sortedBy { it.timestamp }.reversed()
    } catch (e: Exception) {
        println("Error occurred: ${e.message}")
    } finally {
        client.close()
    }
}

@Composable
@Preview
fun App() {
    getTransactions()

    val transactions = transactions.collectAsState()

    MaterialTheme {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            transactions.value.forEach {
                Card {
                    Column {
                        Text(it.timestamp.toString())
                        Text(it.subtotal.toString())
                        it.products.forEach { product ->
                            Text(product.productLabel)
                            Text(product.count.toString())
                        }
                        Text(it.type)
                        Text(it.receiptUrl ?: "")
                        Text(it.reversalId ?: "")
                        Text(it.reversalType ?: "")
                    }
                }
            }
        }
    }
}