package com.example.afinal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private TextView greetings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        greetings = view.findViewById(R.id.tvUserName);
        ImageView bannerImage = view.findViewById(R.id.bannerImage);
        ImageView suplementosImage = view.findViewById(R.id.suplementosImage);
        ImageView mentalImage = view.findViewById(R.id.mentalImage);
        ImageView exercicioImage = view.findViewById(R.id.exercicioImage);
        ImageView remedio = view.findViewById(R.id.remedImage);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        greetings.setText(name);
                    } else {
                        Toast.makeText(getContext(), "Nome não encontrado", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Erro ao recuperar o nome: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        bannerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.doctoralia.com.br/"));
                startActivity(Intent.createChooser(intent, "Abrir com"));
            }
        });

        suplementosImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mundoverde.com.br/"));
                startActivity(Intent.createChooser(intent, "Abrir com"));
            }
        });

        mentalImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.headspace.com/pt"));
                startActivity(Intent.createChooser(intent, "Abrir com"));
            }
        });

        exercicioImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nike.com.br/sc/treino-app-nike-training-club?srsltid=AfmBOoqZx7UnhocQIrynBzh4RX8zVtJMQiyjMZLaTXeobhNrdF_V0K8v"));
                startActivity(Intent.createChooser(intent, "Abrir com"));
            }
        });

        remedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.drogaraia.com.br/?gad_source=1&gclid=CjwKCAiAxKy5BhBbEiwAYiW--8bo7XI4W-01dJISwzUWvsEFIIwBmBPLIVCT7tZe7tLdzQFrGDPzOxoCRAgQAvD_BwE"));
                startActivity(Intent.createChooser(intent, "Abrir com"));
            }
        });
        return view;
    }
}