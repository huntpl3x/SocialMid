package com.roichomsky.socialmid;

import java.util.HashMap;
import java.util.List;

public class Like {
    private String likesCounter;
    private HashMap<String, Object> likedByList;

    public Like(){}

    public Like(String likesCounter, HashMap<String, Object> likedByList) {
        this.likesCounter = likesCounter;
        this.likedByList = likedByList;
    }

    public String getLikesCounter() {
        return likesCounter;
    }

    public void setLikesCounter(String likesCounter) {
        this.likesCounter = likesCounter;
    }

    public HashMap<String, Object> getLikedByList() {
        return likedByList;
    }

    public void setLikedByList(HashMap<String, Object> likedByList) {
        this.likedByList = likedByList;
    }
}
