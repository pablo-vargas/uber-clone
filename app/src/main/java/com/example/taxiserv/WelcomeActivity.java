package com.example.taxiserv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taxiserv.models.Client;
import com.example.taxiserv.utils.FirebaseAuthenticationAPI;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

public class WelcomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    CircleImageView cvProfile;
    TextView tvUsername;
    TextView tvEmail;
    
    FirebaseAuthenticationAPI mAuth;
    Client currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView)findViewById(R.id.navView);
        View hView = navigationView.getHeaderView(0);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        tvUsername = (TextView) hView.findViewById(R.id.tvUsernameDrawer);
        tvEmail = (TextView) hView.findViewById(R.id.tvEmailDrawer);
        cvProfile = (CircleImageView) hView.findViewById(R.id.cvProfileDrawer);

        mAuth = new FirebaseAuthenticationAPI();

        setupToolbar();
        setupDatas();
        setFragmentByDefault();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupDatas() {
        Picasso.get()
                .load(getCurrentClient().getPhotoUrl())
                .placeholder(R.drawable.ic_baseline_person)
                .error(R.drawable.ic_baseline_person)
                .into(cvProfile);
        tvUsername.setText(getCurrentClient().getUsername());
        tvEmail.setText(getCurrentClient().getEmail());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setFragmentByDefault() {
        MenuItem item = navigationView.getMenu().getItem(0);
        item.setChecked(true);
        getSupportActionBar().setTitle(item.getTitle());
    }

    private void exit() {
        mAuth.signOut();
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private Client getCurrentClient() {
        if(currentUser == null) {
            currentUser = mAuth.getAuthClient();
        }
        return currentUser;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start:
                Toast.makeText(WelcomeActivity.this, item.getTitle(), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_profile:
                Toast.makeText(WelcomeActivity.this, item.getTitle(), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_history:
                Toast.makeText(WelcomeActivity.this, item.getTitle(), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_termsUse:
                Toast.makeText(WelcomeActivity.this, item.getTitle(), Toast.LENGTH_LONG).show();
                break;
            case R.id.action_sigOut:
                exit();
                break;
        }
        item.setChecked(true);
        drawerLayout.closeDrawers();
        getSupportActionBar().setTitle(item.getTitle());
        return true;
    }
}