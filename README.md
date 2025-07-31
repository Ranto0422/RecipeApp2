# Recipe App

An Android Studio application developed as a requirement for the **INTEGRATIVE PROGRAMMING AND TECHNOLOGIES** subject.

## Description

<<<<<<< Updated upstream
5. (Optional) To create an admin account, insert a user with role 'admin':
   - start the app and register a new user with the name "Admin" and email "admin" then go to phpMyAdmin and manually change the role to 'admin' for that user.
## Important
make a folder in C:\xampp\htdocs\ named MyRecipeUploads(this is for the uploaded images) and MyRecipeAppRestApi(put all the PHP files here)

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

when typing the password the text box is censored but the autocomplete suggestion of keyboard is not and the password shows

Improvement

add validation for email,passwords, name duplcation and emailduplication in db

Improvement
=======
This is a recipe management application that allows users to create, view, and manage recipes. The app includes user authentication, recipe submission, and administrative features.

## Features

- User registration and authentication
- Recipe creation and management
- Recipe browsing and search functionality
- Administrative approval system for recipes
- Image upload for recipes

## Setup Instructions

### Prerequisites
- Android Studio
- PHP server (XAMPP/WAMP recommended)
- MySQL database
- phpMyAdmin

### Installation

1. Clone this repository to your local machine
2. Import the project into Android Studio
3. Set up your PHP server and import the SQL structure from `Sql Structure import this into phpmyadmin/myrecipeapp.sql`
4. Configure your database connection in the PHP files
5. Update the API endpoints in the Android app to match your server configuration

### Admin Account Setup (Optional)

To create an admin account:
1. Start the app and register a new user with the name "Admin" and email "admin"
2. Go to phpMyAdmin
3. Manually change the role to 'admin' for that user in the database

## Known Issues

### Search Bar Bug
**Issue**: Search functionality inconsistent behavior
- **How to replicate**: 
  1. Input 'sa' in searchbar → shows "failed to load"
  2. With 'sa' still in the bar, input 'L' → loads recipes with "sal" in the name
  3. Remove the 'L' → now shows recipes with "sa" (where it didn't before)

## Planned Improvements

### High Priority
- [ ] **Registration Success Message**: Add confirmation message after successful user registration
- [ ] **Email/Password Validation**: Add proper validation for emails, passwords, name duplication, and email duplication in database
- [ ] **Guest User Navigation**: Add direct "Go to Login" option when logged in as guest instead of requiring navigation through other buttons

### Medium Priority
- [ ] **Location Memory**: Save scroll position when navigating from home to recipe details and back
- [ ] **Password Security**: Fix password visibility in keyboard text suggestions while typing

### Low Priority
- [ ] Fix search bar inconsistency issue
>>>>>>> Stashed changes

if logged in as guest find a way to implement a "go to login screen" instead of clicking buttons in the navbar to go to login screen

NEED TO IMPLEMENT
show message why the approval is declined

remove unused imports

validations for each input in addrecipeactivity and recipeeditactivity

inconsistency im not asking for caloris review in useruploaded recipes probably more 

Change color of "for approval" and "approved" recipes from user uploads
