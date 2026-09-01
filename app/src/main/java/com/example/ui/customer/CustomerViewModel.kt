package com.example.ui.customer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MujahidRepository
import com.example.model.AppNotification
import com.example.model.Customer
import com.example.model.MarketRates
import com.example.model.RateHistoryEntry
import com.example.util.NetworkMonitor
import com.example.util.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EffectiveItemRate(
    val itemIndex: Int,
    val itemName: String,
    val currentRate: Double,
    val previousRate: Double,
    val isCustomRate: Boolean
)

class CustomerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MujahidRepository(application)
    private val sessionManager = SessionManager(application)
    private val networkMonitor = NetworkMonitor(application)

    val currentCustomerId: String = sessionManager.getUserId() ?: ""

    val customerFlow: StateFlow<Customer?> = if (currentCustomerId.isNotBlank()) {
        repository.getCustomerByIdFlow(currentCustomerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        MutableStateFlow(null)
    }

    val currentRatesFlow: StateFlow<MarketRates?> = repository.getCurrentRatesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rateHistoryFlow: StateFlow<List<RateHistoryEntry>> = repository.getAllRateHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationsFlow: StateFlow<List<AppNotification>> = if (currentCustomerId.isNotBlank()) {
        repository.getNotificationsForCustomerFlow(currentCustomerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } else {
        MutableStateFlow(emptyList())
    }

    val isOnlineFlow: StateFlow<Boolean> = networkMonitor.isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _isAccountBlocked = MutableStateFlow(false)
    val isAccountBlocked: StateFlow<Boolean> = _isAccountBlocked.asStateFlow()

    init {
        // Monitor account active status
        viewModelScope.launch {
            customerFlow.collect { customer ->
                if (customer != null && !customer.isActive) {
                    _isAccountBlocked.value = true
                    sessionManager.clearSession()
                }
            }
        }
    }

    fun getEffectiveRates(customer: Customer?, marketRates: MarketRates?): List<EffectiveItemRate> {
        val rates = marketRates ?: MarketRates(date = "", item1 = 0.0, item2 = 0.0, item3 = 0.0, item4 = 0.0)
        val hasCustom = customer?.hasCustomRates == true

        val r1 = if (hasCustom && customer?.customRateItem1 != null) customer.customRateItem1 else rates.item1
        val r2 = if (hasCustom && customer?.customRateItem2 != null) customer.customRateItem2 else rates.item2
        val r3 = if (hasCustom && customer?.customRateItem3 != null) customer.customRateItem3 else rates.item3
        val r4 = if (hasCustom && customer?.customRateItem4 != null) customer.customRateItem4 else rates.item4

        return listOf(
            EffectiveItemRate(1, "Item 1", r1, rates.previousItem1, hasCustom && customer?.customRateItem1 != null),
            EffectiveItemRate(2, "Item 2", r2, rates.previousItem2, hasCustom && customer?.customRateItem2 != null),
            EffectiveItemRate(3, "Item 3", r3, rates.previousItem3, hasCustom && customer?.customRateItem3 != null),
            EffectiveItemRate(4, "Item 4", r4, rates.previousItem4, hasCustom && customer?.customRateItem4 != null)
        )
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
