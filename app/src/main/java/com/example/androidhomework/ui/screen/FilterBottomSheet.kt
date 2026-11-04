package com.example.androidhomework.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidhomework.data.preferences.FilterSettings
import com.example.androidhomework.data.preferences.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    initialFilters: FilterSettings,
    onDismiss: () -> Unit,
    onApply: (FilterSettings) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(initialFilters.selectedLanguage) }
    var onlyWithCover by remember { mutableStateOf(initialFilters.onlyWithCover) }
    var sortBy by remember { mutableStateOf(initialFilters.sortBy) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(
                text = "Filters & Sorting",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sort By", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = sortBy == SortOption.DEFAULT,
                        onClick = { sortBy = SortOption.DEFAULT },
                        label = { Text("Default") }
                    )
                    FilterChip(
                        selected = sortBy == SortOption.NEWEST,
                        onClick = { sortBy = SortOption.NEWEST },
                        label = { Text("Newest") }
                    )
                    FilterChip(
                        selected = sortBy == SortOption.MOST_EDITIONS,
                        onClick = { sortBy = SortOption.MOST_EDITIONS },
                        label = { Text("Editions") }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Language", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedLanguage.isEmpty(),
                        onClick = { selectedLanguage = "" },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = selectedLanguage == "ENG",
                        onClick = { selectedLanguage = "ENG" },
                        label = { Text("English") }
                    )
                    FilterChip(
                        selected = selectedLanguage == "RUS",
                        onClick = { selectedLanguage = "RUS" },
                        label = { Text("Russian") }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Only books with cover", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = onlyWithCover,
                    onCheckedChange = { onlyWithCover = it }
                )
            }

            Button(
                onClick = {
                    onApply(FilterSettings(selectedLanguage, onlyWithCover, sortBy))
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Apply & Search", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}