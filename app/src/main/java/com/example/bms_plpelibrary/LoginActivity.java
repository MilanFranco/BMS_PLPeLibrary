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
import com.example.bms_plpelibrary.R;
import com.example.bms_plpelibrary.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private TextInputLayout emailLayout, passwordLayout;
    private Button loginButton;
    private TextView forgotPasswordTextView, registerTextView;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordTextView = findViewById(R.id.forgot_password_text_view);
        registerTextView = findViewById(R.id.register_text_view);
        progressBar = findViewById(R.id.progress_bar);

        // Set click listeners
        loginButton.setOnClickListener(v -> loginUser());

        forgotPasswordTextView.setOnClickListener(v -> {
            // Navigate to forgot password screen
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        registerTextView.setOnClickListener(v -> {
            // Navigate to registration screen
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (!ValidationUtils.isValidEmail(email)) {
            emailLayout.setError("Please enter a valid email");
            return;
        } else {
            emailLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password cannot be empty");
            return;
        } else {
            passwordLayout.setError(null);
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Authenticate with Firebase
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // Login successful, navigate to main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(LoginActivity.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}