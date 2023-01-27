package com.example.forumlearning;

import static com.example.forumlearning.MainActivity.MY_REQUEST_CODE;
import static com.example.forumlearning.MainActivity.getInstance;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CreateQuestionActivity extends AppCompatActivity {

    private EditText edtTitle, edtContent;
    private Button btnCreateQuestion;
    private ImageView imgQuestion, imgAvatar;
    private ProgressDialog progressDialog;
    private Uri mUri;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);

        user = FirebaseHelper.getCurrentUser();
        initUi();
        initListener();
    }

    private void initUi() {
        edtTitle = findViewById(R.id.edt_title);
        edtContent = findViewById(R.id.edt_content);
        btnCreateQuestion = findViewById(R.id.btn_create);
        imgQuestion = findViewById(R.id.img_create_question);
        imgAvatar = findViewById(R.id.img_avatar_user);

        // get avatar image form DB
        StorageReference avatarsStorageRef = FirebaseHelper.mFStorage.child("assets/avatars/" + user.getUid());
        avatarsStorageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                try {
                    Glide.with(getApplicationContext())
                            .load(task.getResult())
                            .error(R.drawable.ic_avatar_default)
                            .into(imgAvatar);
                } catch (Exception _) {
                }
            }
        });
    }

    private void initListener() {
        btnCreateQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCreateQuestion();
            }
        });
        imgQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRequestPermission();
            }
        });
    }

    private void onClickCreateQuestion() {
        String title = edtTitle.getText().toString();
        String content = edtContent.getText().toString();
        if (title.equals("")) {
            Toast.makeText(CreateQuestionActivity.this, "Title can't empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (content.equals("")) {
            Toast.makeText(CreateQuestionActivity.this, "Content can't empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        // Init: get current user login info
        DatabaseReference myRef = FirebaseHelper.mDatabaseReference;
        if (user == null) {
            return;
        }
        // TODO: refactor this code :D - by dang
        // Step 1: update load image question if needed
        String idQuestion = UUID.randomUUID().toString();

        if (mUri != null) {
            StorageReference avatarsStorageRef = FirebaseHelper.mFStorage.child("assets/questions/images/" + idQuestion);
            avatarsStorageRef.putFile(mUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            // Step 2: create question data
                            Date currentTime = Calendar.getInstance().getTime();
                            Question question = new Question(idQuestion, user.getUid(), title, content, currentTime.getTime());
                            myRef.child("questions").child(idQuestion).setValue(question, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@androidx.annotation.Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Question Posted", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Post Failure", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Step 2: create question data
            Date currentTime = Calendar.getInstance().getTime();
            Question question = new Question(idQuestion, user.getUid(), title, content, currentTime.getTime());
            myRef.child("questions").child(idQuestion).setValue(question, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@androidx.annotation.Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Question Posted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onClickRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGllery();
            return;
        }

        if (getApplication().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGllery();
        } else {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            CreateQuestionActivity.this.requestPermissions(permissions, MY_REQUEST_CODE);
        }
    }

    private void openGllery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Question Picture"));
    }

    final private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent intent = result.getData();
                if (intent == null) return;
                mUri = intent.getData();
                // set new picture url local to this CreateQuestionActivity
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mUri);
                    imgQuestion.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}