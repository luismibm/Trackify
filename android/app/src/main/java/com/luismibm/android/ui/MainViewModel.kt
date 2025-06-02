package com.luismibm.android.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _startDate = MutableStateFlow<Date>(getDefaultStartDate())
    val startDate: StateFlow<Date> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Date>(Calendar.getInstance().time)
    val endDate: StateFlow<Date> = _endDate.asStateFlow()

    private val _startDateText = MutableStateFlow(dateFormat.format(_startDate.value))
    val startDateText: StateFlow<String> = _startDateText.asStateFlow()

    private val _endDateText = MutableStateFlow(dateFormat.format(_endDate.value))
    val endDateText: StateFlow<String> = _endDateText.asStateFlow()

    private val _dateFilterChanged = MutableStateFlow(0)
    val dateFilterChanged: StateFlow<Int> = _dateFilterChanged.asStateFlow()

    private fun getDefaultStartDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        return calendar.time
    }

    fun updateDateFilter(startDate: String, endDate: String) {
        try {
            val startDateParsed = dateFormat.parse(startDate)
            val endDateParsed = dateFormat.parse(endDate)

            if (startDateParsed != null && endDateParsed != null) {
                _startDate.value = startDateParsed
                _endDate.value = endDateParsed
                _startDateText.value = startDate
                _endDateText.value = endDate
                _dateFilterChanged.value = _dateFilterChanged.value + 1
            }
        } catch (e: Exception) {
            Log.d("MainViewModel", "Invalid Date")
        }
    }

    fun getDateFormat(): SimpleDateFormat = dateFormat

}