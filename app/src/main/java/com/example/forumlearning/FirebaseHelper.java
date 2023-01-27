package com.example.forumlearning;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    public static FirebaseHelper firebaseHelper;

//    public DatabaseReference mDatabase;

    public static FirebaseAuth mAuth;

    public static FirebaseDatabase mDatabase;

    public static DatabaseReference mDatabaseReference;

    public static StorageReference mFStorage;

    public FirebaseHelper() {
        // [START initialize_database_ref]
//        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

//    public static FirebaseHelper getInstance() {
//        if (firebaseHelper == null) {
//            return firebaseHelper = new FirebaseHelper();
//        } else {
//            return firebaseHelper;
//        }
//    }

    public static void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFStorage = FirebaseStorage.getInstance().getReference();
    }

    public static FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

//    public void writeNewUser(String userId, String name, String email) {
//        User user = new User(name, email);
//
//        mDatabase.child("users").child(userId).setValue(user);
//    }
}
