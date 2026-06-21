# Debtbook 📱💰

**Debtbook** is an intelligent Android application designed to help you track personal debts and manage repayments efficiently. By leveraging the power of the Google Gemini API, the app provides personalized insights into your financial habits and helps you stay on top of your financial obligations.

## Features ✨

* **Debt Tracking:** Easily log, monitor, and manage your debts and repayment progress.
* **AI-Powered Insights:** Get personalized analysis and summaries of your overall financial standing.
* **Modern Architecture:** Built with Kotlin and adheres to modern Android development practices.

---

## Prerequisites 🛠️

Before you begin, ensure you have met the following requirements:
* **[Android Studio](https://android.com):** Download and install the latest stable version.
* **Gemini API Key:** Obtain your free key from [Google AI Studio](https://aistudio.google.com/).

---

## Getting Started 🚀

Follow these steps to get your local development environment up and running:

### 1. Clone the Repository
```bash
git clone https://github.com/piyush-1803/Debtbook.git
```

### 2. Open in Android Studio
1. Launch Android Studio and select **Open**.
2. Navigate to your cloned project directory.
3. Allow Gradle to sync and import all dependencies.

### 3. Configure API Key
In the root directory of your project, create a new file named `.env`. Add your Gemini API Key to it:
```properties
GEMINI_API_KEY=your_api_key_here
```

### 4. Build Configuration
Open `app/build.gradle.kts` and comment out or remove the following line to ensure a smooth local build:
```kotlin
signingConfig = signingConfigs.getByName("debugConfig")
```

### 5. Run the App
1. Connect your physical Android device or start an emulator.
2. Click the **Run** button in Android Studio.

---

## Troubleshooting 🔧

Having issues building or running the app? Try these quick fixes:

* **API Key Error:** Ensure your `.env` file is in the exact root folder of the project, not inside the `app/` module folder.
* **Gradle Sync Fails:** Check your internet connection and make sure your Android Studio SDK platforms are fully updated.
* **Build Fails due to Missing Signature:** Double-check that `signingConfig` was successfully commented out in `app/build.gradle.kts`.

---

## Upcoming Features 🔮

We are continuously working to improve Debtbook. Here is a look at what's coming next:
* **Payment Reminders:** Push notifications for upcoming due dates.
* **Budget Forecasting:** AI-driven projections on when you will be debt-free.
* **Dark Mode:** A sleek, eye-friendly dark theme.

---

## Built With 🛠️

* **[Kotlin](https://kotlinlang.org)** - Programming Language
* **[Android SDK](https://android.com)** - Framework
* **[Google Gemini API](https://google.dev)** - AI Integration

---

## License 📄

This project is open-source. Feel free to explore and contribute!
