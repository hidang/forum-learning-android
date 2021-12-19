package com.example.forumlearning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class DetailQuestion extends AppCompatActivity {

    private Question question;
    private TextView tvTitle, tvContent, tvAuthor, tvTime;
    private EditText edtComment;
    private ProgressDialog progressDialog;
    private Button btnComment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_question);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);

        initUi();

        Bundle b = getIntent().getExtras();
        String id = b.getString("questionID");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("questions");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Question _question = postSnapshot.getValue(Question.class);
                    if (id.equals(_question.getId())) {
                        question = _question;
                        tvTitle.setText(question.getTitle());
                        tvAuthor.setText(question.author.getFullname());
                        tvContent.setText(question.getContent());

                        Date date = new Date(question.time*1000L); // *1000 is to convert seconds to milliseconds
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/2021  -  hh:mm:ss"); // the format of your date
                        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7")); // give a timezone reference for formating (see comment at the bottom
                        String formattedDate = sdf.format(date);
                        tvTime.setText(formattedDate);
                        return;
                    }
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
        tvTitle = findViewById(R.id.tv_title_question);
        tvAuthor = findViewById(R.id.tv_author_question);
        tvContent = findViewById(R.id.tv_content_question);
        tvTime = findViewById(R.id.tv_time_question);
        edtComment = findViewById(R.id.edt_comment);
        btnComment = findViewById(R.id.btn_comment);
    }

    private void onClickCreateComment() {
        String content = edtComment.getText().toString();
        if (content.equals("")) {
            Toast.makeText(this, "Comment không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        User _user = new User(user.getUid(), user.getEmail(), user.getDisplayName());
        String idComment = UUID.randomUUID().toString();
        Date currentTime = Calendar.getInstance().getTime();

        Comment comment = new Comment(idComment, question.getId(), _user, content, currentTime.getTime());
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