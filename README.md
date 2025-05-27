# Trackify 💰

Built as part of a final-year project, Trackify is a collaborative finance tracking app designed to help individuals & couples manage their finances within shared environments called Spaces.

The main idea behind Trackify is to offer individuals or couples a lightweight and intuitive way to record income and expenses, visualize their financial situation, and set budgets. All within a shared context where multiple users can interact equally.

## 💡 Features

- Create or join shared financial Spaces
- Record categorized transactions (income/expenses)
- Track monthly budgets by category
- View real-time charts and summaries
- Organize spending by objectives (e.g. savings, rent, leisure)
- Secure login system using JWT authentication
- Clean and modular architecture <!--(MVVM on Android, REST API on backend)-->

## 📂 Project Structure
The project follows a monorepo approach, combining both frontend and backend in the same repository.
```
trackify/
├── android/      # Native Android App
└── backend/      # Spring (Kotlin)
```

## 📱 Android App
The Android application is responsible for the main user experience. Built using Kotlin and Jetpack Compose<!-- using the MVVM pattern-->, it connects to the backend through secure API calls and provides a clean UI for managing transactions, budgets, and Spaces. 

<!--
```
api/
├── ApiClient.kt
└── ApiService.kt

models/
├── AuthModels.kt
├── BudgetModels.kt
├── SpaceModels.kt
└── TransactionModels.kt

ui/
├── BudgetScreen.kt
├── HomeScreen.kt
├── LoginScreen.kt
├── ObjectiveScreen.kt
├── RegisterScreen.kt
├── SettingsScreen.kt
├── SpaceSelectionScreen.kt
└── TransactionsScreen.kt

MainActivity.kt
```
-->


Key responsibilities:
- Handle user authentication (login, registration)
- Allow users to join or create financial Spaces
- Display categorized expenses with graphs
- Support for budgets and spending objectives  

## ☁️ Backend
The backend is built with Kotlin and Spring Boot<!--, following a modular structure-->. It exposes a secure REST API using JWT authentication and connects to a PostgreSQL database for persistent storage.

The server manages:

- User registration and authentication
- CRUD operations for transactions, budgets and spaces
- Secure token-based session management

<!--
Future additions:
- App screenshots
- License (MIT?)
- App & Repository status
- Contact/Links
-->