package com.example.teabot;

import android.net.Uri;

public class ChatMessage {

    private String message;
    private Uri imageUri;

    public ChatMessage(String message, Uri imageUri) {
        this.message = message;
        this.imageUri = imageUri;
    }

    public String getMessage() {
        return message;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}
