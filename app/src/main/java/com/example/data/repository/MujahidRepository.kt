package com.example.data.repository

import android.content.Context
import com.example.data.local.AdminEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CustomerEntity
import com.example.data.local.MarketRatesEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.RateHistoryEntity
import com.example.model.AdminUser
import com.example.model.AppNotification
import com.example.model.BalanceType
import com.example.model.Customer
import com.example.model.MarketRates
import com.example.model.NotificationType
import com.example.model.RateHistoryEntry
import com.example.util.FormatUtils
import com.example.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MujahidRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val customerDao = database.customerDao()
    private val marketRatesDao = database.marketRatesDao()
    private val rateHistoryDao = database.rateHistoryDao()
    private val notificationDao = database.notificationDao()
    private val adminDao = database.adminDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        // Seed Master Admin if not present
        if (adminDao.getAdminCount() == 0) {
            val masterAdmin = AdminUser(
                id = "admin_master_1",
                name = "Mujahid Accounts Admin",
                username = "admin",
                passwordHash = SecurityUtils.hashPassword("admin123"),
                email = "admin@mujahidaccounts.com"
            )
            adminDao.insertAdmin(AdminEntity.fromDomain(masterAdmin))
        }

        // Seed Initial Market Rates if not present
        if (marketRatesDao.getCurrentRates() == null) {
            val initialRates = MarketRates(
                id = "current",
                date = FormatUtils.formatDateOnly(),
                item1 = 295.0,
                item2 = 300.0,
                item3 = 305.0,
                item4 = 310.0,
                isMarketOpen = true,
                previousItem1 = 292.0,
                previousItem2 = 298.0,
                previousItem3 = 303.0,
                previousItem4 = 308.0,
                updatedTime = System.currentTimeMillis(),
                updatedBy = "Admin"
            )
            marketRatesDao.setMarketRates(MarketRatesEntity.fromDomain(initialRates))

            // Seed initial history entries for clean startup display
            val history1 = RateHistoryEntry(
                id = UUID.randomUUID().toString(),
                date = FormatUtils.formatDateOnly(System.currentTimeMillis()),
                timestamp = System.currentTimeMillis(),
                item1 = 295.0,
                item2 = 300.0,
                item3 = 305.0,
                item4 = 310.0,
                isMarketOpen = true,
                note = "Daily morning rate published"
            )
            val history2 = RateHistoryEntry(
                id = UUID.randomUUID().toString(),
                date = FormatUtils.formatDateOnly(System.currentTimeMillis() - 86400000L),
                timestamp = System.currentTimeMillis() - 86400000L,
                item1 = 292.0,
                item2 = 298.0,
                item3 = 303.0,
                item4 = 308.0,
                isMarketOpen = true,
                note = "Standard closing rate"
            )
            val history3 = RateHistoryEntry(
                id = UUID.randomUUID().toString(),
                date = FormatUtils.formatDateOnly(System.currentTimeMillis() - 172800000L),
                timestamp = System.currentTimeMillis() - 172800000L,
                item1 = 290.0,
                item2 = 295.0,
                item3 = 300.0,
                item4 = 305.0,
                isMarketOpen = false,
                note = "Market Holiday - Closed"
            )
            rateHistoryDao.insertHistoryList(listOf(history1, history2, history3).map { RateHistoryEntity.fromDomain(it) })
        }
    }

    // ==================== AUTHENTICATION ====================

    suspend fun authenticateAdmin(username: String, plainPass: String): AdminUser? = withContext(Dispatchers.IO) {
        val admin = adminDao.getAdminByUsername(username.trim()) ?: return@withContext null
        if (SecurityUtils.verifyPassword(plainPass, admin.passwordHash)) {
            admin.toDomain()
        } else {
            null
        }
    }

    suspend fun authenticateCustomer(username: String, plainPass: String): Result<Customer> = withContext(Dispatchers.IO) {
        val customerEntity = customerDao.getCustomerByUsername(username.trim().lowercase())
            ?: return@withContext Result.failure(Exception("Customer username not found"))

        if (!customerEntity.isActive) {
            return@withContext Result.failure(Exception("Your account is deactivated by Admin. Please contact office."))
        }

        if (SecurityUtils.verifyPassword(plainPass, customerEntity.passwordHash)) {
            Result.success(customerEntity.toDomain())
        } else {
            Result.failure(Exception("Invalid password. Please check your credentials."))
        }
    }

    suspend fun getCustomerById(id: String): Customer? = withContext(Dispatchers.IO) {
        customerDao.getCustomerById(id)?.toDomain()
    }

    fun getCustomerByIdFlow(id: String): Flow<Customer?> {
        return customerDao.getCustomerByIdFlow(id).map { it?.toDomain() }
    }

    // ==================== CUSTOMER MANAGEMENT ====================

    fun getAllCustomersFlow(): Flow<List<Customer>> {
        return customerDao.getAllCustomersFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun addCustomer(
        name: String,
        username: String,
        plainPass: String,
        phone: String,
        balance: Double,
        balanceType: BalanceType,
        hasCustomRates: Boolean = false,
        customRateItem1: Double? = null,
        customRateItem2: Double? = null,
        customRateItem3: Double? = null,
        customRateItem4: Double? = null
    ): Result<Customer> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        if (customerDao.getCustomerByUsername(cleanUsername) != null) {
            return@withContext Result.failure(Exception("Username '$cleanUsername' already exists. Please choose a unique username."))
        }

        val newCustomer = Customer(
            id = "cust_${System.currentTimeMillis()}_${(1000..9999).random()}",
            name = name.trim(),
            username = cleanUsername,
            passwordHash = SecurityUtils.hashPassword(plainPass),
            phone = phone.trim(),
            balance = balance,
            balanceType = balanceType,
            isActive = true,
            hasCustomRates = hasCustomRates,
            customRateItem1 = customRateItem1,
            customRateItem2 = customRateItem2,
            customRateItem3 = customRateItem3,
            customRateItem4 = customRateItem4,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        customerDao.insertCustomer(CustomerEntity.fromDomain(newCustomer))

        // Also create welcome notification for the customer
        val notif = AppNotification(
            id = UUID.randomUUID().toString(),
            customerId = newCustomer.id,
            title = "Welcome to Mujahid Accounts",
            message = "Your account for ${newCustomer.name} has been created successfully. Track your balance and daily market rates here.",
            timestamp = System.currentTimeMillis(),
            type = NotificationType.SYSTEM_ALERT
        )
        notificationDao.insertNotification(NotificationEntity.fromDomain(notif))

        Result.success(newCustomer)
    }

    suspend fun updateCustomer(customer: Customer, newPlainPassword: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = customerDao.getCustomerById(customer.id)
            ?: return@withContext Result.failure(Exception("Customer not found"))

        val finalPasswordHash = if (!newPlainPassword.isNullOrBlank()) {
            SecurityUtils.hashPassword(newPlainPassword)
        } else {
            existing.passwordHash
        }

        val updated = customer.copy(
            passwordHash = finalPasswordHash,
            updatedAt = System.currentTimeMillis()
        )
        customerDao.updateCustomer(CustomerEntity.fromDomain(updated))
        Result.success(Unit)
    }

    suspend fun updateCustomerBalance(
        customerId: String,
        newBalance: Double,
        newBalanceType: BalanceType,
        note: String = ""
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = customerDao.getCustomerById(customerId)
            ?: return@withContext Result.failure(Exception("Customer not found"))

        val updated = existing.copy(
            balance = newBalance,
            balanceType = newBalanceType.name,
            updatedAt = System.currentTimeMillis()
        )
        customerDao.updateCustomer(updated)

        // Create balance update notification
        val notif = AppNotification(
            id = UUID.randomUUID().toString(),
            customerId = customerId,
            title = "Balance Updated",
            message = "Your balance is now ${FormatUtils.formatPkr(newBalance)} (${newBalanceType.name}). ${if (note.isNotBlank()) "Note: $note" else ""}",
            timestamp = System.currentTimeMillis(),
            type = NotificationType.BALANCE_UPDATE
        )
        notificationDao.insertNotification(NotificationEntity.fromDomain(notif))

        Result.success(Unit)
    }

    suspend fun toggleCustomerActiveStatus(customerId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val existing = customerDao.getCustomerById(customerId)
            ?: return@withContext Result.failure(Exception("Customer not found"))
        val newStatus = !existing.isActive
        val updated = existing.copy(isActive = newStatus, updatedAt = System.currentTimeMillis())
        customerDao.updateCustomer(updated)
        Result.success(newStatus)
    }

    suspend fun deleteCustomer(customerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        customerDao.deleteCustomerById(customerId)
        Result.success(Unit)
    }

    // ==================== DAILY MARKET RATES ====================

    fun getCurrentRatesFlow(): Flow<MarketRates?> {
        return marketRatesDao.getCurrentRatesFlow().map { it?.toDomain() }
    }

    suspend fun getCurrentRates(): MarketRates? = withContext(Dispatchers.IO) {
        marketRatesDao.getCurrentRates()?.toDomain()
    }

    suspend fun updateIndividualRate(
        itemIndex: Int, // 1, 2, 3, or 4
        newRate: Double,
        updatedBy: String = "Admin"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val current = getCurrentRates() ?: MarketRates(
            date = FormatUtils.formatDateOnly(),
            item1 = 0.0, item2 = 0.0, item3 = 0.0, item4 = 0.0
        )

        val updated = when (itemIndex) {
            1 -> current.copy(
                previousItem1 = current.item1,
                item1 = newRate,
                updatedTime = System.currentTimeMillis(),
                updatedBy = updatedBy
            )
            2 -> current.copy(
                previousItem2 = current.item2,
                item2 = newRate,
                updatedTime = System.currentTimeMillis(),
                updatedBy = updatedBy
            )
            3 -> current.copy(
                previousItem3 = current.item3,
                item3 = newRate,
                updatedTime = System.currentTimeMillis(),
                updatedBy = updatedBy
            )
            4 -> current.copy(
                previousItem4 = current.item4,
                item4 = newRate,
                updatedTime = System.currentTimeMillis(),
                updatedBy = updatedBy
            )
            else -> return@withContext Result.failure(Exception("Invalid item index $itemIndex"))
        }

        marketRatesDao.setMarketRates(MarketRatesEntity.fromDomain(updated))

        // Save history entry
        val history = RateHistoryEntry(
            id = UUID.randomUUID().toString(),
            date = FormatUtils.formatDateOnly(),
            timestamp = System.currentTimeMillis(),
            item1 = updated.item1,
            item2 = updated.item2,
            item3 = updated.item3,
            item4 = updated.item4,
            isMarketOpen = updated.isMarketOpen,
            updatedBy = updatedBy,
            note = "Item $itemIndex updated to ${FormatUtils.formatPkr(newRate)}"
        )
        rateHistoryDao.insertHistory(RateHistoryEntity.fromDomain(history))

        // Broadcast rate update notification
        val notif = AppNotification(
            id = UUID.randomUUID().toString(),
            customerId = null,
            title = "Market Rates Updated",
            message = "Item $itemIndex rate has been updated to ${FormatUtils.formatPkr(newRate)}.",
            timestamp = System.currentTimeMillis(),
            type = NotificationType.RATE_UPDATE
        )
        notificationDao.insertNotification(NotificationEntity.fromDomain(notif))

        Result.success(Unit)
    }

    suspend fun updateAllRates(
        item1: Double,
        item2: Double,
        item3: Double,
        item4: Double,
        updatedBy: String = "Admin"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val current = getCurrentRates() ?: MarketRates(
            date = FormatUtils.formatDateOnly(),
            item1 = item1, item2 = item2, item3 = item3, item4 = item4
        )

        val updated = current.copy(
            date = FormatUtils.formatDateOnly(),
            previousItem1 = current.item1,
            previousItem2 = current.item2,
            previousItem3 = current.item3,
            previousItem4 = current.item4,
            item1 = item1,
            item2 = item2,
            item3 = item3,
            item4 = item4,
            updatedTime = System.currentTimeMillis(),
            updatedBy = updatedBy
        )

        marketRatesDao.setMarketRates(MarketRatesEntity.fromDomain(updated))

        // Add history entry
        val history = RateHistoryEntry(
            id = UUID.randomUUID().toString(),
            date = FormatUtils.formatDateOnly(),
            timestamp = System.currentTimeMillis(),
            item1 = item1,
            item2 = item2,
            item3 = item3,
            item4 = item4,
            isMarketOpen = updated.isMarketOpen,
            updatedBy = updatedBy,
            note = "Today's daily market rates published"
        )
        rateHistoryDao.insertHistory(RateHistoryEntity.fromDomain(history))

        // Broadcast notification
        val notif = AppNotification(
            id = UUID.randomUUID().toString(),
            customerId = null,
            title = "Today's Market Rates Updated",
            message = "New daily market rates: Item 1: ${FormatUtils.formatPkr(item1)}, Item 2: ${FormatUtils.formatPkr(item2)}, Item 3: ${FormatUtils.formatPkr(item3)}, Item 4: ${FormatUtils.formatPkr(item4)}.",
            timestamp = System.currentTimeMillis(),
            type = NotificationType.RATE_UPDATE
        )
        notificationDao.insertNotification(NotificationEntity.fromDomain(notif))

        Result.success(Unit)
    }

    suspend fun setMarketStatus(isOpen: Boolean, updatedBy: String = "Admin"): Result<Unit> = withContext(Dispatchers.IO) {
        val current = getCurrentRates() ?: return@withContext Result.failure(Exception("No rates found"))
        val updated = current.copy(
            isMarketOpen = isOpen,
            updatedTime = System.currentTimeMillis(),
            updatedBy = updatedBy
        )
        marketRatesDao.setMarketRates(MarketRatesEntity.fromDomain(updated))

        // Create history log for market open/close
        val history = RateHistoryEntry(
            id = UUID.randomUUID().toString(),
            date = FormatUtils.formatDateOnly(),
            timestamp = System.currentTimeMillis(),
            item1 = current.item1,
            item2 = current.item2,
            item3 = current.item3,
            item4 = current.item4,
            isMarketOpen = isOpen,
            updatedBy = updatedBy,
            note = if (isOpen) "Market Opened" else "Market Closed"
        )
        rateHistoryDao.insertHistory(RateHistoryEntity.fromDomain(history))

        // Broadcast alert
        val statusText = if (isOpen) "Market is now OPEN." else "Market is CLOSED today."
        val notif = AppNotification(
            id = UUID.randomUUID().toString(),
            customerId = null,
            title = if (isOpen) "🟢 Market Open" else "🔴 Market Closed",
            message = statusText,
            timestamp = System.currentTimeMillis(),
            type = NotificationType.MARKET_STATUS
        )
        notificationDao.insertNotification(NotificationEntity.fromDomain(notif))

        Result.success(Unit)
    }

    // ==================== RATE HISTORY ====================

    fun getAllRateHistoryFlow(): Flow<List<RateHistoryEntry>> {
        return rateHistoryDao.getAllHistoryFlow().map { list -> list.map { it.toDomain() } }
    }

    // ==================== NOTIFICATIONS ====================

    fun getNotificationsForCustomerFlow(customerId: String): Flow<List<AppNotification>> {
        return notificationDao.getNotificationsForCustomerFlow(customerId).map { list -> list.map { it.toDomain() } }
    }

    fun getAllNotificationsFlow(): Flow<List<AppNotification>> {
        return notificationDao.getAllNotificationsFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun markNotificationAsRead(id: String) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun sendBroadcastNotification(title: String, message: String) = withContext(Dispatchers.IO) {
        val notif = AppNotification(
            id = UUID.randomUUID().toString(),
            customerId = null,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            type = NotificationType.SYSTEM_ALERT
        )
        notificationDao.insertNotification(NotificationEntity.fromDomain(notif))
    }
}
