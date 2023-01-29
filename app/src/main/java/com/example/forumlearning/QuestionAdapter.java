package com.example.forumlearning;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private List<Question> mListQuestion;
    private IClickListener mIClickListener;

    public interface IClickListener {
        void onClickItem(Question question);
    }


    public QuestionAdapter(List<Question> mListQuestion, IClickListener listener) {
        this.mListQuestion = mListQuestion;
        this.mIClickListener = listener;
    }
    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = mListQuestion.get(position);
        if (question == null) return;
        // get user data by idAuthor in Question
        DatabaseReference userRef = FirebaseHelper.mDatabaseReference
                .child("users")
                .child(question.getIdAuthor());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    try {
                        User author = task.getResult().getValue(User.class);
                        if (author != null) {
                            // set fullname
                            holder.tvFullname.setText(author.getFullname());
                            // set date
                            Date date = new Date(question.getTime());
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd-MM-yyyy");
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                            String formattedDate = sdf.format(date);
                            holder.tvDatetime.setText(formattedDate);
                            // set vote scores
                            int sources = question.getListUserVoteUp().size() - question.getListUserVoteDown().size();
                            holder.tvVoteScores.setText(Integer.toString(sources));
                            // set avatar author
                            StorageReference avatarsStorageRef = FirebaseHelper.mFStorage.child("assets/avatars/" + author.getId());
                            if (avatarsStorageRef != null) {
                                avatarsStorageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        try {
                                            Uri uri = task.getResult();
                                            if (uri != null) {
                                                Glide.with(holder.itemView.getRootView())
                                                        .load(uri)
                                                        .error(R.drawable.ic_avatar_default)
                                                        .into(holder.imgAuthorAvatar);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        holder.tvTitleQuestion.setText(question.getTitle());
        holder.itemQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIClickListener.onClickItem(question);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mListQuestion != null) return mListQuestion.size();
        return 0;
    }

    public class QuestionViewHolder extends RecyclerView.ViewHolder {

        private TextView tvFullname, tvDatetime, tvTitleQuestion, tvVoteScores;
        private ImageView imgAuthorAvatar;
        private LinearLayout itemQuestion;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullname = itemView.findViewById(R.id.tv_item_question_fullname);
            tvDatetime = itemView.findViewById(R.id.tv_item_question_datetime);
            tvTitleQuestion = itemView.findViewById(R.id.tv_item_question_title);
            tvVoteScores = itemView.findViewById(R.id.tv_item_question_vote_scores);
            itemQuestion = itemView.findViewById(R.id.item_question);
            imgAuthorAvatar = itemView.findViewById(R.id.img_item_question_author);
        }
    }
}
