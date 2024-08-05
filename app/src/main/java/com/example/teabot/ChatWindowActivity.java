package com.example.teabot;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatWindowActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_GALLERY = 1;
    private RecyclerView recyclerView;
    private Uri selectedImageUri;
    private EditText chatInput;
    private List<ChatMessage> chatMessages;
    private ChatMessageAdapter chatMessageAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean chatStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", false);

        if (isDarkMode) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme); // Default theme
        }

        setContentView(R.layout.activity_chat_window);

        recyclerView = findViewById(R.id.recyclerView);
        chatInput = findViewById(R.id.chattext);
        ImageView addImageBtn = findViewById(R.id.add);
        ImageView sendBtn = findViewById(R.id.sendbut);
        TextView qn1 = findViewById(R.id.qn1);
        TextView qn2 = findViewById(R.id.qn2);
        TextView qn3 = findViewById(R.id.qn3);

        chatMessages = new ArrayList<>();
        chatMessageAdapter = new ChatMessageAdapter(chatMessages, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatMessageAdapter);

        // Retrieve the image URI from the intent
        String imageUriString = getIntent().getStringExtra("selectedImageUri");
        if (imageUriString != null) {
            selectedImageUri = Uri.parse(imageUriString);
            // Replace the default image with the selected image
            ImageView imageView = findViewById(R.id.capturedimage);
            imageView.setImageURI(selectedImageUri);
        }

        // Set click listeners on predefined questions
        qn1.setOnClickListener(v -> sendMessage(qn1.getText().toString()));
        qn2.setOnClickListener(v -> sendMessage(qn2.getText().toString()));
        qn3.setOnClickListener(v -> sendMessage(qn3.getText().toString()));

        addImageBtn.setOnClickListener(view -> dispatchPickPictureIntent());
        sendBtn.setOnClickListener(view -> sendMessage(chatInput.getText().toString()));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listenForMessages();
    }

    private void dispatchPickPictureIntent() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
        }
    }

    private void sendMessage(String messageText) {
        if (!messageText.trim().isEmpty() || selectedImageUri != null) {
            ChatMessage message = new ChatMessage(messageText, selectedImageUri);
            db.collection("messages").add(message);
            chatInput.setText(""); // Clear the input field after sending
            selectedImageUri = null; // Reset the selected image URI
        }
        if (!chatStarted) {
            findViewById(R.id.linearLayout).setVisibility(View.GONE);
            findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
            chatStarted = true;
        }
    }


    private void listenForMessages() {
        Query query = db.collection("messages").orderBy("timestamp");
        query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                // Handle error
                return;
            }
            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

            for (DocumentChange change : documentChanges) {
                DocumentSnapshot document = change.getDocument();
                ChatMessage chatMessage = document.toObject(ChatMessage.class);
                switch (change.getType()) {
                    case ADDED:
                        chatMessages.add(chatMessage);
                        chatMessageAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerView.scrollToPosition(chatMessages.size() - 1);
                        break;
                    case MODIFIED:
                        // Handle modified document
                        break;
                    case REMOVED:
                        // Handle removed document
                        break;
                }
            }
        });
    }
}
