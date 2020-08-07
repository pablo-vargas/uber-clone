package com.example.taxiserv.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taxiserv.R;
import com.example.taxiserv.WelcomeActivity;
import com.example.taxiserv.models.Client;
import com.example.taxiserv.utils.FirebaseAuthenticationAPI;
import com.example.taxiserv.utils.FirebaseRealtimeDatabaseAPI;
import com.example.taxiserv.utils.FirebaseStorageAPI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private final static int RC_STORAGE = 21;

    CircleImageView cvProfile;
    ImageButton ibUpload;
    TextInputEditText etUsername;
    TextInputEditText etNumber;
    TextView tvSaldo;
    Button btnEdit;
    CoordinatorLayout contentProfile;
    ProgressDialog progressDialog;

    private boolean isEdit = false;
    private Client client;

    private FirebaseAuthenticationAPI mAuth;
    private FirebaseRealtimeDatabaseAPI mDatabase;
    private FirebaseStorageAPI mStorage;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        contentProfile = (CoordinatorLayout) view.findViewById(R.id.contentProfile);
        cvProfile = (CircleImageView)view.findViewById(R.id.cvImageProfile);
        ibUpload = (ImageButton) view.findViewById(R.id.ibEditProfile);
        etUsername = (TextInputEditText)view.findViewById(R.id.etUsername);
        etNumber = (TextInputEditText)view.findViewById(R.id.etNumber);
        tvSaldo = (TextView)view.findViewById(R.id.tvSaldo);
        btnEdit = (Button)view.findViewById(R.id.btnEdit);

        mAuth = new FirebaseAuthenticationAPI();
        mDatabase = new FirebaseRealtimeDatabaseAPI();
        mStorage = new FirebaseStorageAPI();
        progressDialog = new ProgressDialog(getContext());

        setupDatas();
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditOrSave();
            }
        });
        cvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        ibUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        return view;
    }

    private void setupDatas() {
        // TODO: Falta agregar datos del num celular y saldo
        etUsername.setText(getCurrentClient().getUsername());
        etNumber.setText("78111111");
        loadImage(getCurrentClient().getPhotoUrl());
    }

    private void EditOrSave() {
        if(isEdit) {
            if(verifyDatas()) {
                updateDatas();
                isEdit = false;
                setInputsVisibility(false);
                btnEdit.setText("Editar");
            }
        } else {
            isEdit = true;
            setInputsVisibility(true);
            btnEdit.setText("Guardar");
        }
    }

    private void updateDatas() {
        showProgressDialog();
        final String username = etUsername.getText().toString().trim();
        String number = etNumber.getText().toString().trim();
        Map<String, Object> updates = new HashMap<>();
        updates.put(Client.USERNAME, username);
        updates.put(Client.NUMBER, number);
        mDatabase.getProfileByIdReference(getCurrentClient().getUid()).updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();
                        mAuth.getCurrentUser().updateProfile(profileUpdates);
                        hideProgressDialog();
                        showSnackBar("Datos actualizados corréctamente.");
                        ((WelcomeActivity)getActivity()).setUsername(username);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showSnackBar("Ocurrió un error.");
                        hideProgressDialog();
                    }
                });
    }

    private void openGallery() {
        if(isEdit) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, RC_STORAGE);
        }
    }

    private void showImagePreview(Intent data) {
        final String urlLocal = data.getDataString();
        final Uri uri = data.getData();

        View view = getLayoutInflater().inflate(R.layout.dialog_image_upload, null);
        final ImageView imgDialog = view.findViewById(R.id.imgDialog);
        final TextView tvMessage = view.findViewById(R.id.tvMessage);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("Cambiar imagen de perfil")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateImage(Uri.parse(urlLocal));
                    }
                })
                .setNegativeButton("Cancelar", null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),
                            uri);
                    imgDialog.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tvMessage.setText("¿Desea actualizar su imagen de perfil?");
            }
        });
        alertDialog.show();
    }

    private void updateImage(Uri uri) {
        showProgressDialog();
        StorageReference photoRef = mStorage.getProfileReferenceByEmail(getCurrentClient().getEmail());
        photoRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if(uri != null) {
                            updatePhotoUrl(uri);
                            hideProgressDialog();
                        } else {
                            showSnackBar("Error al subir la imagen.");
                            hideProgressDialog();
                        }
                    }
                });
            }
        });
    }

    private void updatePhotoUrl(final Uri uri) {
        final String myUri = uri.toString();
        Map<String, Object> updates = new HashMap<>();
        updates.put(Client.PHOTO_URL, myUri);

        mDatabase.getProfileByIdReference(getCurrentClient().getUid()).updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        mAuth.getCurrentUser().updateProfile(profileUpdates);
                        loadImage(myUri);
                        ((WelcomeActivity)getActivity()).loadPhoto(myUri);
                    }
                });
    }

    private void loadImage(String photoUrl) {
        Picasso.get()
                .load(photoUrl)
                .placeholder(R.drawable.ic_baseline_person)
                .error(R.drawable.ic_baseline_person)
                .into(cvProfile);
    }

    private void setInputsVisibility(boolean enable) {
        etUsername.setEnabled(enable);
        etNumber.setEnabled(enable);
        ibUpload.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private Client getCurrentClient() {
        if(client == null) {
            client = mAuth.getAuthClient();
        }
        return client;
    }

    private boolean verifyDatas() {
        boolean isValid = true;
        if(etUsername.getText().toString().trim().isEmpty()) {
            isValid = false;
            etUsername.setError("El campo es requerido.");
            etUsername.requestFocus();
        }
        if(etNumber.getText().toString().trim().isEmpty()) {
            isValid = false;
            etNumber.setError("El campo es requerido.");
            etNumber.requestFocus();
        }
        return isValid;
    }

    private void showSnackBar(String message) {
        Snackbar.make(contentProfile, message, Snackbar.LENGTH_LONG).show();
    }

    private void showProgressDialog() {
        progressDialog.setMessage("Espere...");
        progressDialog.show();
    }

    private void hideProgressDialog() {
        progressDialog.hide();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RC_STORAGE:
                    showImagePreview(data);
                    break;
            }
        }
    }
}