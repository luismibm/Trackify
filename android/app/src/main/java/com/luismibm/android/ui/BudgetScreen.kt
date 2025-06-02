package com.luismibm.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luismibm.android.api.ApiClient
import com.luismibm.android.models.Budget
import com.luismibm.android.models.CreateBudgetRequest
import com.luismibm.android.models.Transaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    token: String?,
    spaceId: String?,
    onError: (String) -> Unit
) {
    var budgets by remember { mutableStateOf<List<Budget>>(emptyList()) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var budgetName by remember { mutableStateOf("") }
    var budgetAmount by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var budgetToDelete by remember { mutableStateOf<Budget?>(null) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    val defaultEndDate = calendar.time
    calendar.add(Calendar.MONTH, -1)
    val defaultStartDate = calendar.time
    
    var startDateText by remember { mutableStateOf(dateFormat.format(defaultStartDate)) }
    var endDateText by remember { mutableStateOf(dateFormat.format(defaultEndDate)) }
    var showDateFilterDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    val spentAmounts = remember(budgets, transactions) {
        budgets.associate { budget ->
            val spent = transactions
                .filter { it.category == budget.name && it.spaceId == budget.spaceId }
                .sumOf { transaction -> if (transaction.amount < 0) abs(transaction.amount.toDouble()) else 0.0 }
            budget.id to spent.toFloat()
        }
    }

    LaunchedEffect(token, spaceId, startDateText, endDateText) {
        if (token != null && spaceId != null) {
            isLoading = true
            try {
                budgets = ApiClient.apiService.getBudgetsBySpace("Bearer $token", spaceId)

                val allTransactions = ApiClient.apiService.getTransactionsBySpace("Bearer $token", spaceId)

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
                onError("Error al cargar datos: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
            if (token == null) onError("Token no disponible. Por favor, inicia sesión de nuevo.")
            if (spaceId == null) onError("Por favor, selecciona un espacio primero.")
            isLoading = false
        }
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
                            isLoading = true
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

    if (showDeleteDialog && budgetToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; budgetToDelete = null },
            title = { Text("Confirmar Eliminación", color = Color.White) },
            text = { Text("¿Estás seguro de que quieres eliminar el presupuesto '${budgetToDelete!!.name}'?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (token != null) {
                                try {
                                    val response = ApiClient.apiService.deleteBudget("Bearer $token", budgetToDelete!!.id)
                                    if (response.isSuccessful) {
                                        budgets = budgets.filterNot { it.id == budgetToDelete!!.id }
                                        showDeleteDialog = false
                                        budgetToDelete = null
                                    } else {
                                        onError("Error al eliminar presupuesto: ${response.message()}")
                                    }
                                } catch (e: Exception) {
                                    onError("Error al eliminar presupuesto: ${e.message}")
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
                    onClick = { showDeleteDialog = false; budgetToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = Color(0xFF2C2C2C)
        )
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Crear Nuevo Presupuesto", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = budgetName,
                        onValueChange = { budgetName = it },
                        label = { Text("Nombre del Presupuesto", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedContainerColor = Color.DarkGray,
                            unfocusedContainerColor = Color.DarkGray,
                            focusedIndicatorColor = Color(0xFF1DB954),
                            unfocusedIndicatorColor = Color.Gray,
                            focusedLabelColor = Color(0xFF1DB954),
                            unfocusedLabelColor = Color.Gray
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = budgetAmount,
                        onValueChange = { budgetAmount = it },
                        label = { Text("Cantidad (€)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedContainerColor = Color.DarkGray,
                            unfocusedContainerColor = Color.DarkGray,
                            focusedIndicatorColor = Color(0xFF1DB954),
                            unfocusedIndicatorColor = Color.Gray,
                            focusedLabelColor = Color(0xFF1DB954),
                            unfocusedLabelColor = Color.Gray
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountFloat = budgetAmount.toFloatOrNull()
                        if (token != null && spaceId != null && budgetName.isNotBlank() && amountFloat != null) {
                            coroutineScope.launch {
                                try {
                                    val currentUser = ApiClient.apiService.getCurrentUser("Bearer $token")
                                    val userId = currentUser.id

                                    val newBudgetRequest = CreateBudgetRequest(
                                        name = budgetName,
                                        amount = amountFloat,
                                        userId = userId,
                                        spaceId = spaceId
                                    )
                                    val createdBudget = ApiClient.apiService.createBudget(
                                        "Bearer $token",
                                        newBudgetRequest
                                    )
                                    budgets = budgets + createdBudget
                                    showCreateDialog = false
                                    budgetName = ""
                                    budgetAmount = ""
                                } catch (e: Exception) {
                                    onError("Error al crear presupuesto: ${e.message}")
                                }
                            }
                        } else {
                            var errorMsg = "Revisa los campos:"
                            if (token == null) errorMsg += "\n- Falta el token de usuario."
                            if (spaceId == null) errorMsg += "\n- Falta el ID del espacio."
                            if (budgetName.isBlank()) errorMsg += "\n- El nombre no puede estar vacío."
                            if (amountFloat == null) errorMsg += "\n- La cantidad debe ser un número válido."
                            onError(errorMsg)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Text("Crear", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCreateDialog = false },
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
                    onClick = { showCreateDialog = true },
                    containerColor = Color(0xFF1DB954)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Crear presupuesto",
                        tint = Color.White
                    )
                }
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
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
                        text = "Cargando datos...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else if (budgets.isEmpty() && spaceId != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No hay presupuestos disponibles",
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Los presupuestos que crees aparecerán aquí.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (spaceId == null) {
                 Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Selecciona un espacio para ver los presupuestos.",
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Periodo: $startDateText a $endDateText",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                    }
                    
                    items(budgets) { budget ->
                        val spent = spentAmounts[budget.id] ?: 0f
                        BudgetCard(
                            budget = budget, 
                            spentAmount = spent,
                            onDeleteClicked = { 
                                budgetToDelete = it
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
fun BudgetCard(
    budget: Budget, 
    spentAmount: Float,
    onDeleteClicked: (Budget) -> Unit
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
                    text = budget.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.ENGLISH, "%.2f € / %.2f €", spentAmount, budget.amount),
                    fontSize = 16.sp,
                    color = if (spentAmount > budget.amount) Color.Red else Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = { onDeleteClicked(budget) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar presupuesto",
                    tint = Color.LightGray
                )
            }
        }
    }
}