# Recipe App

An Android Studio application developed as a requirement for the **INTEGRATIVE PROGRAMMING AND TECHNOLOGIES** subject.


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
