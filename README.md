# Android Studio App

This is an Android Studio app developed as a requirement for the **INTEGRATIVE PROGRAMMING AND TECHNOLOGIES** subject.

---

## 📱 Features

- Modern Android UI
- Written in **Kotlin** and **Java**
- Built with **Gradle**
- User authentication (login/register)
- Admin approval for public recipes
- Guest mode for browsing recipes

---

## 🛠️ Technologies Used

- **Kotlin**
- **Java**
- **Android Studio**
- **Gradle**
- **PHP** (for backend authentication)
- **MySQL** (for user database)

---

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/Ranto0422/RecipeApp2.git
```

### 2. PHP Backend Setup

1. Copy the files from `php Files for login and register/` (login.php, register.php) into your XAMPP `htdocs` directory:
   ```
   C:/xampp/htdocs/myrecipeapp/
   ```
2. Start Apache and MySQL from the XAMPP control panel.
3. Open phpMyAdmin and create a new database:
   - Name: `myrecipeapp`

4. Create the `users` table by running this SQL in phpMyAdmin:
   ```sql
   CREATE TABLE users (
       id INT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       email VARCHAR(100) NOT NULL UNIQUE,
       password VARCHAR(255) NOT NULL,
       role ENUM('admin', 'user') DEFAULT 'user'
   );
   ```

5. (Optional) To create an admin account, insert a user with role 'admin':
   - start the app and register a new user with the name "Admin" and email "admin" then go to phpMyAdmin and manually change the role to 'admin' for that user.

6. Make sure your Android app's API URLs point to your local XAMPP server (e.g., `http://10.0.2.2/myrecipeapp/login.php`).

---

## 📂 Project Structure

- `app/src/main/java/com/example/recipeapp/ui/` — Android UI screens and components
- `php Files for login and register/` — PHP backend files for authentication
- `README.md` — Project instructions

---

## ✨ Additional Notes

- The app supports guest mode, admin approval, and user registration/login.
- For full functionality, ensure your backend is running and accessible from your Android emulator/device.
- You may need to adjust firewall or network settings for emulator-to-XAMPP communication.


## TODO
Issue
Searchbar
How to replicate
input 'sa' in searchbar it will show failed to load
then with 'sa' in the bar input 'L' this will load recipe with sal in the name
then remove the 'L' again it will now show the recipe with sa where it did not before

Improvement
Remember location
when i press a recipe in home then press back i want it to save the location to where i clicked the recipe

Improvement
User information
add a registration success message

improvement
security issue
when typing the password naka censor yung box but nakikita plain text sa text suggestion sa keyboard

Improvement
add validation for email,passwords, name duplcation and emailduplication in db

Improvement
if logged in as guest find a way to add a go to login screen instead of clicking other buttons to go to login

