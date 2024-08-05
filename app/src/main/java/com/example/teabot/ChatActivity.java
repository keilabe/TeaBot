package com.example.teabot;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import java.io.IOException;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private ImageView capturedimage;
    private EditText qn1, qn2, qn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        capturedimage = findViewById(R.id.capturedimage);
        qn1 = findViewById(R.id.qn1);
        qn2 = findViewById(R.id.qn2);
        qn3 = findViewById(R.id.qn3);

        ImageButton chatQuit = findViewById(R.id.chatquit);
        chatQuit.setOnClickListener(v -> finish());

        ImageButton moreChat = findViewById(R.id.morechat);
        moreChat.setOnClickListener(v -> {
            //activities for the more option
        });
        capturedimage.setOnClickListener(v -> openImageOptions());

        //chatbot default questions
        qn1.setText("What is the disease?");
        qn2.setText("What is the cause of the disease?");
        qn3.setText("What are the possible curatives?");
    }

    private void openImageOptions(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/");

        Intent chooser = Intent.createChooser(galleryIntent, "Select Image");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent});
        startActivityForResult(chooser, REQUEST_IMAGE_CAPTURE);
    }

    protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if(requestCode == REQUEST_IMAGE_CAPTURE && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                capturedimage.setImageBitmap(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data.getData() != null) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    capturedimage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}