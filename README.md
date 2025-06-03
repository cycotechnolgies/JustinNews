# JustinNews

JustinNews is an Android application developed by CycoTechnologies that provides a platform for users to sign up, log in, and access news content. The app uses Firebase for authentication and user data management.

## Features

- **User Onboarding:** Simple onboarding flow with options to log in or sign up for new users.
- **User Registration:** Secure sign-up with email and password validation (including strong password requirements).
- **Authentication:** User login using email and password, powered by Firebase Authentication.
- **User Data Storage:** Stores user information (username, email) in Firebase Firestore on registration.
- **Modern Android UI:** Uses AndroidX, Material Design, and edge-to-edge layouts.
- **Splash Screen:** Animated splash screen leading to onboarding.

## Getting Started

### Prerequisites

- Android Studio (Electric Eel or newer recommended)
- Android device or emulator running Android 8.0 (API 26) or above
- Google Firebase account (for authentication and Firestore database)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/cycotechnolgies/JustinNews.git
   cd JustinNews
   ```

2. **Open in Android Studio:**  
   Import the project into Android Studio.

3. **Configure Firebase:**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/).
   - Register your Android app and download the `google-services.json` file.
   - Place `google-services.json` in `app/` directory.

4. **Build the Project:**  
   Sync Gradle and build the project.

5. **Run the App:**  
   Deploy to an emulator or physical device.

### Project Structure

- `app/src/main/java/com/cycotechnologies/justinnews/`
  - `MainActivity.java`: Home activity after login.
  - `OnboardActivity.java`: Handles onboarding logic.
  - `LoginActivity.java`: Handles user login.
  - `SignupActivity.java`: Handles user registration and validation.
  - `SplashActivity.java`: Splash screen implementation.
  - `validator.java`: Static methods for input validation.

## Usage

1. **Launch the app:**  
   The splash screen will be displayed, followed by the onboarding screen.

2. **Sign up:**  
   New users can create an account with a username, email, and secure password.

3. **Login:**  
   Existing users can log in with their credentials.

4. **Home:**  
   After successful login, users are taken to the main activity.

## Password Requirements

- At least 8 characters
- One uppercase letter (A–Z)
- One lowercase letter (a–z)
- One digit (0–9)
- One special character (!@#$%^&*)

## Dependencies

- AndroidX and Material Components
- Firebase Authentication
- Firebase Firestore
- SweetAlertDialog (for user feedback dialogs)

## Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT](LICENSE)

---

**Developed by CycoTechnologies**
