package com.roichomsky.socialmid;

import java.util.HashMap;

public class Like {
    private String likesCounter;
    private HashMap<String, Object> likedByList;
    private String postID;

    public Like(){}

    public Like(String likesCounter, HashMap<String, Object> likedByList, String postID) {
        this.likesCounter = likesCounter;
        this.likedByList = likedByList;
        this.postID = postID;
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

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }
}
