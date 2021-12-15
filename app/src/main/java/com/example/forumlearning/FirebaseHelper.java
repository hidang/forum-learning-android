package com.example.forumlearning;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";

    public FirebaseHelper(DatabaseReference database) {
        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]
    }

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    public void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        mDatabase.child("users").child(userId).setValue(user);
    }
}
