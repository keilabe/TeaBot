package com.example.teabot;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ImagePickerActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSION_CODE = 100;

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private List<Uri> imageUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        recyclerView = findViewById(R.id.galleryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // Adjust grid columns as needed

        imageUris = new ArrayList<>();
        adapter = new GalleryAdapter(this, imageUris);
        recyclerView.setAdapter(adapter);

        // Request both READ_EXTERNAL_STORAGE and CAMERA permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (Tiramisu) and higher
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.CAMERA
                }, REQUEST_PERMISSION_CODE);
            } else {
                loadImagesFromGallery();
            }
        } else {
            // Android 12 and lower
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                }, REQUEST_PERMISSION_CODE);
            } else {
                loadImagesFromGallery();
            }
        }

        FloatingActionButton cameraFab = findViewById(R.id.cameraFab);
        cameraFab.setOnClickListener(v -> openCamera());
    }


    private void loadImagesFromGallery() {
        imageUris = getImageUrisFromGallery();
        if (imageUris != null && !imageUris.isEmpty()) {
            adapter = new GalleryAdapter(this, imageUris);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "No images found in the gallery.", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Uri> getImageUrisFromGallery() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE
        };

        Cursor cursor = contentResolver.query(uri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
        List<Uri> imageUris = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));

                if (!mimeType.startsWith("image/")) continue;

                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                imageUris.add(imageUri);
            }
            cursor.close();
        }

        return imageUris;
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CODE);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean readExternalStorageGranted = false;
        boolean cameraGranted = false;

        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        readExternalStorageGranted = true;
                    } else if (permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        cameraGranted = true;
                    }
                }

                if (readExternalStorageGranted && cameraGranted) {
                    loadImagesFromGallery();
                } else {
                    Toast.makeText(this, "Permission denied. The feature cannot be used.", Toast.LENGTH_SHORT).show();
                    // Handle permission denial here
                }
                break;
        }
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Uri imageUri = saveImageAndGetUri(imageBitmap);
                imageUris.add(imageUri);
                adapter.notifyDataSetChanged();

                // Start ChatWindowActivity and pass the image URI
                Intent intent = new Intent(ImagePickerActivity.this, ChatWindowActivity.class);
                intent.putExtra("selectedImageUri", imageUri.toString()); // Pass the URI as a string
                startActivity(intent);
            }
        }
    }


    private Uri saveImageAndGetUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Captured Image", null);
        return Uri.parse(path);
    }
}
