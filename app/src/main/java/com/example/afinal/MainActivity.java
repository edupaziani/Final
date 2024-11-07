package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.afinal.databinding.ActivityMainBinding;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Verifica se o usuário está autenticado
        if (user == null) {
            Intent calIntent = new Intent(MainActivity.this, Login.class);
            startActivity(calIntent);
            finish();// Fecha a MainActivity para evitar que o usuário retorne sem fazer login
        }

        // Configura a navegação entre os fragments
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

//                switch (item.getItemId()) {
//                    case R.id.home:
//                        replaceFragment(new HomeFragment());
//                        break;
//
//                    case R.id.settings:
//                        replaceFragment(new SettingsFragment());
//                        break;
//
//                    case R.id.report:
//                        replaceFragment(new ReportFragment());
//                        break;
//
//                    default:
//                        return false;
//                }
//                return true;
//            });

            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.settings) {
                replaceFragment(new SettingsFragment());
            } else if (item.getItemId() == R.id.report) {
                replaceFragment(new ReportFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.options_menu, menu);
//        return true;
//    }
//
}