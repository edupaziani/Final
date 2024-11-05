package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private Button logoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Inicializar o botão de logout
        logoutButton = view.findViewById(R.id.logout);

        // Configurar o clique do botão de logout
        logoutButton.setOnClickListener(v -> {
            // Logout do usuário usando FirebaseAuth
            FirebaseAuth.getInstance().signOut();

            // Mostrar mensagem de confirmação
            Toast.makeText(getContext(), "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();

            // Redirecionar para a tela de login
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpar a pilha de atividades
            startActivity(intent);
        });
        return view;
    }
}