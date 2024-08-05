package com.example.teabot;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private EditText uname;
    private EditText emailreg;
    private EditText passwordreg;
    private EditText passwordconfirm;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        // Initialize firebase auth
        auth = FirebaseAuth.getInstance();
        //Initialize firebase database
        database = FirebaseDatabase.getInstance();


        // Initialize views
        uname = findViewById(R.id.uname);
        emailreg = findViewById(R.id.emailreg);
        passwordreg = findViewById(R.id.passwordreg);
        passwordconfirm = findViewById(R.id.passwordconfirm);
        Button registerbtn = findViewById(R.id.registerbtn);

        // Set click listener for register button
        registerbtn.setOnClickListener(view -> {
            String name = uname.getText().toString();
            String email = emailreg.getText().toString();
            String password = passwordreg.getText().toString();
            String passwordcon = passwordconfirm.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || passwordcon.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(passwordcon)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                register(email, password);
            }
        });



        TextView btn =findViewById(R.id.haveanAcc);
        btn.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this,LoginActivity.class)));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }
    // Register method now defined outside onClick
    private void register(String emailreg, String passwordreg) {
        auth.createUserWithEmailAndPassword(emailreg, passwordreg)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        // Get the newly registered user
                        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                        String name = uname.getText().toString();

                        //write user data to the database
                        writeUserDataToFirestore( userId, name, emailreg, passwordreg);

                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        // In case registration fails, display the error message
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void writeUserDataToFirestore(String userId, String name, String email, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("password", password);

        db.collection("Users") // Your collection name
                .add(user) // Firestore auto-generates the document ID
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Get the auto-generated ID
                        String autoGeneratedId = documentReference.getId();

                        // Update the document with the userId field
                        documentReference.update("userId", userId)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(RegisterActivity.this, "User data saved to Firestore with ID: " + autoGeneratedId, Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this, "Error updating userId field", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Error saving user data to Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}

