package com.example.forumlearning;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Question {
    public Question() {

    }

    private String idAuthor = "";
    private String title;
    private String content;
    private String id;
    private long time;
    private List<String> listUserVoteUp = new ArrayList<String>();
    private List<String> listUserVoteDown = new ArrayList<String>();

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setListUserVoteUp(List<String> listUserVoteUp) {
        this.listUserVoteUp = listUserVoteUp;
    }

    public void setListUserVoteDown(List<String> listUserVoteDown) {
        this.listUserVoteDown = listUserVoteDown;
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

    public List<String> getListUserVoteUp() {
        return listUserVoteUp;
    }

    public List<String> getListUserVoteDown() {
        return listUserVoteDown;
    }

    public int getVoteScores() {
        return listUserVoteUp.size() - listUserVoteDown.size();
    }

    public int getVoteUpCount() {
        return listUserVoteUp.size();
    }

    public int getVoteDownCount() {
        return listUserVoteDown.size();
    }

    public boolean isUserVoteUp(String idUser) {
        return listUserVoteUp.contains(idUser);
    }

    public boolean isUserVoteDown(String idUser) {
        return listUserVoteDown.contains(idUser);
    }

    public void setUserVoteUp(String idUser) {
        removeUserVoteDown(idUser);
        listUserVoteUp.add(idUser);
    }

    public void setUserVoteDown(String idUser) {
        removeUserVoteUp(idUser);
        listUserVoteDown.add(idUser);
    }

    public void removeUserVoteUp(String idUser) {
        try {
            listUserVoteUp.remove(idUser);
        } catch (Exception ignored) {
        }
    }

    public void removeUserVoteDown(String idUser) {
        try {
            listUserVoteDown.remove(idUser);
        } catch (Exception ignored) {
        }
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
        result.put("listUserVoteUp", listUserVoteUp);
        result.put("listUserVoteDown", listUserVoteDown);
        return result;
    }
}
