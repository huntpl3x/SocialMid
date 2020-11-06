package com.roichomsky.socialmid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.sql.Timestamp;
import java.util.HashMap;

public class UploadPostActivity extends AppCompatActivity {

    //views
    Button galleryBtn, cameraBtn, uploadBtn;
    ImageView previewIv;
    EditText descriptionEt;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser user;

    ActionBar actionBar;

    //progress dialog
    ProgressDialog pd;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    //arrays of permissions to be requested
    String[] cameraPermissions;
    String[] storagePermissions;

    DatabaseReference databaseReference;

    //storage
    StorageReference storageReference;
    String storagePath = "Users_Post_Images/";

    //uri of picked image
    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Upload New Post");
        //enable back button in actionbar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Posts");
        storageReference = FirebaseStorage.getInstance().getReference();
        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        galleryBtn = findViewById(R.id.galleryBtn);
        cameraBtn = findViewById(R.id.cameraBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        previewIv = findViewById(R.id.previewIv);
        descriptionEt = findViewById(R.id.descriptionEt);

        pd = new ProgressDialog(this);
        pd.setMessage("Uploading Post...");

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkStoragePermission()){
                    requestStoragePermission();
                }
                else{
                    pickFromGallery();
                }
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkCameraPermission()){
                    requestCameraPermission();
                }
                else{
                    pickFromCamera();
                }
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPostPhoto(image_uri);
            }
        });
    }

    private boolean checkStoragePermission(){
        /*check if storage permission is enabled or not
         * return trie of enabled
         * return false if not enabled*/
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        //request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);

    }

    private boolean checkCameraPermission(){
        /*check if camera permission is enabled or not
         * return trie of enabled
         * return false if not enabled*/
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        //request runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*This method called when user press Allow or Deny from permission request dialog
         * here we will handle permissions cases (allowed & denied)*/

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                //picking from camera, first check if camera and storage permissions allowed or not
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //permissions enabled
                        pickFromCamera();
                    }
                    else{
                        //permissions denied
                        Toast.makeText(this, "Please enable camera & storage permission", Toast.LENGTH_LONG).show();

                    }
                }
            }
            case STORAGE_REQUEST_CODE:{
                //picking from gallery, first check if storage permission allowed or not
                if (grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //permissions enabled
                        pickFromGallery();
                    }
                    else{
                        //permissions denied
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_LONG).show();

                    }
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void uploadPostPhoto(Uri uri) {
        //show progress
        pd.show();

        String filePathAndName = storagePath + "" + "post" + "_" + uri.toString();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        //check if image is uploaded or not and url is received
                        if (uriTask.isSuccessful()){
                            //image uploaded
                            //add url in post's database
                            HashMap<String, Object> results = new HashMap<>();
                            //first parameter is the key - post
                            //second parameter is the value of the image url stored in the firebase storage
                            results.put("postURL", downloadUri.toString());
                            results.put("description", descriptionEt.getText().toString().trim());
                            results.put("publisherID", user.getUid());
                            String postID = databaseReference.push().getKey();
                            results.put("postID", postID);
                            results.put("timestamp", Long.toString(timestamp.getTime()));
                            databaseReference.child(postID).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //url in database of post is added successfully
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getApplicationContext(), "Image uploaded...", Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error adding url in database of posts
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(UploadPostActivity.this, "Error uploading Image...", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                        else{
                            //error
                            pd.dismiss();
                            Toast.makeText(UploadPostActivity.this, "Some error occurred", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some errors, show error and dismiss the dialog
                        pd.dismiss();
                        Toast.makeText(UploadPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        /*This method will be called after picking image from camera or gallery*/
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //image is picked from gallery, get uri of image
                image_uri = data.getData();
                if (image_uri != null) {
                    previewIv.setImageURI(image_uri);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please choose a photo", Toast.LENGTH_SHORT).show();
                }
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image is picked from camera, get uri of image
                if (image_uri != null) {
                    previewIv.setImageURI(image_uri);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please choose a photo", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
