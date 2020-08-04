package com.example.taxiserv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.taxiserv.models.Client;
import com.example.taxiserv.utils.FirebaseAuthenticationAPI;

public class WelcomeActivity extends AppCompatActivity {
    Button btnExit;
    FirebaseAuthenticationAPI mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        
        btnExit = (Button)findViewById(R.id.btnExit);
        mAuth = new FirebaseAuthenticationAPI();
        
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        Client client = mAuth.getAuthClient();
        Log.e("name", client.getUsername());
        Log.e("email", client.getEmail());
        Log.e("photo", client.getPhotoUrl());
    }

    private void exit() {
        mAuth.signOut();
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}