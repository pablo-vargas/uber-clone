package com.example.taxiserv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.taxiserv.utils.FirebaseAuthenticationAPI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;

public class LoginActivity extends AppCompatActivity {
    Button btnLogin;
    TextInputEditText etEmail;
    TextInputEditText etPassword;

    private FirebaseAuthenticationAPI mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button)findViewById(R.id.btnLogin);
        etEmail = (TextInputEditText)findViewById(R.id.etEmailLogin);
        etPassword = (TextInputEditText)findViewById(R.id.etPasswordLogin);

        mAuth = new FirebaseAuthenticationAPI();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        if(verifyDatas()) {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            mAuth.getmAuth().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(LoginActivity.this, "Email ó contraseña incorrectas.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private boolean verifyDatas() {
        boolean isValid = true;
        if(etEmail.getText().toString().trim().isEmpty()) {
            isValid = false;
            etEmail.setError("El campo es obligatorio.");
            etEmail.requestFocus();
        } else if(!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()) {
            isValid = false;
            etEmail.setError("El formato del correo no es válido.");
            etEmail.requestFocus();
        }
        if(etPassword.getText().toString().trim().isEmpty()) {
            isValid = false;
            etPassword.setError("El campo es obligatorio.");
            etPassword.requestFocus();
        }
        return isValid;
    }
}