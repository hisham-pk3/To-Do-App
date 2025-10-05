# 📝 To-Do App (Active & Completed Tasks)

A simple and elegant **To-Do List app** built using **Jetpack Compose**.  
This app allows users to add, track, complete, and delete their tasks with a clean Material 3 interface.  
It also preserves the app state during configuration changes such as screen rotations.

---

## 📱 App Overview

The app consists of two sections:

- **Items (Active):** Displays all ongoing tasks.  
  Each row includes the task label, a checkbox to mark completion, and a delete icon.
- **Completed Items:** Displays tasks that are finished.  
  Unchecking a completed task moves it back to the active list.

### ✨ Features

✅ Add new tasks (trims whitespace and validates non-empty input)  
✅ Mark tasks as completed or active  
✅ Delete tasks  
✅ Show friendly empty-state messages when lists are empty  
✅ Preserve state during device rotations  
✅ Built entirely with **Jetpack Compose + Material 3**

---

## 🧩 Concepts Used

### 🧱 Data Class

A `data class` is used to represent a single to-do item.  
It keeps the model simple and structured while providing built-in utility functions like `copy()`, `equals()`, and `hashCode()`.

```kotlin
data class TodoItem(
    val id: Long,
    val label: String,
    val isDone: Boolean = false
)
```

---

### ⚙️ State Management

The app uses **`mutableStateOf`** and **`rememberSaveable`** to store and observe the list of tasks.  
`rememberSaveable` ensures that the list persists across configuration changes (like screen rotations).

```kotlin
val todoStateList: MutableState<List<TodoItem>> = rememberSaveable(stateSaver = todoListSaver) {
    mutableStateOf(listOf())
}
```

Whenever the state changes (such as adding or completing a task), **Compose automatically recomposes** the affected UI components.

---

### 📤 State Hoisting

State and UI logic are separated for better reusability and testability.  
Child composables like `TodoList` and `TodoRow` are **stateless** — they receive data and callbacks from their parent.

Example:

```kotlin
TodoList(
    items = activeItems,
    onToggleDone = { item -> /* state handled in parent */ },
    onDelete = { item -> /* delete handled in parent */ }
)
```

This clear **unidirectional data flow** ensures that UI remains predictable and easy to debug.

---

## 🖼️ Screenshots

![Screenshot 4](screenshots/ss4.png)

---

## 🧰 Tech Stack

- **Language:** Kotlin  
- **UI Framework:** Jetpack Compose  
- **Design:** Material 3  
- **IDE:** Android Studio  
- **Minimum SDK:** 24  

---

## 🚀 How to Run the App

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/todo-compose-app.git
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and run the app on an emulator or connected device.

---

## 👨‍💻 Author

**Hisham Panamthodi Kajahussain**  
🎓 Graduate Student — Web & Mobile Application Development  

---

## 🪪 License

This project is licensed under the **MIT License**.  
You are free to use, modify, and distribute this project as long as attribution is provided.

---

⭐ *If you like this project, don’t forget to star the repository on GitHub!*

