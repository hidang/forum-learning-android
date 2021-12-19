package com.example.forumlearning;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class Question {
    public Question() {

    }

    public User getAuthor() {
        return author;
    }

    public User author;
    public String title;
    public String content;
    public String id;
    public String image;
    public long time;

    public long getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }


    public Question(String id, User author, String title, String content, long time) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.content = content;
        this.time = time;
    }
}
