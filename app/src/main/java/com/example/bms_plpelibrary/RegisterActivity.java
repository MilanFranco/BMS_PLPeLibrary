package com.example.bms_plpelibrary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.bms_plpelibrary.models.User;
import com.example.bms_plpelibrary.utils.ValidationUtils;

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
        if (!validateInputs(name, email, password, confirmPassword)) {
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Create user with Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Safely get current user
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Create user object with confirmed userId
                            String userId = firebaseUser.getUid();
                            User user = new User(userId, name, email);

                            // Save user details to Firestore
                            firestore.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressBar.setVisibility(View.GONE);
                                            showSuccessAndNavigate();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressBar.setVisibility(View.GONE);
                                            // If Firestore save fails, sign out the user
                                            firebaseAuth.signOut();
                                            Toast.makeText(RegisterActivity.this,
                                                    "Failed to create user profile: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Unexpected: successful task but no user
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this,
                                    "Unexpected error during registration",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
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

        return isValid;
    }

    private void handleRegistrationError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(RegisterActivity.this,
                    "Email already in use",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RegisterActivity.this,
                    "Registration failed: " + exception.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccessAndNavigate() {
        Toast.makeText(RegisterActivity.this,
                "Registration successful!", Toast.LENGTH_SHORT).show();

        // Navigate to main activity
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}