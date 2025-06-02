package com.luismibm.android.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.luismibm.android.api.ApiClient
import com.luismibm.android.models.CreateTransactionRequest
import com.luismibm.android.models.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon

@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier,
    token: String?,
    spaceId: String?,
    onError: (String) -> Unit
) {
    val transactions by viewModel.transactions
    val isLoading by viewModel.isLoading
    val showDeleteDialog by viewModel.showDeleteDialog
    val transactionToDelete by viewModel.transactionToDelete
    val showCreateTransactionDialog by viewModel.showCreateTransactionDialog
    val isCreatingTransaction by viewModel.isCreatingTransaction
    val transactionAmountInput by viewModel.transactionAmountInput
    val transactionCategoryInput by viewModel.transactionCategoryInput
    val transactionObjectiveInput by viewModel.transactionObjectiveInput
    val transactionDescriptionInput by viewModel.transactionDescriptionInput
    val startDateText by viewModel.startDateText
    val endDateText by viewModel.endDateText
    val showDateFilterDialog by viewModel.showDateFilterDialog

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    LaunchedEffect(token, spaceId, startDateText, endDateText) {
        viewModel.loadTransactions(token, spaceId, onError)
    }

    if (showCreateTransactionDialog) {
        CreateTransactionDialog(
            onDismissRequest = {
                viewModel.toggleCreateTransactionDialog(false)
            },
            onSaveRequest = { amount, category, objective, description ->
                viewModel.createTransaction(
                    amount, category, objective, description,
                    token, spaceId, onError
                )
            },
            amount = transactionAmountInput,
            onAmountChange = { viewModel.onTransactionAmountInputChange(it) },
            category = transactionCategoryInput,
            onCategoryChange = { viewModel.onTransactionCategoryInputChange(it) },
            objective = transactionObjectiveInput,
            onObjectiveChange = { viewModel.onTransactionObjectiveInputChange(it) },
            description = transactionDescriptionInput,
            onDescriptionChange = { viewModel.onTransactionDescriptionInputChange(it) },
            isLoading = isCreatingTransaction
        )
    }

    if (showDeleteDialog && transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.setTransactionToDelete(null) },
            title = { Text("Confirmar Eliminación", color = Color.White) },
            text = { Text("¿Estás seguro de que quieres eliminar esta transacción: ${transactionToDelete!!.category} (${String.format(Locale.GERMAN, "%.2f", transactionToDelete!!.amount)}€)?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteTransaction(token,onError) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.setTransactionToDelete(null) },
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
            onDismissRequest = { viewModel.toggleDateFilterDialog(false) },
            title = { Text("Filtrar por Fechas", color = Color.White) },
            text = {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text("Fecha de inicio", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = startDateText,
                        onValueChange = { viewModel.onStartDateTextChange(it) },
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
                        onValueChange = { viewModel.onEndDateTextChange(it) },
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
                    onClick = { viewModel.applyDateFilter(onError) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Text("Aplicar", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.toggleDateFilterDialog(false) },
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
                    onClick = { viewModel.toggleCreateTransactionDialog(true) },
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
                            onDeleteClicked = { viewModel.setTransactionToDelete(it) }
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