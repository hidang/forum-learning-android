package com.example.forumlearning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUp extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPassword, edtPasswordAgain;
    private Button btnSignUp;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initUi();
        initListener();
    }

    private void initUi() {
        edtFullName = findViewById(R.id.edt_fullname);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        edtPasswordAgain = findViewById(R.id.edt_password_agin);
        btnSignUp = findViewById(R.id.btn_sign_up);

        progressDialog = new ProgressDialog(this);
    }

    private void initListener() {
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSignUp();
            }
        });
    }

    private void onClickSignUp() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String passwordAgain = edtPasswordAgain.getText().toString().trim();

        if (!password.equals(passwordAgain)) {
            Toast.makeText(this, "Wrong Password Confirm!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Write new user to RealTime DB (don't user firebaseHelper in here, it init in MainActivity)
                            String idNewUser = Objects.requireNonNull(task.getResult().getUser()).getUid();
                            User userTemp = new User(idNewUser, email, fullName, "", "");
                            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users");
                            myRef.child(idNewUser).updateChildren(userTemp.toMap(), new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@androidx.annotation.Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                    Toast.makeText(getApplicationContext(), "Create Profile success!", Toast.LENGTH_SHORT).show();
                                }
                            });
//                            Log.d(TAG, "createUserWithEmail:success");
                            Intent intent = new Intent(SignUp.this, MainActivity.class);
                            startActivity(intent);
                            finishAffinity(); // close all activiy before open (Home) MainActivity
                        } else {
//                          Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUp.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}