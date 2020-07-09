package com.example.testo;

import com.google.firebase.database.FirebaseDatabase;

public class EnablePersistence extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
