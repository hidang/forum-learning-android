package com.example.forumlearning;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class DetailQuestion extends AppCompatActivity {

    private Question question;
    private ImageView imgQuestion, imgAvatarAuthor;
    private TextView tvTitle, tvContent, tvAuthorTime;
    private EditText edtComment;
    private ProgressDialog progressDialog;
    private Button btnComment;
    private RecyclerView rcComments;
    private CommentAdapter mCommentAdapter;
    private List<Comment> mListComments;

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
                    tvTitle.setText(question.getTitle());
                    // get form db (users table) -> get image user
                    try {
                        DatabaseReference userRef = FirebaseHelper.mDatabaseReference.child("users").child(question.getIdAuthor());
                        userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    User author = task.getResult().getValue(User.class);
                                    if (author != null) {
                                        Date date = new Date(question.time);
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

                    tvContent.setText(question.getContent());
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

    }

    private void initUi() {
        imgQuestion = findViewById(R.id.img_question);
        imgAvatarAuthor = findViewById(R.id.img_avatar_author);
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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("comments");

        Query query = myRef.orderByChild("idQuestion").equalTo(question.getId());
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Comment comment = snapshot.getValue(Comment.class);
                if (question != null) {
                    mListComments.add(comment);
                    mCommentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Comment comment = snapshot.getValue(Comment.class);
                if (mListComments == null || mListComments.isEmpty()) {
                    return;
                }
                for (int i = 0; i < mListComments.size(); i++) {
                    if (comment.getId().equals(mListComments.get(i).getId())) {
                        mListComments.set(i, comment);
                        break;
                    }
                }

                mCommentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Comment comment = snapshot.getValue(Comment.class);
                if (mListComments == null || mListComments.isEmpty()) {
                    return;
                }
                for (int i = 0; i < mListComments.size(); i++) {
                    if (comment.getId().equals(mListComments.get(i).getId())) {
                        mListComments.remove(mListComments.get(i));
                        break;
                    }
                }

                mCommentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
}