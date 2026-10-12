package com.example.fiscus.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import fiscus.shared.generated.resources.Res
import fiscus.shared.generated.resources.ic_add_circle
import org.jetbrains.compose.resources.painterResource

@Composable
fun FiscusBottomBar(
    leftTab: Tab,
    rightTab: Tab,
    currentTab: Tab,
    isAddActive: Boolean,
    onTabSelected: (Tab) -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(leftTab, selected = currentTab == leftTab, onClick = { onTabSelected(leftTab) })
            AddButton(active = isAddActive, onClick = onAddClick)
            TabItem(rightTab, selected = currentTab == rightTab, onClick = { onTabSelected(rightTab) })
        }
    }
}

@Composable
private fun TabItem(tab: Tab, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    BarItem(label = tab.options.title, color = color, onClick = onClick) {
        tab.options.icon?.let { Icon(it, contentDescription = null, tint = color, modifier = Modifier.size(24.dp)) }
    }
}

@Composable
private fun AddButton(active: Boolean, onClick: () -> Unit) {
    val color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    BarItem(label = "Tambah", color = color, onClick = onClick) {
        Icon(
            painterResource(Res.drawable.ic_add_circle),
            contentDescription = "Tambah pengeluaran",
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun BarItem(
    label: String,
    color: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
