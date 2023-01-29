package com.example.forumlearning;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseHelper {
    public interface IResultCallback {
        void success();

        void failure();
    }

    private static final String TAG = "FirebaseHelper";
    public static FirebaseHelper firebaseHelper;

//    public DatabaseReference mDatabase;

    public static FirebaseAuth mAuth;

    public static FirebaseDatabase mDatabase;

    public static DatabaseReference mDatabaseReference;

    public static StorageReference mFStorage;

    public FirebaseHelper() {
        // init if needed
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

    public static void updateQuestion(Question question, IResultCallback result) {
        mDatabaseReference
                .child("questions")
                .child(question.getId())
                .updateChildren(question.toMap(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@androidx.annotation.Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        result.success();
                    }
                });
    }

    public static void updateUser(User user, IResultCallback result) {
        mDatabaseReference.child("users").child(getCurrentUser().getUid()).updateChildren(user.toMap(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                result.success();
            }
        });
    }
}
