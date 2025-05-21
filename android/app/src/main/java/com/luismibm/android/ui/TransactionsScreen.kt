package com.luismibm.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luismibm.android.api.RetrofitClient
import com.luismibm.android.auth.Transaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    token: String?,
    spaceId: String?,
    onError: (String) -> Unit
) {
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // State for deletion confirmation
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LaunchedEffect(key1 = token, key2 = spaceId) {
        if (token != null && spaceId != null) {
            isLoading = true
            try {
                transactions = RetrofitClient.authService.getTransactionsBySpace("Bearer $token", spaceId)
            } catch (e: Exception) {
                onError("Error al cargar las transacciones: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
             if (token == null) onError("Token no disponible. Por favor, inicia sesión de nuevo.")
             if (spaceId == null) onError("Por favor, selecciona un espacio para ver las transacciones.")
            isLoading = false
        }
    }

    // Confirmation Dialog for Deletion
    if (showDeleteDialog && transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; transactionToDelete = null },
            title = { Text("Confirmar Eliminación", color = Color.White) },
            text = { Text("¿Estás seguro de que quieres eliminar esta transacción: ${transactionToDelete!!.category} (${String.format(Locale.GERMAN, "%.2f", transactionToDelete!!.amount)}€)?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (token != null) {
                                try {
                                    val response = RetrofitClient.authService.deleteTransaction("Bearer $token", transactionToDelete!!.id)
                                    if (response.isSuccessful) {
                                        transactions = transactions.filterNot { it.id == transactionToDelete!!.id }
                                        showDeleteDialog = false
                                        transactionToDelete = null
                                    } else {
                                        onError("Error al eliminar transacción: ${response.message()}")
                                    }
                                } catch (e: Exception) {
                                    onError("Error al eliminar transacción: ${e.message}")
                                }
                            } else {
                                onError("Token no disponible para eliminar.")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false; transactionToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = Color(0xFF2C2C2C)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cargando transacciones...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else if (transactions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (spaceId == null) "Selecciona un espacio para ver las transacciones." else "No hay transacciones disponibles",
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                if (spaceId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Las transacciones que realices aparecerán aquí",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        dateFormatter = dateFormatter,
                        onDeleteClicked = {
                            transactionToDelete = it
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    dateFormatter: SimpleDateFormat,
    onDeleteClicked: (Transaction) -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.category,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transaction.description,
                    color = Color.LightGray,
                    fontSize = 15.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fecha: ${dateFormatter.format(transaction.date)}",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format(Locale.GERMAN, "%.2f €", transaction.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (transaction.amount >= 0) Color(0xFF1DB954) else Color.Red,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = { onDeleteClicked(transaction) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar transacción",
                    tint = Color.LightGray
                )
            }
        }
    }
} 