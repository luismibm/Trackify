package com.luismibm.android.ui.home

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import com.luismibm.android.api.ApiClient
import com.luismibm.android.models.CreateTransactionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeViewModel : ViewModel() {

    private val _token = mutableStateOf("")
    private val _spaceId = mutableStateOf("")

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _totalIncome = mutableStateOf(0.0)
    val totalIncome: State<Double> = _totalIncome

    private val _totalExpenses = mutableStateOf(0.0)
    val totalExpenses: State<Double> = _totalExpenses

    private val _balance = mutableStateOf(0.0)
    val balance: State<Double> = _balance

    private val _categoryPieData = mutableStateOf<List<PieEntry>>(emptyList())
    val categoryPieData: State<List<PieEntry>> = _categoryPieData

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _startDateText = mutableStateOf("")
    private val _endDateText = mutableStateOf("")

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private val _showCreateTransactionDialog = mutableStateOf(false)
    val showCreateTransactionDialog: State<Boolean> = _showCreateTransactionDialog

    private val _transactionAmountInput = mutableStateOf("")
    val transactionAmountInput: State<String> = _transactionAmountInput

    private val _transactionCategoryInput = mutableStateOf("")
    val transactionCategoryInput: State<String> = _transactionCategoryInput

    private val _transactionObjectiveInput = mutableStateOf("")
    val transactionObjectiveInput: State<String> = _transactionObjectiveInput

    private val _transactionDescriptionInput = mutableStateOf("")
    val transactionDescriptionInput: State<String> = _transactionDescriptionInput


    fun toggleCreateTransactionDialog(show: Boolean) {
        _showCreateTransactionDialog.value = show
        if (!show) {
            resetTransactionInputs()
        }
    }

    fun onTransactionAmountInputChange(amount: String) {
        _transactionAmountInput.value = amount
    }

    fun onTransactionCategoryInputChange(category: String) {
        _transactionCategoryInput.value = category
    }

    fun onTransactionObjectiveInputChange(objective: String) {
        _transactionObjectiveInput.value = objective
    }

    fun onTransactionDescriptionInputChange(description: String) {
        _transactionDescriptionInput.value = description
    }

    fun resetTransactionInputs() {
        _transactionAmountInput.value = ""
        _transactionCategoryInput.value = ""
        _transactionObjectiveInput.value = ""
        _transactionDescriptionInput.value = ""
    }

    fun loadData(token: String? = null, spaceId: String? = null, startDateStr: String, endDateStr: String) {
        if (token.isNullOrBlank() || spaceId.isNullOrBlank()) {
            _errorMessage.value = "Token o Space ID no disponibles."
            _isLoading.value = false
            return
        }

        _token.value = token
        _spaceId.value = spaceId

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val allTransactions = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getTransactionsBySpace("Bearer $token", spaceId)
                }

                val startDate = dateFormat.parse(startDateStr)
                val endDate = dateFormat.parse(endDateStr)

                val calendar = Calendar.getInstance()
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

                _totalIncome.value = income
                _totalExpenses.value = expenses
                _balance.value = income + expenses

                val pieEntries = expensesByCategory
                    .filter { it.value < 0 }
                    .map { (category, amount) -> PieEntry(kotlin.math.abs(amount.toFloat()), category) }
                    .filter { it.value > 0 }

                _categoryPieData.value = pieEntries

            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar datos: ${e.message}"
                Log.e("HomeViewModel", "Error al cargar datos", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTransaction(
        token: String,
        spaceId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val amountFloat = _transactionAmountInput.value.toFloatOrNull()
        if (amountFloat == null) {
            onError("Importe inválido")
            return
        }

        if (_transactionCategoryInput.value.isBlank()) {
            onError("La categoría no puede estar vacía")
            return
        }

        if (_transactionDescriptionInput.value.isBlank()) {
            onError("La descripción no puede estar vacía")
            return
        }

        viewModelScope.launch {
            try {
                val currentUser = ApiClient.apiService.getCurrentUser("Bearer $token")
                val userId = currentUser.id

                val finalObjective = if (_transactionObjectiveInput.value.isBlank()) "None" else _transactionObjectiveInput.value

                val newTransactionRequest = CreateTransactionRequest(
                    amount = amountFloat,
                    category = _transactionCategoryInput.value,
                    objective = finalObjective,
                    userId = userId,
                    spaceId = spaceId,
                    date = null,
                    description = _transactionDescriptionInput.value
                )

                val createdTransaction = withContext(Dispatchers.IO) {
                    ApiClient.apiService.createTransaction("Bearer $token", newTransactionRequest)
                }

                resetTransactionInputs()
                _showCreateTransactionDialog.value = false
                onSuccess("Transacción creada: ${createdTransaction.category} ${createdTransaction.amount}")
                loadData(token, spaceId, _startDateText.value, _endDateText.value)

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al crear transacción: ${e.message}", e)
                onError("Error al crear transacción: ${e.message}")
            }
        }
    }
}