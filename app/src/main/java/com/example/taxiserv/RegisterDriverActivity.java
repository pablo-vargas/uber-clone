package com.example.taxiserv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import com.example.taxiserv.models.Client;
import com.example.taxiserv.utils.FirebaseAuthenticationAPI;
import com.example.taxiserv.utils.FirebaseRealtimeDatabaseAPI;
import com.example.taxiserv.utils.FirebaseStorageAPI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class RegisterDriverActivity extends AppCompatActivity {
    private static final int RC_STORAGE = 21;
    private static final int RP_STORAGE = 121;

    CoordinatorLayout contentRegister;
    Button btnRegister;
    TextInputEditText etUsername;
    TextInputEditText etEmail;
    TextInputEditText etNumber;
    TextInputEditText etPassword1;
    TextInputEditText etPassword2;
    CircleImageView cvPhoto;

    private FirebaseAuthenticationAPI mAuth;
    private FirebaseRealtimeDatabaseAPI mDatabase;
    private FirebaseStorageAPI mStorage;

    private Uri photoSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        contentRegister = (CoordinatorLayout)findViewById(R.id.contenRegisterDriver);
        btnRegister = (Button)findViewById(R.id.btnRegister);
        etUsername = (TextInputEditText)findViewById(R.id.etUsernameDriver);
        etEmail = (TextInputEditText)findViewById(R.id.etCorreoDriver);
        etNumber = (TextInputEditText)findViewById(R.id.etNumber);
        etPassword1 = (TextInputEditText)findViewById(R.id.etPassword1);
        etPassword2 = (TextInputEditText)findViewById(R.id.etPassword2);
        cvPhoto = (CircleImageView)findViewById(R.id.cvRegisterDriver);

        mAuth = new FirebaseAuthenticationAPI();
        mDatabase = new FirebaseRealtimeDatabaseAPI();
        mStorage = new FirebaseStorageAPI();
        
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDriver();
            }
        });
        cvPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionStorage();
            }
        });
    }

    private void checkPermissionStorage() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        RP_STORAGE);
                return;
            }
        }
        openGallery();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case RP_STORAGE:
                    openGallery();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_STORAGE && resultCode == Activity.RESULT_OK) {
            showImagePreview(data);
        }
    }

    private void showImagePreview(Intent data) {
        if(data != null) {
            photoSelected = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                        photoSelected);
                cvPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveDriver() {
        if(verifyDatas()) {
            if (comparePassword()) {
                String correo = etEmail.getText().toString().trim();
                String password = etPassword2.getText().toString().trim();
                mAuth.getmAuth().createUserWithEmailAndPassword(correo, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    uploadImage();
                                } else {
                                    Snackbar.make(contentRegister, "Error.", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        }
    }

    private void uploadImage() {
        if (photoSelected != null) {
            final String email = etEmail.getText().toString().trim();
            StorageReference photoRef = mStorage.getProfileReferenceByEmail(email);
            photoRef.putFile(photoSelected).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if(uri != null) {
                                createDatas(uri);
                            } else {
                                Snackbar.make(contentRegister, "Error al subir la imagen.", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
        }
    }

    private void createDatas(Uri uri) {
        Client client = new Client();
        client.setUid(mAuth.getmAuth().getUid());
        client.setUsername(etUsername.getText().toString().trim());
        client.setEmail(etEmail.getText().toString().trim());
        client.setNumber(etNumber.getText().toString().trim());
        client.setPhotoUrl(uri.toString());
        updateDataFire(client);
        mDatabase.getProfileByIdReference(client.getUid()).setValue(client)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Intent intent = new Intent(RegisterDriverActivity.this, WelcomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Snackbar.make(contentRegister, "Error al guardar.", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void updateDataFire(Client client) {
        FirebaseUser user = mAuth.getCurrentUser();
        user.updateEmail(client.getEmail());
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(client.getPhotoUrl()))
                .setDisplayName(client.getUsername())
                .build();
        user.updateProfile(profileUpdates);
    }

    private boolean verifyDatas() {
        boolean isValid = true;
        if(etUsername.getText().toString().trim().isEmpty()) {
            isValid = false;
            etUsername.setError("El campo es obligatorio.");
            etUsername.requestFocus();
        }
        if(etEmail.getText().toString().trim().isEmpty()) {
            isValid = false;
            etEmail.setError("El campo es obligatorio.");
            etEmail.requestFocus();
        } else if(!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()) {
            isValid = false;
            etEmail.setError("El formato del correo no es válido.");
            etEmail.requestFocus();
        }
        if(etNumber.getText().toString().trim().length() != 8) {
            isValid = false;
            etNumber.setError("Introduzca un número válido.");
            etNumber.requestFocus();
        }
        if(etPassword1.getText().toString().trim().isEmpty()) {
            isValid = false;
            etPassword1.setError("El campo es obligatorio.");
            etPassword1.requestFocus();
        }
        if(etPassword2.getText().toString().trim().isEmpty()) {
            isValid = false;
            etPassword2.setError("El campo es obligatorio.");
            etPassword2.requestFocus();
        }
        if(photoSelected == null) {
            isValid = false;
            Snackbar.make(contentRegister, "Debe agregar una foto.", Snackbar.LENGTH_LONG).show();
        }

        return isValid;
    }

    private boolean comparePassword() {
        boolean isValid = true;
        if(!etPassword1.getText().toString().trim().equals(etPassword2.getText().toString().trim())) {
            isValid = false;
            Snackbar.make(contentRegister, "Las contraseñas no coinciden.", Snackbar.LENGTH_LONG).show();
        }
        return isValid;
    }
}