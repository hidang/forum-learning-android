package com.example.forumlearning;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class MyComment extends AppCompatActivity {

    private RecyclerView rcComments;
    private MyCommentAdapter mMyCommentAdapter;
    private List<Comment> mListComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_comment);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        rcComments = findViewById(R.id.rcv_my_comments);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcComments.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rcComments.addItemDecoration(dividerItemDecoration);

        mListComments = new ArrayList<>();
        mMyCommentAdapter = new MyCommentAdapter(mListComments, new MyCommentAdapter.IClickListener() {
            @Override
            public void onClickItem(Comment comment) {
                Intent intent = new Intent(MyComment.this, DetailQuestion.class);
                intent.putExtra("questionID", comment.getIdQuestion());
                startActivity(intent);
            }
        }, new MyCommentAdapter.IClickUpdateListener() {
            @Override
            public void onClickUpdateItem(Comment comment) {
                Toast.makeText(MyComment.this, "Dang update", Toast.LENGTH_SHORT).show();
            }
        }, new MyCommentAdapter.IClickDeleteListener() {
            @Override
            public void onClickDeleteItem(Comment comment) {
                Toast.makeText(MyComment.this, "Dang delet", Toast.LENGTH_SHORT).show();
            }
        });
        rcComments.setAdapter(mMyCommentAdapter);

        getListCommentsFromRealtimeDatabase();

    }

    private void getListCommentsFromRealtimeDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("comments");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Query query = myRef.orderByChild("author/id").equalTo(user.getUid());
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Comment comment = snapshot.getValue(Comment.class);
                if (comment != null) {
                    mListComments.add(0, comment);
                    mMyCommentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Comment comment =  snapshot.getValue(Comment.class);
                if (mListComments == null || mListComments.isEmpty()) {
                    return;
                }
                for (int i=0; i< mListComments.size(); i++) {
                    if (comment.getId().equals(mListComments.get(i).getId())) {
                        mListComments.set(i, comment);
                        break;
                    }
                }
                mMyCommentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Question question =  snapshot.getValue(Question.class);
                if (mListComments == null || mListComments.isEmpty()) {
                    return;
                }
                for (int i=0; i< mListComments.size(); i++) {
                    if (question.getId().equals(mListComments.get(i).getId())) {
                        mListComments.remove(mListComments.get(i));
                        break;
                    }
                }
                mMyCommentAdapter.notifyDataSetChanged();
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
}