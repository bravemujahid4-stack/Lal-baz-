package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomers(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerByIdFlow(id: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE username = :username LIMIT 1")
    suspend fun getCustomerByUsername(username: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: String)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int
}

@Dao
interface MarketRatesDao {
    @Query("SELECT * FROM market_rates WHERE id = 'current' LIMIT 1")
    fun getCurrentRatesFlow(): Flow<MarketRatesEntity?>

    @Query("SELECT * FROM market_rates WHERE id = 'current' LIMIT 1")
    suspend fun getCurrentRates(): MarketRatesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setMarketRates(rates: MarketRatesEntity)
}

@Dao
interface RateHistoryDao {
    @Query("SELECT * FROM rate_history ORDER BY timestamp DESC")
    fun getAllHistoryFlow(): Flow<List<RateHistoryEntity>>

    @Query("SELECT * FROM rate_history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<RateHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: RateHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryList(entries: List<RateHistoryEntity>)

    @Query("DELETE FROM rate_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE customerId = :customerId OR customerId IS NULL ORDER BY timestamp DESC")
    fun getNotificationsForCustomerFlow(customerId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}

@Dao
interface AdminDao {
    @Query("SELECT * FROM admins WHERE username = :username LIMIT 1")
    suspend fun getAdminByUsername(username: String): AdminEntity?

    @Query("SELECT * FROM admins WHERE id = :id")
    suspend fun getAdminById(id: String): AdminEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminEntity)

    @Query("SELECT COUNT(*) FROM admins")
    suspend fun getAdminCount(): Int
}
