package com.roichomsky.socialmid;

import java.util.ArrayList;

public class Post {
    private String postID;
    private String postURL;
    private String description;
    private String publisherID;
    private String likesCounter;

    public Post(){ }

    public Post(String description,String postID, String postURL, String publisherID, String likesCounter) {
        this.postID = postID;
        this.postURL = postURL;
        this.description = description;
        this.publisherID = publisherID;
        this.likesCounter = likesCounter;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getPostURL() {
        return postURL;
    }

    public void setPostURL(String postURL) {
        this.postURL = postURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisherID() {
        return publisherID;
    }

    public void setPublisherID(String publisherID) {
        this.publisherID = publisherID;
    }

    public String getLikesCounter() {
        return likesCounter;
    }

    public void setLikesCounter(String likesCounter) {
        this.likesCounter = likesCounter;
    }
}
