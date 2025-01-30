package com.example.clubmate.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.clubmate.viewmodel.Category

@Composable
fun CategoryDropdown(selectedCategory: Category, onCategorySelected: (Category) -> Unit) {

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(text = selectedCategory.name)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Admin") },
                onClick = {
                    onCategorySelected(Category.Admin)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("General") },
                onClick = {
                    onCategorySelected(Category.General)
                    expanded = false
                }
            )
        }
    }
}
