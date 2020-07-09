package com.example.testo.Notifications;



import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;


public class MyFirebaseIdService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Tokens").child(user.getUid());
            Token token=new Token(s);
            reference.setValue(token);
        }
    }
}
