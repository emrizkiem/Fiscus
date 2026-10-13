package com.example.fiscus.presentation.addexpense

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fiscus.domain.model.Category
import com.example.fiscus.presentation.common.component.CategoryChip
import com.example.fiscus.presentation.common.util.ThousandSeparatorTransformation
import com.example.fiscus.presentation.common.util.formatFullDate
import com.example.fiscus.presentation.common.util.label
import fiscus.shared.generated.resources.Res
import fiscus.shared.generated.resources.ic_arrow_back
import fiscus.shared.generated.resources.ic_calendar
import fiscus.shared.generated.resources.ic_receipt
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Instant

@Composable
fun AddExpenseContent(
    state: AddExpenseState,
    onBack: () -> Unit,
    onAmountChange: (String) -> Unit,
    onMerchantChange: (String) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showReceiptImage by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        when {
                            state.isReviewMode -> "Review struk"
                            state.isEditMode -> "Edit pengeluaran"
                            else -> "Tambah pengeluaran"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Kembali")
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = onSave,
                enabled = !state.isSaving && !state.isReadingReceipt,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp)
                    .height(44.dp)
            ) {
                Text("Simpan", style = MaterialTheme.typography.labelLarge)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (state.isReviewMode) {
                ReceiptInfoRow(isReading = state.isReadingReceipt, onViewImage = { showReceiptImage = true })
            }
            AmountField(
                digits = state.amountDigits,
                showError = state.showAmountError,
                onChange = onAmountChange
            )
            if (state.isReviewMode && !state.isReadingReceipt) {
                Text(
                    "Periksa lagi sebelum disimpan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FormField(label = "Merchant") {
                OutlinedTextField(
                    value = state.merchant,
                    onValueChange = onMerchantChange,
                    placeholder = { Text("Warung Bu Sri") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            FormField(label = "Kategori", error = if (state.showCategoryError) "Pilih kategori dulu" else null) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Category.entries) { category ->
                        CategoryChip(
                            label = category.label,
                            selected = state.category == category,
                            onClick = { onCategorySelected(category) }
                        )
                    }
                }
            }
            FormField(label = "Tanggal") {
                DateRow(text = formatFullDate(state.date), onClick = { showDatePicker = true })
            }
            FormField(label = "Catatan") {
                OutlinedTextField(
                    value = state.note,
                    onValueChange = onNoteChange,
                    placeholder = { Text("Opsional") },
                    shape = MaterialTheme.shapes.medium,
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    val receiptImage = state.receiptImage
    if (showReceiptImage && receiptImage != null) {
        ReceiptImageDialog(imageBytes = receiptImage, onDismiss = { showReceiptImage = false })
    }

    if (showDatePicker) {
        ExpenseDatePickerDialog(
            selected = state.date,
            today = state.today,
            onConfirm = { date ->
                onDateSelected(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun AmountField(digits: String, showError: Boolean, onChange: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val amountStyle = MaterialTheme.typography.titleLarge
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val lineColor = when {
        showError -> colors.error
        isFocused -> colors.primary
        else -> colors.outlineVariant
    }

    Column {
        Text("Nominal", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = digits,
            onValueChange = onChange,
            textStyle = amountStyle.copy(color = colors.onSurface),
            visualTransformation = ThousandSeparatorTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(colors.primary),
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rp ", style = amountStyle, color = colors.onSurfaceVariant)
                        Box {
                            if (digits.isEmpty()) {
                                Text("0", style = amountStyle, color = colors.onSurfaceVariant)
                            }
                            innerTextField()
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(thickness = if (isFocused || showError) 2.dp else 1.dp, color = lineColor)
                }
            }
        )
        if (showError) {
            Spacer(Modifier.height(6.dp))
            Text("Nominal harus lebih dari 0", style = MaterialTheme.typography.bodySmall, color = colors.error)
        }
    }
}

@Composable
private fun FormField(label: String, error: String? = null, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun DateRow(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.weight(1f))
            Icon(
                painterResource(Res.drawable.ic_calendar),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDatePickerDialog(
    selected: LocalDate,
    today: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val todayMillis = today.toEpochMillis()
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = selected.toEpochMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
            override fun isSelectableYear(year: Int) = year <= today.year
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { pickerState.selectedDateMillis?.let { onConfirm(it.toLocalDate()) } ?: onDismiss() }) {
                Text("Pilih")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
)

private fun LocalDate.toEpochMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private fun Long.toLocalDate(): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

@Composable
private fun ReceiptInfoRow(isReading: Boolean, onViewImage: () -> Unit) {
    Surface(
        onClick = onViewImage,
        enabled = !isReading,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                if (isReading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(painterResource(Res.drawable.ic_receipt), contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(if (isReading) "Membaca struk..." else "Dibaca dari struk", style = MaterialTheme.typography.bodyLarge)
                if (!isReading) {
                    Text("Lihat gambar", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ReceiptImageDialog(imageBytes: ByteArray, onDismiss: () -> Unit) {
    val bitmap = remember(imageBytes) { runCatching { imageBytes.decodeToImageBitmap() }.getOrNull() }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Gambar struk",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                    )
                } else {
                    Text("Gambar tidak bisa ditampilkan", style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Tutup")
                }
            }
        }
    }
}
