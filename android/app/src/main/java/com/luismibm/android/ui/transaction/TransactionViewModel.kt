package com.luismibm.android.ui.transaction

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luismibm.android.api.ApiClient
import com.luismibm.android.models.CreateTransactionRequest
import com.luismibm.android.models.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionViewModel : ViewModel() {

    private val _transactions = mutableStateOf<List<Transaction>>(emptyList())
    val transactions: State<List<Transaction>> = _transactions

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _transactionToDelete = mutableStateOf<Transaction?>(null)
    val transactionToDelete: State<Transaction?> = _transactionToDelete

    private val _showDeleteDialog = mutableStateOf(false)
    val showDeleteDialog: State<Boolean> = _showDeleteDialog

    private val _showCreateTransactionDialog = mutableStateOf(false)
    val showCreateTransactionDialog: State<Boolean> = _showCreateTransactionDialog

    private val _isCreatingTransaction = mutableStateOf(false)
    val isCreatingTransaction: State<Boolean> = _isCreatingTransaction

    private val _transactionAmountInput = mutableStateOf("")
    val transactionAmountInput: State<String> = _transactionAmountInput

    private val _transactionCategoryInput = mutableStateOf("")
    val transactionCategoryInput: State<String> = _transactionCategoryInput

    private val _transactionObjectiveInput = mutableStateOf("")
    val transactionObjectiveInput: State<String> = _transactionObjectiveInput

    private val _transactionDescriptionInput = mutableStateOf("")
    val transactionDescriptionInput: State<String> = _transactionDescriptionInput

    private val _showDateFilterDialog = mutableStateOf(false)
    val showDateFilterDialog: State<Boolean> = _showDateFilterDialog

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private val _token = mutableStateOf("")
    private val _spaceId = mutableStateOf("")

    private val _startDateText = mutableStateOf("")
    val startDateText: State<String> = _startDateText

    private val _endDateText = mutableStateOf("")
    val endDateText: State<String> = _endDateText

    init {
        val defaultEndDate = calendar.time
        calendar.add(Calendar.MONTH, -1)
        val defaultStartDate = calendar.time

        _startDateText.value = dateFormat.format(defaultStartDate)
        _endDateText.value = dateFormat.format(defaultEndDate)
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

    fun onStartDateTextChange(date: String) {
        _startDateText.value = date
    }

    fun onEndDateTextChange(date: String) {
        _endDateText.value = date
    }

    fun toggleCreateTransactionDialog(show: Boolean) {
        _showCreateTransactionDialog.value = show
        if (!show) {
            resetTransactionInputs()
        }
    }

    fun toggleDateFilterDialog(show: Boolean) {
        _showDateFilterDialog.value = show
    }

    fun setTransactionToDelete(transaction: Transaction?) {
        _transactionToDelete.value = transaction
        _showDeleteDialog.value = transaction != null
    }

    fun resetTransactionInputs() {
        _transactionAmountInput.value = ""
        _transactionCategoryInput.value = ""
        _transactionObjectiveInput.value = ""
        _transactionDescriptionInput.value = ""
    }

    fun loadTransactions(token: String?, spaceId: String?, onError: (String) -> Unit) {
        if (token == null || spaceId == null) {
            if (token == null) onError("Token no disponible. Por favor, inicia sesión de nuevo.")
            if (spaceId == null) onError("Por favor, selecciona un espacio para ver las transacciones.")
            _isLoading.value = false
            return
        }

        _token.value = token
        _spaceId.value = spaceId

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allTransactions = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getTransactionsBySpace("Bearer $token", spaceId)
                }

                val startDate = dateFormat.parse(_startDateText.value)
                val endDate = dateFormat.parse(_endDateText.value)

                val calendar = Calendar.getInstance()
                calendar.time = endDate
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val adjustedEndDate = calendar.time

                _transactions.value = allTransactions.filter {
                    it.date >= startDate && it.date < adjustedEndDate
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error loading transactions", e)
                onError("Error al cargar las transacciones: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyDateFilter(onError: (String) -> Unit) {
        try {
            dateFormat.parse(_startDateText.value)
            dateFormat.parse(_endDateText.value)
            _showDateFilterDialog.value = false
            loadTransactions(_token.value, _spaceId.value, onError)
        } catch (e: Exception) {
            onError("Formato de fecha inválido. Use YYYY-MM-DD")
        }
    }

    fun createTransaction(
        amount: Float,
        category: String,
        objective: String,
        description: String,
        token: String?,
        spaceId: String?,
        onError: (String) -> Unit
    ) {
        if (token == null || spaceId == null) {
            onError("Token o Space ID no disponibles.")
            return
        }

        _isCreatingTransaction.value = true
        viewModelScope.launch {
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

                val createdTransaction = withContext(Dispatchers.IO) {
                    ApiClient.apiService.createTransaction("Bearer $token", newTransactionRequest)
                }

                // Añadir la nueva transacción al principio de la lista
                _transactions.value = listOf(createdTransaction) + _transactions.value

                resetTransactionInputs()
                _showCreateTransactionDialog.value = false
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error creating transaction", e)
                onError("Error al crear transacción: ${e.message}")
            } finally {
                _isCreatingTransaction.value = false
            }
        }
    }

    fun deleteTransaction(token: String?, onError: (String) -> Unit) {
        val transaction = _transactionToDelete.value ?: return

        if (token == null) {
            onError("Token no disponible para eliminar.")
            return
        }

        viewModelScope.launch {
            try {
                val response: Response<Void> = withContext(Dispatchers.IO) {
                    ApiClient.apiService.deleteTransaction("Bearer $token", transaction.id)
                }

                if (response.isSuccessful) {
                    _transactions.value = _transactions.value.filterNot { it.id == transaction.id }
                } else {
                    onError("Error al eliminar transacción: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error deleting transaction", e)
                onError("Error al eliminar transacción: ${e.message}")
            } finally {
                _showDeleteDialog.value = false
                _transactionToDelete.value = null
            }
        }
    }
}