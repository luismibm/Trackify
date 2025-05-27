package com.luismibm.android.ui

import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.luismibm.android.api.ApiClient
import com.luismibm.android.models.CreateTransactionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextButton
import com.github.mikephil.charting.formatter.PercentFormatter
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    token: String,
    spaceId: String
) {
    var isLoading by remember { mutableStateOf(true) }
    var totalIncome by remember { mutableStateOf(0.0) }
    var totalExpenses by remember { mutableStateOf(0.0) }
    var balance by remember { mutableStateOf(0.0) }
    var categoryPieData by remember { mutableStateOf<List<PieEntry>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados para filtro de fechas
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    
    // Por defecto, establecemos el rango del último mes
    val defaultEndDate = calendar.time
    calendar.add(Calendar.MONTH, -1)
    val defaultStartDate = calendar.time
    
    var startDateText by remember { mutableStateOf(dateFormat.format(defaultStartDate)) }
    var endDateText by remember { mutableStateOf(dateFormat.format(defaultEndDate)) }
    var showDateFilterDialog by remember { mutableStateOf(false) }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showCreateTransactionDialog by remember { mutableStateOf(false) }
    var transactionAmountInput by remember { mutableStateOf("") }
    var transactionCategoryInput by remember { mutableStateOf("") }
    var transactionObjectiveInput by remember { mutableStateOf("") }
    var transactionDescriptionInput by remember { mutableStateOf("") }

    LaunchedEffect(token, spaceId, isLoading, startDateText, endDateText) {
        if (isLoading && token.isNotBlank() && spaceId.isNotBlank()) {
            errorMessage = null
            try {
                val allTransactions = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getTransactionsBySpace("Bearer $token", spaceId)
                }
                
                val startDate = dateFormat.parse(startDateText)
                val endDate = dateFormat.parse(endDateText)
                
                calendar.time = endDate
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val adjustedEndDate = calendar.time
                
                val filteredTransactions = allTransactions.filter {
                    it.date >= startDate && it.date < adjustedEndDate
                }
                
                var income = 0.0
                var expenses = 0.0
                val expensesByCategory = mutableMapOf<String, Double>()

                filteredTransactions.forEach { transaction ->
                    if (transaction.amount > 0) {
                        income += transaction.amount
                    } else {
                        expenses += transaction.amount
                        val category = transaction.category ?: "Sin Categoría"
                        expensesByCategory[category] = (expensesByCategory[category] ?: 0.0) + transaction.amount
                    }
                }

                totalIncome = income
                totalExpenses = expenses
                balance = income + expenses

                val pieEntries = expensesByCategory
                    .filter { it.value < 0 }
                    .map { (category, amount) -> PieEntry(kotlin.math.abs(amount.toFloat()), category) }
                    .filter { it.value > 0 }

                categoryPieData = pieEntries

            } catch (e: Exception) {
                errorMessage = "Error al cargar datos: ${e.message}"
                Log.e("HomeScreen", "Error al cargar datos", e)
            } finally {
                isLoading = false
            }
        } else if (token.isBlank() || spaceId.isBlank()) {
            errorMessage = "Token o Space ID no disponibles."
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
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = Color.White,
                            backgroundColor = Color.DarkGray,
                            cursorColor = Color(0xFF1DB954),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
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
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = Color.White,
                            backgroundColor = Color.DarkGray,
                            cursorColor = Color(0xFF1DB954),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
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
                            Toast.makeText(context, "Formato de fecha inválido. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1DB954))
                ) {
                    Text("Aplicar", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDateFilterDialog = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)
                ) {
                    Text("Cancelar", color = Color.White)
                }
            },
            backgroundColor = Color(0xFF2C2C2C)
        )
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
                showCreateTransactionDialog = false
                if (token.isNotBlank() && spaceId.isNotBlank()) {
                    scope.launch {
                        try {
                            val currentUser = ApiClient.apiService.getCurrentUser("Bearer $token")
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
                            Log.d("HomeScreenDebug", "Intentando crear transacción con: Amount: $amount, Category: '$category', Objective: '$finalObjective', UserId: '$userId', SpaceId: '$spaceId', Description: '$description'")
                            
                            val createdTransaction = withContext(Dispatchers.IO) {
                                ApiClient.apiService.createTransaction("Bearer $token", newTransactionRequest)
                            }
                            Log.d("HomeScreen", "Transacción creada: ${createdTransaction.id}")
                            Toast.makeText(context, "Transacción creada: ${createdTransaction.category} ${createdTransaction.amount}", Toast.LENGTH_LONG).show()
                            
                            transactionAmountInput = ""
                            transactionCategoryInput = ""
                            transactionObjectiveInput = ""
                            transactionDescriptionInput = ""

                            isLoading = true
                        } catch (e: Exception) {
                            Log.e("HomeScreen", "Error al crear transacción: ${e.message}", e)
                            Toast.makeText(context, "Error al crear transacción: ${e.message}", Toast.LENGTH_LONG).show()
                            errorMessage = "Error al crear transacción: ${e.message}"
                        }
                    }
                } else {
                    Toast.makeText(context, "Token o Space ID no disponibles para crear transacción", Toast.LENGTH_LONG).show()
                }
            },
            amount = transactionAmountInput,
            onAmountChange = { transactionAmountInput = it },
            category = transactionCategoryInput,
            onCategoryChange = { transactionCategoryInput = it },
            objective = transactionObjectiveInput,
            onObjectiveChange = { transactionObjectiveInput = it },
            description = transactionDescriptionInput,
            onDescriptionChange = { transactionDescriptionInput = it }
        )
    }

    M3Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateTransactionDialog = true },
                containerColor = Color(0xFF1DB954),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Añadir transacción", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading && categoryPieData.isEmpty()) {
                CircularProgressIndicator(color = Color(0xFF1DB954))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resumen Financiero",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Button(
                        onClick = { showDateFilterDialog = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Filtrar por Fechas", color = Color.White, fontSize = 12.sp)
                    }
                }
                
                Text(
                    text = "Periodo: $startDateText a $endDateText",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                FinancialSummaryText(
                    income = totalIncome,
                    expenses = totalExpenses,
                    balance = balance,
                    formatter = currencyFormatter
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (categoryPieData.isNotEmpty()) {
                    Text(
                        text = "Gastos por Categoría",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryPercentageChart(entries = categoryPieData)
                } else if (!isLoading) {
                     Text(
                        text = "No hay datos de gastos por categoría para mostrar.",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun FinancialSummaryText(
    income: Double,
    expenses: Double,
    balance: Double,
    formatter: NumberFormat
) {
    val positiveGreen = Color(0xFF1DB954)
    val negativeRed = Color.Red

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Balance:", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text(formatter.format(balance), fontSize = 20.sp, color = if (balance >= 0) positiveGreen else negativeRed, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ingresos:", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(formatter.format(income), fontSize = 18.sp, color = positiveGreen, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Gastos:", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(formatter.format(kotlin.math.abs(expenses)), fontSize = 18.sp, color = negativeRed, fontWeight = FontWeight.Bold)
        }

    }
}

@Composable
fun CategoryPercentageChart(entries: List<PieEntry>) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    800
                )
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(Color.Black.toArgb())
                setEntryLabelColor(Color.White.toArgb())
                setEntryLabelTextSize(12f)
                legend.isEnabled = false

                val dataSet = PieDataSet(entries, "").apply {
                    colors = ColorTemplate.MATERIAL_COLORS.toList()
                    sliceSpace = 3f
                    valueTextColor = Color.White.toArgb()
                    valueTextSize = 14f
                    setDrawValues(true)
                }

                data = PieData(dataSet).apply {
                    setValueFormatter(PercentFormatter())
                    setValueTextColor(Color.White.toArgb())
                    setValueTextSize(14f)
                }
                setUsePercentValues(true)
                animateXY(1000, 1000)
                invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
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
    onDescriptionChange: (String) -> Unit
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.White,
                        cursorColor = Color(0xFF1DB954),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        backgroundColor = Color.DarkGray,
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
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.White,
                        cursorColor = Color(0xFF1DB954),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        backgroundColor = Color.DarkGray,
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
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.White,
                        cursorColor = Color(0xFF1DB954),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        backgroundColor = Color.DarkGray,
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
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.White,
                        cursorColor = Color(0xFF1DB954),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        backgroundColor = Color.DarkGray,
                        focusedLabelColor = Color(0xFF1DB954),
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Cancelar", color = Color.LightGray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val amountFloat = amount.toFloatOrNull()
                        if (amountFloat == null) {
                            Toast.makeText(context, "Importe inválido", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (category.isBlank()) {
                            Toast.makeText(context, "La categoría no puede estar vacía", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (description.isBlank()) {
                            Toast.makeText(context, "La descripción no puede estar vacía", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val finalObjectiveOnClick = if (objective.isBlank()) "None" else objective
                        onSaveRequest(amountFloat, category, finalObjectiveOnClick, description)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1DB954))
                ) {
                    Text("Guardar", color = Color.White)
                }
            }
        },
        backgroundColor = Color(0xFF2C2C2C),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    )
}