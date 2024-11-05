package com.example.afinal;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private EditText emailReg, passwordReg, nameReg;
    private Button signupButton;
    private TextView direcionamento;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent calIntent = new Intent(Register.this, MainActivity.class);
            startActivity(calIntent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        emailReg = findViewById(R.id.regEmail);
        passwordReg = findViewById(R.id.regPassword);
        signupButton = findViewById(R.id.signupButton);
        direcionamento = findViewById(R.id.signinText);
        nameReg = findViewById(R.id.nameXML);

        direcionamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent calIntent = new Intent(Register.this, Login.class);
                startActivity(calIntent);
                finish();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password, name;
                email = String.valueOf(emailReg.getText()).trim();
                password = String.valueOf(passwordReg.getText()).trim();
                name = String.valueOf(nameReg.getText()).trim();

                if (email.isEmpty()){
                    Toast.makeText(Register.this, "Enter E-mail", Toast.LENGTH_SHORT).show();
                }else if(password.isEmpty()){
                    Toast.makeText(Register.this, "Enter Password", Toast.LENGTH_LONG).show();
                }else if(name.isEmpty()){
                    Toast.makeText(Register.this, "Enter your Name", Toast.LENGTH_LONG).show();
                }else {

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user!=null) {
                                            // Salvar o nome do usuário no Firebase Realtime Database
                                            String userId = user.getUid();
                                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                                            userRef.child("name").setValue(name);

                                            Toast.makeText(Register.this, "Account created.", Toast.LENGTH_SHORT).show();
                                            Intent calIntent = new Intent(Register.this, Login.class);
                                            startActivity(calIntent);
                                            finish();
                                        }

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(Register.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}