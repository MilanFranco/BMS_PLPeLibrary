package com.example.bms_plpelibrary.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {

    /**
     * Validates if the email is in correct format
     * @param email Email string to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates if the password meets requirements
     * @param password Password string to validate
     * @return true if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }
}