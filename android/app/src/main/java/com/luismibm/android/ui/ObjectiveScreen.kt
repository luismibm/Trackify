package com.luismibm.android.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mikephil.charting.data.PieEntry
import com.luismibm.android.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ObjectiveScreen(
    modifier: Modifier = Modifier,
    token: String?,
    spaceId: String?,
    onError: (String) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var objectivePieData by remember { mutableStateOf<List<PieEntry>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scaffoldState = rememberScaffoldState()

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    
    val defaultEndDate = calendar.time
    calendar.add(Calendar.MONTH, -1)
    val defaultStartDate = calendar.time
    
    var startDateText by remember { mutableStateOf(dateFormat.format(defaultStartDate)) }
    var endDateText by remember { mutableStateOf(dateFormat.format(defaultEndDate)) }
    var showDateFilterDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    LaunchedEffect(token, spaceId, startDateText, endDateText) {
        if (token != null && spaceId != null) {
            isLoading = true
            errorMessage = null
            try {
                val allTransactions = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getTransactionsBySpace("Bearer $token", spaceId)
                }
                
                val startDate = dateFormat.parse(startDateText)
                val endDate = dateFormat.parse(endDateText)
                
                val calendar = Calendar.getInstance()
                calendar.time = endDate
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val adjustedEndDate = calendar.time
                
                val filteredTransactions = allTransactions.filter {
                    it.date >= startDate && it.date < adjustedEndDate
                }

                val expensesByObjective = mutableMapOf<String, Double>()
                filteredTransactions.forEach { transaction ->
                    if (transaction.amount < 0) {
                        val objective = transaction.objective ?: "None"
                        expensesByObjective[objective] = (expensesByObjective[objective] ?: 0.0) + transaction.amount
                    }
                }

                val pieEntries = expensesByObjective
                    .map { (objective, amount) -> PieEntry(kotlin.math.abs(amount.toFloat()), objective) }
                    .filter { it.value > 0 }

                objectivePieData = pieEntries
                if (pieEntries.isEmpty()) {
                    Log.d("ObjectiveScreen", "No hay datos de gastos por objetivo para mostrar en la gráfica.")
                }

            } catch (e: Exception) {
                errorMessage = "Error al cargar datos para objetivos: ${e.message}"
                Log.e("ObjectiveScreen", "Error al cargar datos para objetivos", e)
                onError("Error al cargar datos para objetivos: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
            if (token == null) onError("Token no disponible.")
            if (spaceId == null) onError("Space ID no disponible. Por favor, selecciona un espacio.")
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
                            isLoading = true // Recargar datos con las nuevas fechas
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Formato de fecha inválido. Use YYYY-MM-DD", android.widget.Toast.LENGTH_SHORT).show()
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

    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mostrar rango de fechas
            Text(
                text = "Periodo: $startDateText a $endDateText",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
            
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(color = Color(0xFF1DB954))
            } else if (objectivePieData.isNotEmpty()) {
                ObjectivePercentageChart(entries = objectivePieData)
            } else {
                Text(
                    text = "No hay datos de gastos por objetivo para mostrar en este periodo.",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ObjectivePercentageChart(entries: List<PieEntry>) {
    CategoryPercentageChart(entries = entries)
} 