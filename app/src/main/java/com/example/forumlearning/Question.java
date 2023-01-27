package com.example.forumlearning;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Question {
    public Question() {

    }

    public String idAuthor = "";
    public String title;
    public String content;
    public String id;
    public String image;
    public long time;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIdAuthor() {
        return idAuthor;
    }

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


    public Question(String id, String author, String title, String content, long time) {
        this.id = id;
        this.idAuthor = author;
        this.title = title;
        this.content = content;
        this.time = time;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("content", content);
        return result;
    }
}
