package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.BalanceType
import com.example.model.Customer
import com.example.model.MarketRates
import com.example.model.RateHistoryEntry
import com.example.model.AppNotification
import com.example.model.NotificationType
import com.example.model.AdminUser

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val username: String,
    val passwordHash: String,
    val phone: String = "",
    val balance: Double = 0.0,
    val balanceType: String = BalanceType.RECEIVABLE.name,
    val isActive: Boolean = true,
    val hasCustomRates: Boolean = false,
    val customRateItem1: Double? = null,
    val customRateItem2: Double? = null,
    val customRateItem3: Double? = null,
    val customRateItem4: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Customer = Customer(
        id = id,
        name = name,
        username = username,
        passwordHash = passwordHash,
        phone = phone,
        balance = balance,
        balanceType = try { BalanceType.valueOf(balanceType) } catch (_: Exception) { BalanceType.RECEIVABLE },
        isActive = isActive,
        hasCustomRates = hasCustomRates,
        customRateItem1 = customRateItem1,
        customRateItem2 = customRateItem2,
        customRateItem3 = customRateItem3,
        customRateItem4 = customRateItem4,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(customer: Customer): CustomerEntity = CustomerEntity(
            id = customer.id,
            name = customer.name,
            username = customer.username,
            passwordHash = customer.passwordHash,
            phone = customer.phone,
            balance = customer.balance,
            balanceType = customer.balanceType.name,
            isActive = customer.isActive,
            hasCustomRates = customer.hasCustomRates,
            customRateItem1 = customer.customRateItem1,
            customRateItem2 = customer.customRateItem2,
            customRateItem3 = customer.customRateItem3,
            customRateItem4 = customer.customRateItem4,
            createdAt = customer.createdAt,
            updatedAt = customer.updatedAt
        )
    }
}

@Entity(tableName = "market_rates")
data class MarketRatesEntity(
    @PrimaryKey val id: String = "current",
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
) {
    fun toDomain(): MarketRates = MarketRates(
        id = id,
        date = date,
        item1 = item1,
        item2 = item2,
        item3 = item3,
        item4 = item4,
        isMarketOpen = isMarketOpen,
        previousItem1 = previousItem1,
        previousItem2 = previousItem2,
        previousItem3 = previousItem3,
        previousItem4 = previousItem4,
        updatedTime = updatedTime,
        updatedBy = updatedBy
    )

    companion object {
        fun fromDomain(domain: MarketRates): MarketRatesEntity = MarketRatesEntity(
            id = domain.id,
            date = domain.date,
            item1 = domain.item1,
            item2 = domain.item2,
            item3 = domain.item3,
            item4 = domain.item4,
            isMarketOpen = domain.isMarketOpen,
            previousItem1 = domain.previousItem1,
            previousItem2 = domain.previousItem2,
            previousItem3 = domain.previousItem3,
            previousItem4 = domain.previousItem4,
            updatedTime = domain.updatedTime,
            updatedBy = domain.updatedBy
        )
    }
}

@Entity(tableName = "rate_history")
data class RateHistoryEntity(
    @PrimaryKey val id: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val item1: Double,
    val item2: Double,
    val item3: Double,
    val item4: Double,
    val isMarketOpen: Boolean = true,
    val updatedBy: String = "Admin",
    val note: String = ""
) {
    fun toDomain(): RateHistoryEntry = RateHistoryEntry(
        id = id,
        date = date,
        timestamp = timestamp,
        item1 = item1,
        item2 = item2,
        item3 = item3,
        item4 = item4,
        isMarketOpen = isMarketOpen,
        updatedBy = updatedBy,
        note = note
    )

    companion object {
        fun fromDomain(entry: RateHistoryEntry): RateHistoryEntity = RateHistoryEntity(
            id = entry.id,
            date = entry.date,
            timestamp = entry.timestamp,
            item1 = entry.item1,
            item2 = entry.item2,
            item3 = entry.item3,
            item4 = entry.item4,
            isMarketOpen = entry.isMarketOpen,
            updatedBy = entry.updatedBy,
            note = entry.note
        )
    }
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val customerId: String? = null,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = NotificationType.SYSTEM_ALERT.name
) {
    fun toDomain(): AppNotification = AppNotification(
        id = id,
        customerId = customerId,
        title = title,
        message = message,
        timestamp = timestamp,
        isRead = isRead,
        type = try { NotificationType.valueOf(type) } catch (_: Exception) { NotificationType.SYSTEM_ALERT }
    )

    companion object {
        fun fromDomain(notification: AppNotification): NotificationEntity = NotificationEntity(
            id = notification.id,
            customerId = notification.customerId,
            title = notification.title,
            message = notification.message,
            timestamp = notification.timestamp,
            isRead = notification.isRead,
            type = notification.type.name
        )
    }
}

@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey val id: String,
    val name: String,
    val username: String,
    val passwordHash: String,
    val email: String = "",
    val lastLogin: Long = System.currentTimeMillis()
) {
    fun toDomain(): AdminUser = AdminUser(
        id = id,
        name = name,
        username = username,
        passwordHash = passwordHash,
        email = email,
        lastLogin = lastLogin
    )

    companion object {
        fun fromDomain(admin: AdminUser): AdminEntity = AdminEntity(
            id = admin.id,
            name = admin.name,
            username = admin.username,
            passwordHash = admin.passwordHash,
            email = admin.email,
            lastLogin = admin.lastLogin
        )
    }
}
