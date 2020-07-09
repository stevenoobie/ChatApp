package com.example.testo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;

import com.example.testo.Notifications.Token;
import com.example.testo.onAppDestroyed.AppStatesHandling;
import com.example.testo.onAppDestroyed.onClearTaskFromRecentList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;

import Model.User;
import Model.chatText;

public class activity_signed extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    private static final String TAG = "a7aa signed";
    Toolbar toolbar;
    ViewPager viewPager;
    PageAdapter pageAdapter;
    TabLayout tabLayout;

    DatabaseReference reference;
    AppStatesHandling appStatesHandling;
    ArrayList<String> chatsIds;
    String myNumber;
    FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
    Map<String,String>contactsMap;
    int i=0;
    int contacts_size=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate: SignedActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed);
        contactsMap=new HashMap<>();
        myNumber=firebaseUser.getPhoneNumber().replace("+2","");
       // Log.d(TAG, "onCreate: "+myNumber);
         requestContactsPermission();

        //startService(new Intent(getBaseContext(), onClearTaskFromRecentList.class));
        appStatesHandling = new AppStatesHandling();

        if (firebaseUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Status("Online");
        }
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tablayout);
        setSupportActionBar(toolbar);

        pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        //put user id inside the firestore database in ("users") document
        chatsIds = new ArrayList<>();
        syncMessages();
        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    private void updateToken(String s) {
        Token token = new Token(s);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.setValue(token);
    }

    private void syncMessages() {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.child("information").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                reference.child("Messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (user.isCheckMessages()) {
                            chatsIds.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                chatsIds.add(snapshot.getValue(String.class));
                                //Log.d(TAG, "onDataChange: " + snapshot.getValue());
                            }
                            //Log.d(TAG, "onDataChange: Size: " + chatsIds.size()+"\n");
                            if (chatsIds.size() > 0)
                                checkMessagesDelivered(user, chatsIds);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void checkMessagesDelivered(final User user, final ArrayList<String> chatsIds) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("chats").child("conversations");
        for (final String id : chatsIds) {
            Query query = reference.child(id).orderByChild("delivered").equalTo(false);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("delivered", true);
                        if (user.isCheckMessages()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getValue(chatText.class).getRecieverId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    snapshot.getRef().updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            final DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                                                    .child("users")
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                            user.setCheckMessages(false);
                                            reference1.child("information").child("checkMessages").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    reference1.child("Messages").removeValue();
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    private void syncContacts(){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("users");
        //Log.d(TAG, "syncContacts: ContactsSize: "+contactsMap.size());

        for(String phonenumber:contactsMap.keySet()){
            Query query=reference.orderByChild("information/phoneNumber").equalTo(phonenumber);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        i++;
                        //Log.d(TAG, "count: "+ i);
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            reference.child(snapshot.getKey()).child("information").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    User user = dataSnapshot.getValue(User.class);
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(user.getId(), contactsMap.get(user.getPhoneNumber()));
                                    //Log.d(TAG, "User: " + map.toString());
                                    reference.child(firebaseUser.getUid()).child("friends").updateChildren(map);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getContactsList() {
        //String Iso = getIso();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phonenumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phonenumber = phonenumber.replaceAll("\\s", "");
            phonenumber = phonenumber.replace("-", "");
                phonenumber=phonenumber.replace("+2","");

            if (phonenumber.length() == 11&&!contactsMap.containsKey(phonenumber)&&!phonenumber.equals(myNumber)) {
                contactsMap.put(phonenumber,name);
               // Log.d(TAG, "getContactsList: "+phonenumber+"  "+name);

            }

        }
        SharedPreferences sharedPreferences=getSharedPreferences("MySharedPref",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        for(String s:contactsMap.keySet()){
            editor.putString(s,contactsMap.get(s));
            Log.d(TAG, "getContactsList: "+s+"  "+contactsMap.get(s));
        }
        editor.apply();


        syncContacts();
    }

    private String getIso() {
        String iso = null;
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null) {
            if (!telephonyManager.getNetworkCountryIso().equals(""))
                iso = telephonyManager.getNetworkCountryIso();
        }
        return CountryToPrefix.getPhone(iso);
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
        appStatesHandling.incrementStarted();

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        appStatesHandling.incrementStopped();
    }


    @Override
    protected void onResume() {
        super.onResume();
        appStatesHandling.incrementResumed();

    }

    @Override
    protected void onPause() {
        super.onPause();
        appStatesHandling.incrementPaused();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_activity_signed, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                Status(Calendar.getInstance().getTime().toString());
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.profile:
                FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
                Intent intent1 = new Intent(this, ProfileActivity.class);
                startActivity(intent1);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void Status(String status) {
        reference = FirebaseDatabase.getInstance().getReference();
        reference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("information").child("activeStatus").setValue(status);
    }

    private void requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }else {
            getContactsList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted !!!!!", Toast.LENGTH_SHORT).show();
                getContactsList();
            }else if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_DENIED){
                Toast.makeText(this, "e2bal mtb2ash badin !!", Toast.LENGTH_LONG).show();
                requestContactsPermission();
            }
        }
    }
}
