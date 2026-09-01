package com.example.model

enum class BalanceType {
    RECEIVABLE, // GET -> Customer has to receive money (Red)
    PAYABLE     // GIVE -> Customer has to pay money (Green)
}

enum class MarketStatus {
    OPEN,
    CLOSED
}

enum class NotificationType {
    RATE_UPDATE,
    MARKET_STATUS,
    BALANCE_UPDATE,
    SYSTEM_ALERT
}

data class Customer(
    val id: String,
    val name: String,
    val username: String,
    val passwordHash: String,
    val phone: String = "",
    val balance: Double = 0.0,
    val balanceType: BalanceType = BalanceType.RECEIVABLE,
    val isActive: Boolean = true,
    val hasCustomRates: Boolean = false,
    val customRateItem1: Double? = null,
    val customRateItem2: Double? = null,
    val customRateItem3: Double? = null,
    val customRateItem4: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class MarketRates(
    val id: String = "current",
    val date: String,
    val item1: Double,
    val item2: Double,
    val item3: Double,
    val item4: Double,
    val isMarketOpen: Boolean = true,
    val previousItem1: Double = item1,
    val previousItem2: Double = item2,
    val previousItem3: Double = item3,
    val previousItem4: Double = item4,
    val updatedTime: Long = System.currentTimeMillis(),
    val updatedBy: String = "Admin"
)

data class RateHistoryEntry(
    val id: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val item1: Double,
    val item2: Double,
    val item3: Double,
    val item4: Double,
    val isMarketOpen: Boolean = true,
    val updatedBy: String = "Admin",
    val note: String = ""
)

data class AppNotification(
    val id: String,
    val customerId: String? = null, // null means broadcast to all customers
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.SYSTEM_ALERT
)

data class AdminUser(
    val id: String,
    val name: String,
    val username: String,
    val passwordHash: String,
    val email: String = "",
    val lastLogin: Long = System.currentTimeMillis()
)
