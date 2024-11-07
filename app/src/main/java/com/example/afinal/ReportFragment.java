package com.example.afinal;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReportFragment extends Fragment {

    private FloatingActionButton fab;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private RecyclerView recyclerView;
    private List<DataClass> datalist;
    private MyAdapter adapter;
    private DatabaseReference databaseReference;
    private ValueEventListener eventListener;
    private SearchView searchView;
    private TextView bpmValueTextView; // TextView para exibir o BPM

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // Inicializar o FloatingActionButton, a RecyclerView e a SearchView
        fab = view.findViewById(R.id.fab);
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.search);
        bpmValueTextView = view.findViewById(R.id.myBpm); // Inicializar o TextView para BPM

        // Configurar o clique do FAB para abrir o UploadDialogFragment
        fab.setOnClickListener(v -> {
            UploadDialogFragment uploadDialog = new UploadDialogFragment();
            uploadDialog.show(getChildFragmentManager(), "UploadDialog");
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        datalist = new ArrayList<>();
        adapter = new MyAdapter(getContext(), datalist);
        recyclerView.setAdapter(adapter);

        // Configuração do Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Android Tutorials");

        // Mostra um diálogo de carregamento enquanto os dados são recuperados
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        loadData(dialog);

        // Configura o listener de busca
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();  // Obtém o ID do usuário autenticado

            // Referência ao caminho do dado de BPM no Firebase
            DatabaseReference bpmRef = FirebaseDatabase.getInstance()
                    .getReference("SharedData/" + userId + "/sensor_data/bpm_data");

            bpmRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int bpm = snapshot.getValue(Integer.class); // Obtém o valor de BPM
                        bpmValueTextView.setText(String.valueOf(bpm) + " bpm"); // Define o texto no TextView
                    } else {
                        bpmValueTextView.setText("-- bpm Sensor(-1)");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
//                    if (getActivity() != null && isAdded()) {
//                        Toast.makeText(getActivity(), "Erro ao ler BPM: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                    }else{
//                        Log.e("ReportFragment", "Contexto nulo ao tentar exibir Toast: " + error.getMessage());
//                    }
                    try {
                        if (getActivity() != null && isAdded()) {
                            Toast.makeText(getActivity(), "Erro ao ler BPM: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("ReportFragment", "Contexto nulo ao tentar exibir Toast: " + error.getMessage());
                        }
                    } catch (Exception e) {
                        Log.e("ReportFragment", "Erro ao tentar exibir Toast: ", e);
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "Usuário não autenticado", Toast.LENGTH_SHORT).show();
        }


        return view;
    }
    private void loadData(AlertDialog dialog) {

        // Obter o UID do usuário autenticado
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Referência para o nó específico do usuário
        databaseReference = FirebaseDatabase.getInstance().getReference("Android Tutorials").child(userId);

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                datalist.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    DataClass dataClass = itemSnapshot.getValue(DataClass.class);
                    if (dataClass != null) {
                        dataClass.setKey(itemSnapshot.getKey());
                        datalist.add(dataClass);
                    }
                }
                adapter.notifyDataSetChanged();
                dialog.dismiss(); // Feche o diálogo após os dados serem carregados
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss(); // Feche o diálogo se ocorrer um erro
                Toast.makeText(getContext(), "Erro ao carregar dados: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void searchList(String text) {
        ArrayList<DataClass> searchList = new ArrayList<>();
        for (DataClass dataClass : datalist) {
            String title = dataClass.getDataTitle();
            if (title != null && text != null && title.toLowerCase().contains(text.toLowerCase())) {
                searchList.add(dataClass);
            }
        }
        adapter.searchDataList(searchList);
    }
}