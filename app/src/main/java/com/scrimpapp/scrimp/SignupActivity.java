package com.scrimpapp.scrimp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.scrimpapp.scrimp.models.User;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    EditText etEmail;
    EditText etPhone;
    EditText etPass;
    EditText etConPass;
    EditText etBracuId;
    EditText etName;
    Button btSignUp;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPass = findViewById(R.id.etPassword);
        etConPass = findViewById(R.id.etConfirmPassword);
        etBracuId = findViewById(R.id.etBracuId);
        etName = findViewById(R.id.etName);

        btSignUp = findViewById(R.id.btSignUp);
        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        mAuth = FirebaseAuth.getInstance();

    }

    private void registerUser() {
        final String name = etName.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String password = etPass.getText().toString().trim();
        final String conPass = etConPass.getText().toString().trim();
        final String bracuId = etBracuId.getText().toString().trim();
        final String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError(getString(R.string.input_error_name));
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.input_error_email));
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.input_error_email_invalid));
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPass.setError(getString(R.string.input_error_password));
            etPass.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPass.setError(getString(R.string.input_error_password_length));
            etPass.requestFocus();
            return;
        }

        if(!conPass.equals(password)) {
            etConPass.setError(getString(R.string.input_error_password_mismatch));
            etConPass.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError(getString(R.string.input_error_phone));
            etPhone.requestFocus();
            return;
        }

        if (phone.length() != 11) {
            etPhone.setError(getString(R.string.input_error_phone_invalid));
            etPhone.requestFocus();
            return;
        }

        if(bracuId.length() != 8) {
            etBracuId.setError(getString(R.string.input_error_bracu_id));
            etBracuId.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            User newUser = new User(
                                name,
                                email,
                                phone,
                                bracuId
                            );

                            Map<String, String> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("email", email);
                            userMap.put("phone", phone);
                            userMap.put("bracu_id", bracuId);

                            FirebaseFirestore.getInstance().collection("users").add(userMap);
                        }
                    }
                }
        );

    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
