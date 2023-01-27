package com.example.forumlearning;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Comment {
    public Comment() {

    }

    public String getId() {
        return id;
    }

    public String getIdQuestion() {
        return idQuestion;
    }

    public String getContent() {
        return content;
    }

    public long getTime() {
        return time;
    }

    public String getIdAuthor() {
        return idAuthor;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String id;
    public String idQuestion;
    public String idAuthor = "";
    public String content;
    public long time;

    public Comment(String id, String idQuestion, String idAuthor, String content, long time) {
        this.id = id;
        this.idQuestion = idQuestion;
        this.idAuthor = idAuthor;
        this.content = content;
        this.time = time;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("content", content);
        return result;
    }
}
