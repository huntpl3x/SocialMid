package com.roichomsky.socialmid;

public class Post {
    private String postID;
    private String image;
    private String description;
    private String publisherID;

    public Post(){

    }

    public Post(String postID, String image, String description, String publisherID) {
        this.postID = postID;
        this.image = image;
        this.description = description;
        this.publisherID = publisherID;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
}
