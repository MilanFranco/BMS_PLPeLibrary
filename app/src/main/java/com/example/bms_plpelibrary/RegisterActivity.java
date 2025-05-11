package com.example.bms_plpelibrary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.bms_plpelibrary.models.User;
import com.example.bms_plpelibrary.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private Button registerButton;
    private TextView loginTextView;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);

        nameLayout = findViewById(R.id.name_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);

        registerButton = findViewById(R.id.register_button);
        loginTextView = findViewById(R.id.login_text_view);
        progressBar = findViewById(R.id.progress_bar);

        // Set up click listeners
        registerButton.setOnClickListener(v -> registerUser());

        loginTextView.setOnClickListener(v -> {
            // Navigate back to login screen
            finish();
        });
    }

    private void registerUser() {
        // Get input values
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate inputs
        boolean isValid = true;

        if (name.isEmpty()) {
            nameLayout.setError("Name cannot be empty");
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        if (!ValidationUtils.isValidEmail(email)) {
            emailLayout.setError("Please enter a valid email");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (!ValidationUtils.isValidPassword(password)) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords don't match");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        if (!isValid) {
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Create user with Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User created successfully, save additional user data
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        User user = new User(userId, name, email);

                        // Save user details to Firestore
                        firestore.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this,
                                            "Registration successful!", Toast.LENGTH_SHORT).show();

                                    // Navigate to main activity
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this,
                                            "Failed to create user profile: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegisterActivity.this,
                                    "Email already in use",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}