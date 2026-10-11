package com.example.fiscus.presentation.common.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fiscus.presentation.common.util.formatShortMonth
import fiscus.shared.generated.resources.Res
import fiscus.shared.generated.resources.ic_chevron_left
import fiscus.shared.generated.resources.ic_chevron_right
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPickerSheet(
    selectedMonth: YearMonth,
    currentMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
) {
    var displayedYear by rememberSaveable { mutableIntStateOf(selectedMonth.year) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Text(
                "Pilih bulan",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            )
            YearSelector(
                year = displayedYear,
                canGoNext = displayedYear < currentMonth.year,
                onPrevious = { displayedYear-- },
                onNext = { displayedYear++ },
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..12).chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { monthNumber ->
                            val month = YearMonth(displayedYear, monthNumber)
                            MonthCell(
                                label = formatShortMonth(month),
                                selected = month == selectedMonth,
                                enabled = month <= currentMonth,
                                onClick = { onMonthSelected(month) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearSelector(year: Int, canGoNext: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(painterResource(Res.drawable.ic_chevron_left), contentDescription = "Tahun sebelumnya")
        }
        Text(
            year.toString(),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(painterResource(Res.drawable.ic_chevron_right), contentDescription = "Tahun berikutnya")
        }
    }
}

@Composable
private fun MonthCell(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = when {
            selected -> colors.primary
            else -> colors.surfaceContainer
        },
        contentColor = when {
            selected -> colors.onPrimary
            enabled -> colors.onSurface
            else -> colors.onSurfaceVariant.copy(alpha = 0.5f)
        },
        modifier = modifier.height(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
