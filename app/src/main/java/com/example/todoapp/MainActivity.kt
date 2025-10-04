package com.example.todoapp

import android.os.Bundle
import androidx.compose.material3.HorizontalDivider
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import java.util.concurrent.atomic.AtomicLong

// Simple data model for each to-do item
data class TodoItem(
    val id: Long,
    val label: String,
    val isDone: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoApp()// Entry point composable
        }
    }
}
// Global ID generator for new items
private val idGenerator = AtomicLong(0L)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp() {
    // Custom Saver: converts List<TodoItem> to List<String> for rememberSaveable
    val todoListSaver = listSaver<List<TodoItem>, String>(
        save = { list ->
            // each item -> "id|isDone|label", label escaped simply by replacing '|' with '\u0001'
            list.map { item ->
                val safeLabel = item.label.replace("|", "\u0001")
                "${item.id}|${if (item.isDone) 1 else 0}|$safeLabel"
            }
        },
        restore = { savedList ->
            savedList.map { s ->
                val parts = s.split("|", limit = 3)
                val id = parts.getOrNull(0)?.toLongOrNull() ?: idGenerator.incrementAndGet()
                val done = parts.getOrNull(1) == "1"
                val label = parts.getOrNull(2)?.replace("\u0001", "|") ?: ""
                TodoItem(id, label, done)
            }
        }
    )

    // State list of To-Do items that survives configuration changes
    val todoStateList: MutableState<List<TodoItem>> = rememberSaveable(stateSaver = todoListSaver) {
        mutableStateOf(listOf())
    }

    // Derived states: separate active and completed items
    val activeItems by remember { derivedStateOf { todoStateList.value.filter { !it.isDone } } }
    val completedItems by remember { derivedStateOf { todoStateList.value.filter { it.isDone } } }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("To-Do: Active & Completed") })
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            // Input + Add button (state hoisted inside this parent)
            var input by remember { mutableStateOf(TextFieldValue("")) }
            var validationMessage by remember { mutableStateOf<String?>(null) }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        if (!it.text.trim().isEmpty()) validationMessage = null
                    },
                    placeholder = { Text("Add item...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val trimmed = input.text.trim()
                    if (trimmed.isEmpty()) {
                        validationMessage = "Please enter a non-empty item."
                    } else {
                        val newItem = TodoItem(idGenerator.incrementAndGet(), trimmed, false)
                        todoStateList.value = listOf(newItem) + todoStateList.value // add to top of active list
                        input = TextFieldValue("")
                        validationMessage = null
                    }
                }) {
                    Text("Add")
                }
            }
            // Show validation message if needed
            validationMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active section
            if (activeItems.isNotEmpty()) {
                Text("Items", style = MaterialTheme.typography.titleLarge)// section header
                Spacer(modifier = Modifier.height(8.dp))
                TodoList(
                    items = activeItems,
                    onToggleDone = { item ->
                        // toggle: move to completed
                        todoStateList.value = todoStateList.value.map {
                            if (it.id == item.id) it.copy(isDone = !it.isDone) else it
                        }
                    },
                    onDelete = { item ->
                        todoStateList.value = todoStateList.value.filter { it.id != item.id }
                    }
                )
            } else {
                // Friendly empty-state message for active items
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        "No items yet. Add your first task above!",
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Completed section
            if (completedItems.isNotEmpty()) {
                Text("Completed Items", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                TodoList(
                    items = completedItems,
                    onToggleDone = { item ->
                        // uncheck -> move back to active
                        todoStateList.value = todoStateList.value.map {
                            if (it.id == item.id) it.copy(isDone = !it.isDone) else it
                        }
                    },
                    onDelete = { item ->
                        todoStateList.value = todoStateList.value.filter { it.id != item.id }
                    }
                )
            } else {
                // Empty state message for completed items
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        "No completed items yet. Complete some tasks!",
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TodoList(
    items: List<TodoItem>,
    onToggleDone: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit
) {
    // LazyColumn for displaying list with unique keys for recomposition
    LazyColumn {
        itemsIndexed(items, key = { _, item -> item.id }) { _, item ->
            TodoRow(
                item = item,
                onToggle = { onToggleDone(item) },
                onDelete = { onDelete(item) }
            )
            HorizontalDivider()// separator between rows
        }
    }
}

@Composable
fun TodoRow(item: TodoItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = item.isDone, onCheckedChange = { onToggle() })// toggle completion
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.label,
            modifier = Modifier.weight(1f)// take remaining space
        )
        IconButton(onClick = onDelete) {
            // delete button
            Icon(Icons.Default.Close, contentDescription = "Delete")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTodo() {
    val sample = remember {
        mutableStateListOf(
            TodoItem(1, "Buy milk", false),
            TodoItem(2, "Submit assignment", true),
            TodoItem(3, "Call home", false)
        )
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Preview", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        TodoList(items = sample.filter { !it.isDone }, onToggleDone = {}, onDelete = {})
        Spacer(modifier = Modifier.height(12.dp))
        TodoList(items = sample.filter { it.isDone }, onToggleDone = {}, onDelete = {})
    }
}
