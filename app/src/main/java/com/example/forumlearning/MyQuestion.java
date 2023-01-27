package com.example.forumlearning;

import static com.example.forumlearning.MainActivity.MY_REQUEST_CODE;
import static com.example.forumlearning.MainActivity.getInstance;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyQuestion extends AppCompatActivity {

    private RecyclerView rcQuestions;
    private MyQuestionAdapter mMyQuestionAdapter;
    private List<Question> mListQuestions;
    private Uri mUriNewImageHolder;
    private ImageView mImageViewUpdateQuestionItemHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_question);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        rcQuestions = findViewById(R.id.rcv_my_questions);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcQuestions.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rcQuestions.addItemDecoration(dividerItemDecoration);

        mListQuestions = new ArrayList<>();
        mMyQuestionAdapter = new MyQuestionAdapter(mListQuestions, new MyQuestionAdapter.IClickListener() {
            @Override
            public void onClickItem(Question question) {
                Intent intent = new Intent(MyQuestion.this, DetailQuestion.class);
                intent.putExtra("questionID", question.getId());
                startActivity(intent);
            }
        }, new MyQuestionAdapter.IClickUpdateListener() {
            @Override
            public void onClickUpdateItem(Question question) {
                openDialogUpdateItem(question);
            }
        }, new MyQuestionAdapter.IClickDeleteListener() {
            @Override
            public void onClickDeleteItem(Question question) {
                onClickDeleteData(question);
            }
        });

        rcQuestions.setAdapter(mMyQuestionAdapter);

        getListQuestionFromRealtimeDatabase();

    }

    private void onClickDeleteData(Question question) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage("Bạn có chắc chắn muốn xoá câu hỏi này?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("questions");

                        myRef.child(String.valueOf(question.getId())).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                Toast.makeText(MyQuestion.this, "Delete question success!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openDialogUpdateItem(Question question) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_upadte_question);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        EditText edtTitle = dialog.findViewById(R.id.edt_update_title_question),
                edtContent= dialog.findViewById(R.id.edt_update_content_question);
        Button btnCancel  = dialog.findViewById(R.id.btn_cancel_update_question) ,
                btnUpdate = dialog.findViewById(R.id.btn_update_question);
        mImageViewUpdateQuestionItemHolder = dialog.findViewById(R.id.img_update_question);

        edtTitle.setText(question.getTitle());
        edtContent.setText(question.getContent());
        // TODO: get image from storage
        // get image question
        StorageReference imageStorageRef = FirebaseHelper.mFStorage.child("assets/questions/images/" + question.getId());
        if (imageStorageRef != null) {
            imageStorageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    try {
                        Uri uri = task.getResult();
                        if (uri != null) {
                            Glide.with(getApplicationContext())
                                    .load(task.getResult())
                                    .into(mImageViewUpdateQuestionItemHolder);
                        }
                    } catch (Exception _) {
                    }
                }
            });
        }

        mImageViewUpdateQuestionItemHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRequestPermission();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference myRef = FirebaseHelper.mDatabaseReference.child("questions");
                String newTitle = edtTitle.getText().toString().trim();
                String newContent = edtContent.getText().toString().trim();
                question.setTitle(newTitle);
                question.setContent(newContent);
                // TODO: update new image question if needed
                if (mUriNewImageHolder != null) {
                    StorageReference avatarsStorageRef = FirebaseHelper.mFStorage.child("assets/questions/images/" + question.getId());
                    avatarsStorageRef.putFile(mUriNewImageHolder)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//                                    Toast.makeText(getApplicationContext(), "Post Image Success", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Post Failure", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                myRef.child(String.valueOf(question.getId())).updateChildren(question.toMap(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        Toast.makeText(MyQuestion.this, "Update question success!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }

    private void getListQuestionFromRealtimeDatabase() {
        DatabaseReference myRef = FirebaseHelper.mDatabaseReference.child("questions");
        FirebaseUser user = FirebaseHelper.getCurrentUser();

        Query query = myRef.orderByChild("idAuthor").equalTo(user.getUid());
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Question question = snapshot.getValue(Question.class);
                if (question != null) {
                    mListQuestions.add(0, question);
                    mMyQuestionAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Question question =  snapshot.getValue(Question.class);
                if (mListQuestions == null || mListQuestions.isEmpty()) {
                    return;
                }
                for (int i=0; i< mListQuestions.size(); i++) {
                    if (question.getId().equals(mListQuestions.get(i).getId())) {
                        mListQuestions.set(i, question);
                        break;
                    }
                }
                mMyQuestionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Question question =  snapshot.getValue(Question.class);
                if (mListQuestions == null || mListQuestions.isEmpty()) {
                    return;
                }
                for (int i=0; i< mListQuestions.size(); i++) {
                    if (question.getId().equals(mListQuestions.get(i).getId())) {
                        mListQuestions.remove(mListQuestions.get(i));
                        break;
                    }
                }
                mMyQuestionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
            MyQuestion.this.requestPermissions(permissions, MY_REQUEST_CODE);
        }
    }

    private void openGllery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Update Question Picture"));
    }

    final private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent intent = result.getData();
                if (intent == null) return;
                mUriNewImageHolder = intent.getData();
                // set new picture url local to this CreateQuestionActivity
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mUriNewImageHolder);
                    mImageViewUpdateQuestionItemHolder.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}