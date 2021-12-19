package com.example.forumlearning;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Comment {
    public Comment() {

    }

    public String id;
    public String idQuestion;
    public User author;
    public String content;
    public long time;

    public Comment(String id, String idQuestion, User author, String content, long time) {
        this.id = id;
        this.idQuestion = idQuestion;
        this.author = author;
        this.content = content;
        this.time = time;
    }
}
