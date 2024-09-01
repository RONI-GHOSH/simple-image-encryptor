package com.bhaaratcore.demo.imageencryptor;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bhaaratcore.roni.simple_image_encryptor.ImageEncryptor;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;

    private static final int PICK_FILE_REQUEST_CODE = 2;
    private ImageView imageView;
    private String base64ImageString="";

    private EditText editTextText;

    private String key ;

    ImageEncryptor imageEncryptor;


    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView);
        tv= findViewById(R.id.tv);
        editTextText = findViewById(R.id.editTextText);

        imageEncryptor =  new ImageEncryptor(getApplicationContext());

        Button button = findViewById(R.id.button);
        Button decryptButton = findViewById(R.id.button3);
        Button readTlkFile = findViewById(R.id.button5);
        button.setOnClickListener(v -> {
            // Open gallery
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });
        readTlkFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");  // You can filter with specific MIME types, or use "*/myext"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/octet-stream"}); // Use appropriate MIME if needed
            startActivityForResult(intent, PICK_FILE_REQUEST_CODE);



        });
        decryptButton.setOnClickListener(v -> {


        });

        Button clearImmageViewButton = findViewById(R.id.button4);
        clearImmageViewButton.setOnClickListener(v -> {
            imageView.setImageBitmap(null);
            editTextText.setText(null);
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                // Get bitmap from URI
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                // Set the image to ImageView (optional)
                imageView.setImageBitmap(bitmap);

                // Convert bitmap to Base64


                Uri encryptedfilepath= imageEncryptor.encryptImageDefault(bitmap,"new_encrypted_immage.textlockImg","roni7664");
                Toast.makeText(this, encryptedfilepath.toString(), Toast.LENGTH_SHORT).show();
                // Now base64ImageString contains the Base64 representation of the image
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                // Read the Base64 string from the file
                try {
                     Bitmap imagedecrypted =  imageEncryptor.decryptImageDefault(fileUri,"roni7664");
                     imageView.setImageBitmap(imagedecrypted);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }




}