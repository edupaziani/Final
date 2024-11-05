package com.example.afinal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class UploadDialogFragment extends DialogFragment {

    private ImageView uploadImage;
    private Button saveButton;
    private EditText uploadTopic, uploadDesc;
    private Uri uri;
    private String imageUrl;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_dialog, container, false);

        // Inicializar componentes
        uploadImage = view.findViewById(R.id.uploadImage);
        saveButton = view.findViewById(R.id.saveButton);
        uploadTopic = view.findViewById(R.id.uploadTopic);
        uploadDesc = view.findViewById(R.id.uploadDesc);

        // Configurar o ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uri = result.getData().getData();
                        uploadImage.setImageURI(uri);
                    }
                });

        // Lógica de seleção de imagem
        uploadImage.setOnClickListener(v -> {
            Intent photoPicker = new Intent(Intent.ACTION_PICK);
            photoPicker.setType("image/*");
            activityResultLauncher.launch(photoPicker);
        });

        // Lógica de upload ao clicar no botão de salvar
        saveButton.setOnClickListener(v -> saveData());

        return view;
    }

    private void saveData() {
        if (uri == null) {
            Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = uploadTopic.getText().toString().trim();
        String desc = uploadDesc.getText().toString().trim();


        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Android Images")
                .child(Objects.requireNonNull(uri.getLastPathSegment()));

        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isComplete());
            Uri urlImage = uriTask.getResult();
            imageUrl = urlImage.toString();
            uploadDataToDatabase(title, desc);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadDataToDatabase(String title, String desc) {
        DataClass dataClass = new DataClass(title, desc, imageUrl);

        FirebaseDatabase.getInstance().getReference("Android Tutorials").child(title)
                .setValue(dataClass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Data Saved", Toast.LENGTH_SHORT).show();
                        dismiss(); // Fecha o diálogo após o upload bem-sucedido
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to Save Data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}