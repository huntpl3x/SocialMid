package com.roichomsky.socialmid;

public class User {

    //use same name as in firebase database
    private String uid;
    private String name;
    private String email;
    private String image;
    private String cover;

    public User(){

    }

    public User(String uid, String name, String email, String search, String image, String cover) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.image = image;
        this.cover = cover;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}
