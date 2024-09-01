package com.bhaaratcore.roni.simple_image_encryptor;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ImageEncryptor {
    private final Context context;
    private String ALGORITHM;

    // Constructor with all parameters
    public ImageEncryptor(Context context, String algorithm) {
        this.context = context;
        this.ALGORITHM = algorithm;
    }

    // Constructor with default algorithm
    public ImageEncryptor(Context context) {
        this(context, "AES/CBC/PKCS5Padding"); // Default algorithm
    }
    /** Direct methods **/

    public Uri encryptImageDefault(Bitmap imageBitmap,String fileName,String password) throws Exception{
        String base64imageString = convertBitmapToBase64String(imageBitmap);
        String encryptedImageBase64String = encryptString(base64imageString,password);

        return saveStringToFile(encryptedImageBase64String,fileName);
    }

    public Bitmap decryptImageDefault(Uri encryptedFile,String password) throws Exception{
        String fileContent = readStringFromFile(encryptedFile);
        String decryptedFileContent = decryptString(fileContent,password);

        return convertBase64StringToBitmap(decryptedFileContent);
    }


    /** Image Conversion methods **/
    public String convertBitmapToBase64String(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    public Bitmap convertBase64StringToBitmap(String base64String) {
        // Decode the Base64 string into a byte array
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);

        // Convert the byte array to a Bitmap
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }


    /** File read/write methods **/
    public String readStringFromFile(Uri uri) {
        String base64String="";
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            base64String = stringBuilder.toString();
            if(inputStream!=null){
                inputStream.close();
            }


            // Now you have the Base64 string, you can use it as needed
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64String;
    }
    public Uri saveStringToFile(String base64String, String fileName) {
        OutputStream outputStream = null;
        Uri fileUri = null;

        try {
            // Create a ContentValues object to define the file metadata
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream"); // Use appropriate MIME type if known
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            // Insert the file in MediaStore (Downloads folder)
            fileUri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);


            // Open the file output stream
            if (fileUri != null) {
                outputStream = context.getContentResolver().openOutputStream(fileUri);
                byte[] fileBytes = base64String.getBytes();
                if(outputStream!=null){
                    outputStream.write(fileBytes);
                }

            }
            Toast.makeText(context, "Encrypted image file saved at Downloads/"+fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return fileUri;
    }

    /** Encryption methods **/

    //Encrypt String
    public String encryptString(String message, String password) throws Exception {
        SecretKeySpec keySpec = generateKey(password);
        Cipher cipher = Cipher.getInstance(ALGORITHM); // Use CBC mode with PKCS5Padding

        // Generate Initialization Vector (IV)
        byte[] iv = new byte[cipher.getBlockSize()];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

        // Encrypt the message
        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        // Combine IV and encrypted data
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedBytes);

        return Base64.encodeToString(byteBuffer.array(), Base64.DEFAULT);
    }

   //Decrypt String
    public String decryptString(String emojis, String password) throws Exception {
        SecretKeySpec keySpec = generateKey(password);
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // Separate IV from encrypted data
        ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.decode(emojis, Base64.DEFAULT));
        byte[] iv = new byte[cipher.getBlockSize()];
        byteBuffer.get(iv);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);

        byte[] encryptedBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedBytes);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }


    //Generate key from password
    private SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();

        return new SecretKeySpec(key, "AES");
    }


}


