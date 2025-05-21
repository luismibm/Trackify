package com.luismibm.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.luismibm.android.api.RetrofitClient
import com.luismibm.android.auth.CreateTransactionRequest
import com.luismibm.android.auth.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon

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

    // State for creation dialog
    var showCreateTransactionDialog by remember { mutableStateOf(false) }
    var transactionAmountInput by remember { mutableStateOf("") }
    var transactionCategoryInput by remember { mutableStateOf("") }
    var transactionObjectiveInput by remember { mutableStateOf("") }
    var transactionDescriptionInput by remember { mutableStateOf("") }
    var isCreatingTransaction by remember { mutableStateOf(false) }

    // State for date filter dialog
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    val defaultEndDate = calendar.time
    calendar.add(Calendar.MONTH, -1)
    val defaultStartDate = calendar.time

    var startDateText by remember { mutableStateOf(dateFormat.format(defaultStartDate)) }
    var endDateText by remember { mutableStateOf(dateFormat.format(defaultEndDate)) }
    var showDateFilterDialog by remember { mutableStateOf(false) }
    
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    LaunchedEffect(token, spaceId, startDateText, endDateText) {
        if (token != null && spaceId != null) {
            isLoading = true
            try {
                val allTransactions = withContext(Dispatchers.IO) {
                    RetrofitClient.authService.getTransactionsBySpace("Bearer $token", spaceId)
                }

                val startDate = dateFormat.parse(startDateText)
                val endDate = dateFormat.parse(endDateText)

                val calendar = Calendar.getInstance()
                calendar.time = endDate
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val adjustedEndDate = calendar.time

                transactions = allTransactions.filter { 
                    it.date >= startDate && it.date < adjustedEndDate
                }
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

    if (showCreateTransactionDialog) {
        CreateTransactionDialog(
            onDismissRequest = {
                showCreateTransactionDialog = false
                transactionAmountInput = ""
                transactionCategoryInput = ""
                transactionObjectiveInput = ""
                transactionDescriptionInput = ""
            },
            onSaveRequest = { amount, category, objective, description ->
                if (token != null && spaceId != null) {
                    isCreatingTransaction = true
                    coroutineScope.launch {
                        try {
                            val currentUser = RetrofitClient.authService.getCurrentUser("Bearer $token")
                            val userId = currentUser.id
                            val finalObjective = if (objective.isBlank()) "None" else objective

                            val newTransactionRequest = CreateTransactionRequest(
                                amount = amount,
                                category = category,
                                objective = finalObjective,
                                userId = userId,
                                spaceId = spaceId,
                                date = null,
                                description = description
                            )
                            val createdTransaction = withContext(Dispatchers.IO) {
                                RetrofitClient.authService.createTransaction("Bearer $token", newTransactionRequest)
                            }
                            transactions = listOf(createdTransaction) + transactions
                            showCreateTransactionDialog = false
                            transactionAmountInput = ""
                            transactionCategoryInput = ""
                            transactionObjectiveInput = ""
                            transactionDescriptionInput = ""
                        } catch (e: Exception) {
                            onError("Error al crear transacción: ${e.message}")
                        } finally {
                            isCreatingTransaction = false
                        }
                    }
                } else {
                    onError("Token o Space ID no disponibles.")
                }
            },
            amount = transactionAmountInput,
            onAmountChange = { transactionAmountInput = it },
            category = transactionCategoryInput,
            onCategoryChange = { transactionCategoryInput = it },
            objective = transactionObjectiveInput,
            onObjectiveChange = { transactionObjectiveInput = it },
            description = transactionDescriptionInput,
            onDescriptionChange = { transactionDescriptionInput = it },
            isLoading = isCreatingTransaction
        )
    }

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

    if (showDateFilterDialog) {
        AlertDialog(
            onDismissRequest = { showDateFilterDialog = false },
            title = { Text("Filtrar por Fechas", color = Color.White) },
            text = {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text("Fecha de inicio", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = startDateText,
                        onValueChange = { startDateText = it },
                        placeholder = { Text("YYYY-MM-DD", color = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = Color.DarkGray,
                            focusedContainerColor = Color.DarkGray,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Fecha de fin", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = endDateText,
                        onValueChange = { endDateText = it },
                        placeholder = { Text("YYYY-MM-DD", color = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = Color.DarkGray,
                            focusedContainerColor = Color.DarkGray,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            dateFormat.parse(startDateText)
                            dateFormat.parse(endDateText)
                            showDateFilterDialog = false
                            isLoading = true // Recargar datos con las nuevas fechas
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Formato de fecha inválido. Use YYYY-MM-DD", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Text("Aplicar", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDateFilterDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = Color(0xFF2C2C2C)
        )
    }

    Scaffold(
        floatingActionButton = {
            if (token != null && spaceId != null) {
                FloatingActionButton(
                    onClick = { showCreateTransactionDialog = true },
                    containerColor = Color(0xFF1DB954),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, "Añadir transacción", tint = Color.White)
                }
            }
        },
        containerColor = Color.Black
    ) { paddingValuesFromScaffold ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValuesFromScaffold)
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
                    // Mostrar el periodo de fechas seleccionado
                    item {
                        Text(
                            text = "Periodo: $startDateText a $endDateText",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                    }
                    
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

@Composable
fun CreateTransactionDialog(
    onDismissRequest: () -> Unit,
    onSaveRequest: (amount: Float, category: String, objective: String, description: String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    objective: String,
    onObjectiveChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                "Nueva Transacción",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                TextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = { Text("Importe (ej: -50 o 100)", color = Color.Gray) },
                    singleLine = true,
                    // keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF1DB954),
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color.DarkGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = Color(0xFF1DB954),
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = category,
                    onValueChange = onCategoryChange,
                    label = { Text("Categoría", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                     colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF1DB954),
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color.DarkGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = Color(0xFF1DB954),
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                 TextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Descripción", color = Color.Gray) },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF1DB954),
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color.DarkGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = Color(0xFF1DB954),
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = objective,
                    onValueChange = onObjectiveChange,
                    label = { Text("Objetivo (opcional)", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF1DB954),
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color.DarkGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = Color(0xFF1DB954),
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountFloat = amount.toFloatOrNull()
                    if (amountFloat == null) {
                        android.widget.Toast.makeText(context, "Importe inválido.", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (category.isBlank()) {
                        android.widget.Toast.makeText(context, "La categoría no puede estar vacía.", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (description.isBlank()) {
                        android.widget.Toast.makeText(context, "La descripción no puede estar vacía.", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onSaveRequest(amountFloat, category, objective, description)
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Guardar", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, enabled = !isLoading) {
                Text("Cancelar", color = Color.LightGray)
            }
        },
        containerColor = Color(0xFF2C2C2C),
        properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading)
    )
} 