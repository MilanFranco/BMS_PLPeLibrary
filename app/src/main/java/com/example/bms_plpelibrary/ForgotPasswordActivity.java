package com.example.bms_plpelibrary;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.example.bms_plpelibrary.utils.ValidationUtils;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private TextInputLayout emailLayout;
    private Button resetPasswordButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        emailEditText = findViewById(R.id.email_edit_text);
        emailLayout = findViewById(R.id.email_layout);
        resetPasswordButton = findViewById(R.id.reset_password_button);
        progressBar = findViewById(R.id.progress_bar);
        toolbar = findViewById(R.id.toolbar);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reset Password");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Set click listener
        resetPasswordButton.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        // Validate email
        if (!ValidationUtils.isValidEmail(email)) {
            emailLayout.setError("Please enter a valid email");
            return;
        } else {
            emailLayout.setError(null);
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Send password reset email
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Failed to send reset email: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}