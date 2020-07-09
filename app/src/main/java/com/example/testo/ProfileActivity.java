package com.example.testo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.testo.onAppDestroyed.AppStatesHandling;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity  {
    private static final String TAG = "ProfileActivity";
    EditText name;
    ImageView image;
    Button updateProfile;
    ProgressBar progressBar;
    ProgressBar progressBarUploadingImage;
    final int TAKE_IMAGE_CODE=10012;
    final int PICK_IMAGE_FROM_GALLERY=121;
    FirebaseUser user;
    int STORAGE_PERMISSION_CODE=1;
    int CAMERA_PERMISSION_CODE=2;

    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("users");


    AppStatesHandling appStatesHandling;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        appStatesHandling=new AppStatesHandling();

        user=FirebaseAuth.getInstance().getCurrentUser();
        name=findViewById(R.id.user_name);
        image=findViewById(R.id.profile_image);
        updateProfile=findViewById(R.id.updateprofile_btn);
        progressBar=findViewById(R.id.progressBar_profile);
        progressBarUploadingImage=findViewById(R.id.progressBarUploadingImage);

        if(user==null){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
        if(user.getDisplayName()!=null){
            name.setText(user.getDisplayName());
            name.setSelection(user.getDisplayName().length());
        }
        if(user.getPhotoUrl()!=null){
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(image);
        }else {
            image.setImageResource(R.drawable.profile_avatar);
        }
    }

    public void handleImageClick(View view) {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setTitle("Choose a method");
        String []list=new String[]{"From Gallery","Take a photo","View picture"};
        alertDialog.setItems(list, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        if(ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                            fireStorageRequest(1);
                            Log.d(TAG, "onClick: "+"Asking for Gallery permission");
                        }else {
                            Intent intent1=new Intent(Intent.ACTION_PICK);
                            intent1.setType("image/*");
                            startActivityForResult(intent1,PICK_IMAGE_FROM_GALLERY);
                        }


                        break;
                    case 1:
                        if(ContextCompat.checkSelfPermission(ProfileActivity.this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                            fireStorageRequest(2);
                        }else {
                            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if(intent.resolveActivity(getPackageManager())!=null){
                                startActivityForResult(intent,TAKE_IMAGE_CODE);
                            }
                        }
                        break;
                    case 2:

                }
           }
       });
       alertDialog.create().show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==TAKE_IMAGE_CODE){
            if(resultCode==RESULT_OK){
                Bitmap bitmap= (Bitmap) data.getExtras().get("data");
                image.setImageBitmap(bitmap);
                handleUploadImage(bitmap);
            }
        }
        else if(requestCode==PICK_IMAGE_FROM_GALLERY){
            if(resultCode==RESULT_OK){
                 Uri uri=data.getData();
                 handleUploadImageGallery(uri);
                 Glide.with(this)
                         .load(uri)
                         .into(image);
            }
        }
    }
    private void handleUploadImageGallery( Uri uri){
        progressBarUploadingImage.setVisibility(View.VISIBLE);
        String realPath = ImageFilePath.getPath(ProfileActivity.this, uri);
        File actualImage=new File(realPath);
        try {
            Bitmap compressedImage=new Compressor(this)
                    .setMaxHeight(250)
                    .setMaxWidth(250)
                    .setQuality(50)
                    .compressToBitmap(actualImage);
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            compressedImage.compress(Bitmap.CompressFormat.JPEG,90,baos);
            byte[] finalImage=baos.toByteArray();

            String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference reference=FirebaseStorage.getInstance().getReference()
                    .child("profileImages")
                    .child(uid+".jpeg");

            UploadTask uploadTask=reference.putBytes(finalImage);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    getDownloadURL(reference);
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "Error uploading", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void handleUploadImage(Bitmap bitmap){
        progressBarUploadingImage.setVisibility(View.VISIBLE);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);


        String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference reference=FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(uid+".jpeg");
        reference.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBarUploadingImage.setVisibility(View.GONE);
                    getDownloadURL(reference);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Failed to upload image",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void getDownloadURL(StorageReference reference){
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                     setUserImage(uri);
            }
        });
    }
    private void setUserImage(final Uri uri){
        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();
        user.updateProfile(request).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileActivity.this, "Image successfully updated", Toast.LENGTH_SHORT).show();
                progressBarUploadingImage.setVisibility(View.GONE);
                databaseReference
                        .child(user.getUid())
                        .child("information")
                        .child("uriPath").setValue(uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Image failed to update", Toast.LENGTH_SHORT).show();
                progressBarUploadingImage.setVisibility(View.GONE);
            }
        });
    }
    public void handleUpdateProfile(View view) {
        progressBar.setVisibility(View.VISIBLE);
        updateProfile.setEnabled(false);
        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder()
                .setDisplayName(name.getText().toString())
                .build();
        user.updateProfile(request).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileActivity.this,"Successfully updated",Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
                updateProfile.setEnabled(true);
                databaseReference
                        .child(user.getUid())
                        .child("information")
                        .child("name").setValue(user.getDisplayName());
                databaseReference.child(user.getUid())
                        .child("search").setValue(user.getDisplayName().toLowerCase());


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this,"Failed to update",Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
                updateProfile.setEnabled(true);
            }
        });
    }
    private void fireStorageRequest(int i){
        if(i==1)
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        else if(i==2){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==STORAGE_PERMISSION_CODE){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent intent1=new Intent(Intent.ACTION_PICK);
                intent1.setType("image/*");
                startActivityForResult(intent1,PICK_IMAGE_FROM_GALLERY);
            }
        }else if(requestCode==CAMERA_PERMISSION_CODE){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intent.resolveActivity(getPackageManager())!=null){
                    startActivityForResult(intent,TAKE_IMAGE_CODE);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        appStatesHandling.incrementPaused();
    }

    @Override
    protected void onStart() {
        super.onStart();
        appStatesHandling.incrementStarted();
    }

    @Override
    protected void onStop() {
        super.onStop();
        appStatesHandling.incrementStopped();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appStatesHandling.incrementResumed();
    }
}
