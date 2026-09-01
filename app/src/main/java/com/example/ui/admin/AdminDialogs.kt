package com.example.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.BalanceType
import com.example.model.Customer
import com.example.ui.common.BalanceBadge
import com.example.ui.theme.*
import com.example.util.FormatUtils

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onAddCustomer: (
        name: String,
        username: String,
        password: String,
        phone: String,
        balance: Double,
        balanceType: BalanceType,
        hasCustomRates: Boolean,
        c1: Double?,
        c2: Double?,
        c3: Double?,
        c4: Double?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var balanceType by remember { mutableStateOf(BalanceType.RECEIVABLE) }
    var hasCustomRates by remember { mutableStateOf(false) }

    var customItem1 by remember { mutableStateOf("") }
    var customItem2 by remember { mutableStateOf("") }
    var customItem3 by remember { mutableStateOf("") }
    var customItem4 by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Customer",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = CardBorder)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (errorMessage != null) {
                        Surface(
                            color = ReceivableRedBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = ReceivableRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Customer Full Name *") },
                        placeholder = { Text("e.g. Abdul Hameed") },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_name"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = { Text("Login Username *") },
                        placeholder = { Text("e.g. abdul_hameed") },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_username"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Login Password *") },
                        placeholder = { Text("••••••••") },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_password"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone / Mobile (Optional)") },
                        placeholder = { Text("0300-1234567") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    // Balance Section
                    Text("Account Balance", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)

                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it },
                        label = { Text("Initial Balance (Rs.)") },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_balance"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Balance Type Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = balanceType == BalanceType.RECEIVABLE,
                            onClick = { balanceType = BalanceType.RECEIVABLE },
                            label = { Text("GET / RECEIVABLE (Red)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f).testTag("chip_receivable"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ReceivableRedBg,
                                selectedLabelColor = ReceivableRed
                            )
                        )
                        FilterChip(
                            selected = balanceType == BalanceType.PAYABLE,
                            onClick = { balanceType = BalanceType.PAYABLE },
                            label = { Text("GIVE / PAYABLE (Green)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f).testTag("chip_payable"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PayableGreenBg,
                                selectedLabelColor = PayableGreen
                            )
                        )
                    }

                    // Customer Specific Rate Toggle
                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use Custom Rate for This Customer", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            Text("Overrides the global market rate for this specific customer", fontSize = 11.sp, color = SlateMuted)
                        }
                        Switch(
                            checked = hasCustomRates,
                            onCheckedChange = { hasCustomRates = it },
                            modifier = Modifier.testTag("switch_custom_rate")
                        )
                    }

                    if (hasCustomRates) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customItem1,
                                onValueChange = { customItem1 = it },
                                label = { Text("Item 1 Custom Rate (Rs.)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = customItem2,
                                onValueChange = { customItem2 = it },
                                label = { Text("Item 2 Custom Rate (Rs.)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = customItem3,
                                onValueChange = { customItem3 = it },
                                label = { Text("Item 3 Custom Rate (Rs.)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = customItem4,
                                onValueChange = { customItem4 = it },
                                label = { Text("Item 4 Custom Rate (Rs.)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Please enter customer name"
                                return@Button
                            }
                            if (username.isBlank()) {
                                errorMessage = "Please enter login username"
                                return@Button
                            }
                            if (password.isBlank()) {
                                errorMessage = "Please enter login password"
                                return@Button
                            }

                            val bal = balanceText.toDoubleOrNull() ?: 0.0
                            val c1 = customItem1.toDoubleOrNull()
                            val c2 = customItem2.toDoubleOrNull()
                            val c3 = customItem3.toDoubleOrNull()
                            val c4 = customItem4.toDoubleOrNull()

                            onAddCustomer(name, username, password, phone, bal, balanceType, hasCustomRates, c1, c2, c3, c4)
                        },
                        modifier = Modifier.weight(1f).testTag("button_save_customer"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Customer")
                    }
                }
            }
        }
    }
}

@Composable
fun EditCustomerDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onSave: (Customer, newPassword: String?) -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phone) }
    var newPassword by remember { mutableStateOf("") }
    var hasCustomRates by remember { mutableStateOf(customer.hasCustomRates) }

    var customItem1 by remember { mutableStateOf(customer.customRateItem1?.toString() ?: "") }
    var customItem2 by remember { mutableStateOf(customer.customRateItem2?.toString() ?: "") }
    var customItem3 by remember { mutableStateOf(customer.customRateItem3?.toString() ?: "") }
    var customItem4 by remember { mutableStateOf(customer.customRateItem4?.toString() ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit Customer Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = CardBorder)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (errorMessage != null) {
                        Surface(
                            color = ReceivableRedBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = errorMessage!!, color = ReceivableRed, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = customer.username,
                        onValueChange = {},
                        label = { Text("Username (Fixed)") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Reset Password (Leave blank to keep unchanged)") },
                        placeholder = { Text("New password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use Custom Rate for This Customer", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            Text("Set customer-specific rate if required", fontSize = 11.sp, color = SlateMuted)
                        }
                        Switch(
                            checked = hasCustomRates,
                            onCheckedChange = { hasCustomRates = it }
                        )
                    }

                    if (hasCustomRates) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customItem1,
                                onValueChange = { customItem1 = it },
                                label = { Text("Item 1 Custom Rate (Rs.)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = customItem2,
                                onValueChange = { customItem2 = it },
                                label = { Text("Item 2 Custom Rate (Rs.)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = customItem3,
                                onValueChange = { customItem3 = it },
                                label = { Text("Item 3 Custom Rate (Rs.)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = customItem4,
                                onValueChange = { customItem4 = it },
                                label = { Text("Item 4 Custom Rate (Rs.)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Customer name cannot be blank"
                                return@Button
                            }

                            val updated = customer.copy(
                                name = name.trim(),
                                phone = phone.trim(),
                                hasCustomRates = hasCustomRates,
                                customRateItem1 = customItem1.toDoubleOrNull(),
                                customRateItem2 = customItem2.toDoubleOrNull(),
                                customRateItem3 = customItem3.toDoubleOrNull(),
                                customRateItem4 = customItem4.toDoubleOrNull()
                            )
                            onSave(updated, if (newPassword.isNotBlank()) newPassword else null)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateBalanceDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onUpdateBalance: (newBalance: Double, balanceType: BalanceType, note: String) -> Unit
) {
    var balanceText by remember { mutableStateOf(customer.balance.toString()) }
    var balanceType by remember { mutableStateOf(customer.balanceType) }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Update Balance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Text(
                    text = "Customer: ${customer.name}",
                    fontSize = 13.sp,
                    color = SlateMuted
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = ReceivableRed, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Balance Amount (Rs.)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_new_balance"),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text("Balance Type", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyDark)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = balanceType == BalanceType.RECEIVABLE,
                        onClick = { balanceType = BalanceType.RECEIVABLE },
                        label = { Text("GET (Red)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f).testTag("select_receivable"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ReceivableRedBg,
                            selectedLabelColor = ReceivableRed
                        )
                    )
                    FilterChip(
                        selected = balanceType == BalanceType.PAYABLE,
                        onClick = { balanceType = BalanceType.PAYABLE },
                        label = { Text("GIVE (Green)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f).testTag("select_payable"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PayableGreenBg,
                            selectedLabelColor = PayableGreen
                        )
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Reason (Optional)") },
                    placeholder = { Text("e.g. Payment received or invoice adjustment") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toDoubleOrNull()
                    if (bal == null) {
                        errorMessage = "Please enter a valid numeric amount"
                        return@Button
                    }
                    onUpdateBalance(bal, balanceType, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                modifier = Modifier.testTag("button_confirm_update_balance")
            ) {
                Text("Update Balance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
