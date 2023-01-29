package com.example.forumlearning;

import static com.example.forumlearning.MainActivity.MY_REQUEST_CODE;
import static com.example.forumlearning.MainActivity.getInstance;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

public class DetailQuestion extends AppCompatActivity {

    private Question question;
    private ImageView imgQuestion, imgAvatarAuthor, imgLike, imgDislike, imgEditQuestion;
    private TextView tvTitle, tvContent, tvAuthorTime, tvLikeCount, tvDislikeCount;
    private EditText edtComment;
    private ProgressDialog progressDialog;
    private Button btnComment;
    private RecyclerView rcComments;
    private CommentAdapter mCommentAdapter;
    private List<Comment> mListComments;

    private Uri mUriNewImageHolder;
    private ImageView mImageViewUpdateQuestionItemHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_question);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);

        initUi();

        Bundle b = getIntent().getExtras();
        String questionID = b.getString("questionID");

        DatabaseReference questionRef = FirebaseHelper
                .mDatabaseReference
                .child("questions")
                .child(questionID);

        questionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                question = dataSnapshot.getValue(Question.class);
                if (question != null) {
                    boolean isCurrentUserOwnerQuestion = FirebaseHelper.getCurrentUser().getUid().equals(question.getIdAuthor());
                    if (!isCurrentUserOwnerQuestion) {
                        // hide button edit
                        imgEditQuestion.setVisibility(View.GONE);
                    }

                    tvTitle.setText(question.getTitle());
                    tvContent.setText(question.getContent());
                    tvLikeCount.setText(Integer.toString(question.getVoteUpCount()));
                    tvDislikeCount.setText(Integer.toString(question.getVoteDownCount()));
                    // set image like dislike if needed
                    Integer imageLikeVote = R.drawable.ic_like_dark;
                    if (question.isUserVoteUp(FirebaseHelper.getCurrentUser().getUid())) {
                        imageLikeVote = R.drawable.ic_like_active;
                    }
                    Glide.with(getApplicationContext())
                            .load(imageLikeVote)
                            .error(R.drawable.ic_like_dark)
                            .into(imgLike);

                    Integer imageDislikeVote = R.drawable.ic_dislike_dark;
                    if (question.isUserVoteDown(FirebaseHelper.getCurrentUser().getUid())) {
                        imageDislikeVote = R.drawable.ic_dislike_active;
                    }
                    Glide.with(getApplicationContext())
                            .load(imageDislikeVote)
                            .error(R.drawable.ic_dislike_dark)
                            .into(imgDislike);

                    // get form db (users table) -> get image user
                    try {
                        DatabaseReference userRef = FirebaseHelper.mDatabaseReference.child("users").child(question.getIdAuthor());
                        userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    User author = task.getResult().getValue(User.class);
                                    if (author != null) {
                                        Date date = new Date(question.getTime());
                                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd-MM-yyyy");
                                        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                                        String formattedDate = sdf.format(date);
                                        tvAuthorTime.setText(formattedDate + " | " + author.getFullname());

                                        StorageReference avatarsStorageRef = FirebaseHelper.mFStorage.child("assets/avatars/" + author.getId());
                                        if (avatarsStorageRef != null) {
                                            avatarsStorageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    try {
                                                        Uri uri = task.getResult();
                                                        if (uri != null) {
                                                            Glide.with(getApplicationContext())
                                                                    .load(task.getResult())
                                                                    .error(R.drawable.ic_avatar_default)
                                                                    .into(imgAvatarAuthor);
                                                        }
                                                    } catch (Exception _) {
                                                    }
                                                }
                                            });
                                        }

                                    }
                                }
                            }
                        });
                    } catch (Exception _) {
                    }
                    // get image question
                    StorageReference imageStorageRef = FirebaseHelper.mFStorage.child("assets/questions/images/" + questionID);
                    if (imageStorageRef != null) {
                        imageStorageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                try {
                                    Uri uri = task.getResult();
                                    if (uri != null) {
                                        Glide.with(getApplicationContext())
                                                .load(task.getResult())
                                                .into(imgQuestion);
                                        imgQuestion.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                ImageViewPopUpHelper.enablePopUpOnClick(DetailQuestion.this, imgQuestion);
                                            }
                                        });
                                    }
                                } catch (Exception _) {
                                }
                            }
                        });
                    }

                    getListCommentFromRealtimeDatabase();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCreateComment();
            }
        });


        imgLike.setOnClickListener(new View.OnClickListener() {
            final String idUser = FirebaseHelper.getCurrentUser().getUid();

            @Override
            public void onClick(View view) {
                // handle like - unlike
                if (question.isUserVoteUp(idUser)) {
                    question.removeUserVoteUp(idUser);
                } else {
                    question.setUserVoteUp(idUser);
                }
                // update question to db realtime
                FirebaseHelper.updateQuestion(question, new FirebaseHelper.IResultCallback() {
                    @Override
                    public void success() {
                    }

                    @Override
                    public void failure() {
                    }
                });
            }
        });

        imgDislike.setOnClickListener(new View.OnClickListener() {
            final String idUser = FirebaseHelper.getCurrentUser().getUid();

            @Override
            public void onClick(View view) {
                // handle dislike - un-dislike
                if (question.isUserVoteDown(idUser)) {
                    question.removeUserVoteDown(idUser);
                } else {
                    question.setUserVoteDown(idUser);
                }
                // update question to db realtime
                FirebaseHelper.updateQuestion(question, new FirebaseHelper.IResultCallback() {
                    @Override
                    public void success() {
                    }

                    @Override
                    public void failure() {
                    }
                });
            }
        });

        imgEditQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogUpdateQuestion();
            }
        });
    }

    private void openDialogUpdateQuestion() {
        final String idUser = FirebaseHelper.getCurrentUser().getUid();
        if (idUser.equals(question.getIdAuthor())) {
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
                            Toast.makeText(DetailQuestion.this, "Update question success!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                }
            });

            dialog.show();
        }
    }

    private void initUi() {
        imgQuestion = findViewById(R.id.img_question);
        imgAvatarAuthor = findViewById(R.id.img_avatar_author);
        imgEditQuestion = findViewById(R.id.img_question_detail_edit);
        imgLike = findViewById(R.id.img_question_detail_like);
        imgDislike = findViewById(R.id.img_question_detail_dislike);
        tvLikeCount = findViewById(R.id.tv_question_detail_like_count);
        tvDislikeCount = findViewById(R.id.tv_question_detail_dislike_count);
        tvTitle = findViewById(R.id.tv_item_question_title);
        tvAuthorTime = findViewById(R.id.tv_question_date_author);
        tvContent = findViewById(R.id.tv_content_question);
        edtComment = findViewById(R.id.edt_comment);
        btnComment = findViewById(R.id.btn_comment);
        rcComments = findViewById(R.id.rcv_comments);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcComments.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rcComments.addItemDecoration(dividerItemDecoration);

        mListComments = new ArrayList<>();
        mCommentAdapter = new CommentAdapter(mListComments);
        rcComments.setAdapter(mCommentAdapter);
    }

    private void getListCommentFromRealtimeDatabase() {
        mListComments.clear();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query myRef = database.getReference("comments");
        Query queryRef = myRef.orderByChild("idQuestion").equalTo(question.getId());

        // cach 1 cua Dang
        queryRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mListComments != null) mListComments.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Comment comment = postSnapshot.getValue(Comment.class);
                    mListComments.add(comment);
                    List<Comment> tempListQuestions = mListComments
                            .stream().sorted(
                                    Comparator.comparing((Comment c) -> c.getTime() > comment.getTime())

                            )
                            .collect(Collectors.toList());
                    mListComments.clear();
                    mListComments.addAll(tempListQuestions);
                }
                mCommentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // cach 2 cua Hung
//        Query query = myRef.orderByChild("idQuestion").equalTo(question.getId());
//        query.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                Comment comment = snapshot.getValue(Comment.class);
//                if (question != null) {
//                    mListComments.add(comment);
//                    mCommentAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                Comment comment = snapshot.getValue(Comment.class);
//                if (mListComments == null || mListComments.isEmpty()) {
//                    return;
//                }
//                for (int i = 0; i < mListComments.size(); i++) {
//                    if (comment.getId().equals(mListComments.get(i).getId())) {
//                        mListComments.set(i, comment);
//                        break;
//                    }
//                }
//
//                mCommentAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                Comment comment = snapshot.getValue(Comment.class);
//                if (mListComments == null || mListComments.isEmpty()) {
//                    return;
//                }
//                for (int i = 0; i < mListComments.size(); i++) {
//                    if (comment.getId().equals(mListComments.get(i).getId())) {
//                        mListComments.remove(mListComments.get(i));
//                        break;
//                    }
//                }
//
//                mCommentAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void onClickCreateComment() {
        String content = edtComment.getText().toString();
        if (content.equals("")) {
            Toast.makeText(this, "Comment không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        DatabaseReference myRef = FirebaseHelper.mDatabaseReference;
        FirebaseUser user = FirebaseHelper.getCurrentUser();
        if (user == null) return;
        String idComment = UUID.randomUUID().toString();
        Date currentTime = Calendar.getInstance().getTime();

        Comment comment = new Comment(idComment, question.getId(), user.getUid(), content, currentTime.getTime());
        myRef.child("comments").child(idComment).setValue(comment, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                progressDialog.dismiss();
                edtComment.setText("");
                Toast.makeText(DetailQuestion.this, "Comment Posted", Toast.LENGTH_SHORT).show();
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


    // support for get image from local device
    private void onClickRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGllery();
            return;
        }

        if (getApplication().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGllery();
        } else {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            DetailQuestion.this.requestPermissions(permissions, MY_REQUEST_CODE);
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