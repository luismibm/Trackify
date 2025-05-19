package com.luismibm.android.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mikephil.charting.data.PieEntry
import com.luismibm.android.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    LaunchedEffect(key1 = token, key2 = spaceId) {
        if (token != null && spaceId != null) {
            isLoading = true
            errorMessage = null
            try {
                val transactions = withContext(Dispatchers.IO) {
                    RetrofitClient.authService.getTransactionsBySpace("Bearer $token", spaceId)
                }

                val expensesByObjective = mutableMapOf<String, Double>()
                transactions.forEach { transaction ->
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

    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = Color.Black,
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (objectivePieData.isNotEmpty()) {
                ObjectivePercentageChart(entries = objectivePieData)
            } else {
                Text(
                    text = "No hay datos de gastos por objetivo para mostrar.",
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