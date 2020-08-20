package com.roichomsky.socialmid;

public class Comment {
    private String publisherID;
    private String content;
    private String commentID;

    public Comment(){}

    public Comment(String publisherID, String content, String commentID) {
        this.publisherID = publisherID;
        this.content = content;
        this.commentID = commentID;
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

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }
}
