package com.example.testo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.User;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    final int RC_CODE=1010;
    private static final String TAG = "MainActivity";
    Button Login_registerButton;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Login_registerButton=findViewById(R.id.login_register_btn);
        Login_registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<AuthUI.IdpConfig> providers= Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.PhoneBuilder().build()
                );
                Intent intent=new Intent(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .setTheme(R.style.AppTheme)
                                .setTosAndPrivacyPolicyUrls(
                                        "https://example.com/terms.html",
                                        "https://example.com/privacy.html")
                                .setIsSmartLockEnabled(false).build()

                );
                startActivityForResult(intent,RC_CODE);

            }
        });

        databaseReference= FirebaseDatabase.getInstance().getReference("users");
    }
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user=firebaseAuth.getInstance().getCurrentUser();
        if(user!=null){

            if(user.getMetadata().getCreationTimestamp()==user.getMetadata().getLastSignInTimestamp()){
                Log.d(TAG, "onAuthStateChanged: "+ user.getMetadata().getCreationTimestamp()+"********"+user.getMetadata().getLastSignInTimestamp());
                String uriAvailable="default";
                if(user.getPhotoUrl()!=null)
                    uriAvailable=user.getPhotoUrl().toString();
                String phoneNumber=user.getPhoneNumber().replace("+2","");
                User user1=new User(user.getUid(),user.getDisplayName(),uriAvailable,"online",false,phoneNumber);
                databaseReference
                        .child(user.getUid())
                        .child("information")
                        .setValue(user1);

                Toast.makeText(this,"welcome new user",Toast.LENGTH_LONG).show();
                Log.d(TAG, "onAuthStateChanged: "+"new User created");
                if(user.getDisplayName()!=null)
                databaseReference.child(user.getUid()).child("search").setValue(user.getDisplayName().toLowerCase());
            }
            else {
                Log.d(TAG, "onAuthStateChanged: "+"Already user signed in");
                if(user.getDisplayName()!=null)
                Toast.makeText(this,"welcome back "+user.getDisplayName()+" !",Toast.LENGTH_LONG).show();
                else Toast.makeText(this, "Welcome back !", Toast.LENGTH_SHORT).show();
            }
            goTosignedinActivity();

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    private void goTosignedinActivity(){
        Intent intent=new Intent(getApplicationContext(),activity_signed.class);
        startActivity(intent);
        Log.d(TAG, "goTosignedinActivity: ");
        getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
    }
    private void AskForContactsPermission(){

    }
}
