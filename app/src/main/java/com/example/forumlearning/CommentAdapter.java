package com.example.forumlearning;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> mListComments;

    public CommentAdapter(List<Comment> mListComments) {
        this.mListComments = mListComments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = mListComments.get(position);
        if (comment == null) return;
        // get user
        DatabaseReference userRef = FirebaseHelper.mDatabaseReference.child("users").child(comment.getIdAuthor());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    User user = task.getResult().getValue(User.class);
                    if (user != null) {
                        holder.tvAuthor.setText(user.getFullname());
                        // get set image
                        // get avatar image form DB
                        StorageReference avatarsStorageRef = FirebaseHelper.mFStorage.child("assets/avatars/" + user.getId());
                        if (avatarsStorageRef != null) {
                            avatarsStorageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    try {
                                        Uri uri = task.getResult();
                                        if (uri != null) {
                                            Glide.with(holder.itemView.getRootView())
                                                    .load(task.getResult())
                                                    .error(R.drawable.ic_avatar_default)
                                                    .into(holder.imgAvatarAuthor);
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
        holder.tvContent.setText(comment.content);

        Date date = new Date(comment.time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd-MM-yyyy");
        sdf.setTimeZone(TimeZone.getDefault());
        String formattedDate = sdf.format(date);
        holder.tvDate.setText(formattedDate);

    }

    @Override
    public int getItemCount() {
        if (mListComments != null) return mListComments.size();
        return 0;
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        private TextView tvAuthor, tvContent, tvDate;
        private ImageView imgAvatarAuthor;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tv_author_comment);
            tvContent = itemView.findViewById(R.id.tv_content_comment);
            tvDate = itemView.findViewById(R.id.tv_date_comment);
            imgAvatarAuthor = itemView.findViewById(R.id.comment_user_img);
        }
    }
}
