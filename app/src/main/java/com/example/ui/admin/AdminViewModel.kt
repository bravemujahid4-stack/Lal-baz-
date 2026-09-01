package com.example.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MujahidRepository
import com.example.model.AppNotification
import com.example.model.BalanceType
import com.example.model.Customer
import com.example.model.MarketRates
import com.example.model.RateHistoryEntry
import com.example.util.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminDashboardStats(
    val totalCustomers: Int = 0,
    val activeCustomers: Int = 0,
    val totalReceivable: Double = 0.0, // GET
    val totalPayable: Double = 0.0,    // GIVE
    val netBalance: Double = 0.0
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MujahidRepository(application)
    private val sessionManager = SessionManager(application)

    val customersFlow: StateFlow<List<Customer>> = repository.getAllCustomersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentRatesFlow: StateFlow<MarketRates?> = repository.getCurrentRatesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rateHistoryFlow: StateFlow<List<RateHistoryEntry>> = repository.getAllRateHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationsFlow: StateFlow<List<AppNotification>> = repository.getAllNotificationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statsFlow: StateFlow<AdminDashboardStats> = customersFlow.map { list ->
        val activeCount = list.count { it.isActive }
        var sumReceivable = 0.0
        var sumPayable = 0.0
        list.forEach { c ->
            if (c.balanceType == BalanceType.RECEIVABLE) {
                sumReceivable += c.balance
            } else {
                sumPayable += c.balance
            }
        }
        AdminDashboardStats(
            totalCustomers = list.size,
            activeCustomers = activeCount,
            totalReceivable = sumReceivable,
            totalPayable = sumPayable,
            netBalance = sumReceivable - sumPayable
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminDashboardStats())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // Rate operations
    fun updateIndividualRate(itemIndex: Int, newRate: Double) {
        viewModelScope.launch {
            val result = repository.updateIndividualRate(itemIndex, newRate, sessionManager.getUserName() ?: "Admin")
            if (result.isSuccess) {
                _statusMessage.value = "Item $itemIndex rate updated successfully to Rs. $newRate"
            } else {
                _statusMessage.value = "Error updating rate: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun updateAllRates(item1: Double, item2: Double, item3: Double, item4: Double) {
        viewModelScope.launch {
            val result = repository.updateAllRates(item1, item2, item3, item4, sessionManager.getUserName() ?: "Admin")
            if (result.isSuccess) {
                _statusMessage.value = "All daily market rates published successfully!"
            } else {
                _statusMessage.value = "Error publishing rates: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun toggleMarketStatus(isOpen: Boolean) {
        viewModelScope.launch {
            val result = repository.setMarketStatus(isOpen, sessionManager.getUserName() ?: "Admin")
            if (result.isSuccess) {
                _statusMessage.value = if (isOpen) "Market marked as OPEN" else "Market marked as CLOSED"
            }
        }
    }

    // Customer operations
    fun addCustomer(
        name: String,
        username: String,
        password: String,
        phone: String,
        balance: Double,
        balanceType: BalanceType,
        hasCustomRates: Boolean,
        cItem1: Double?,
        cItem2: Double?,
        cItem3: Double?,
        cItem4: Double?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.addCustomer(
                name = name,
                username = username,
                plainPass = password,
                phone = phone,
                balance = balance,
                balanceType = balanceType,
                hasCustomRates = hasCustomRates,
                customRateItem1 = cItem1,
                customRateItem2 = cItem2,
                customRateItem3 = cItem3,
                customRateItem4 = cItem4
            )
            result.onSuccess {
                _statusMessage.value = "Customer '${it.name}' added successfully"
                onSuccess()
            }.onFailure {
                _statusMessage.value = it.message ?: "Failed to add customer"
            }
        }
    }

    fun updateCustomer(
        customer: Customer,
        newPassword: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.updateCustomer(customer, newPassword)
            result.onSuccess {
                _statusMessage.value = "Customer updated successfully"
                onSuccess()
            }.onFailure {
                _statusMessage.value = it.message ?: "Failed to update customer"
            }
        }
    }

    fun updateCustomerBalance(customerId: String, newBalance: Double, balanceType: BalanceType, note: String = "") {
        viewModelScope.launch {
            val result = repository.updateCustomerBalance(customerId, newBalance, balanceType, note)
            result.onSuccess {
                _statusMessage.value = "Customer balance updated successfully"
            }.onFailure {
                _statusMessage.value = it.message ?: "Failed to update balance"
            }
        }
    }

    fun toggleCustomerActive(customerId: String) {
        viewModelScope.launch {
            val result = repository.toggleCustomerActiveStatus(customerId)
            result.onSuccess { newStatus ->
                _statusMessage.value = if (newStatus) "Customer activated" else "Customer deactivated"
            }
        }
    }

    fun deleteCustomer(customerId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
            _statusMessage.value = "Customer deleted"
            onSuccess()
        }
    }

    fun sendBroadcastNotification(title: String, message: String) {
        viewModelScope.launch {
            repository.sendBroadcastNotification(title, message)
            _statusMessage.value = "Notification sent to all customers"
        }
    }
}
