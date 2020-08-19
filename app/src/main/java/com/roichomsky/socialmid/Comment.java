package com.roichomsky.socialmid;

public class Comment {
    private String postID;
    private String publisherID;
    private String content;

    public Comment(){}

    public Comment(String postID, String publisherID, String content) {
        this.postID = postID;
        this.publisherID = publisherID;
        this.content = content;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getPublisherID() {
        return publisherID;
    }

    public void setPublisherID(String publisherID) {
        this.publisherID = publisherID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
