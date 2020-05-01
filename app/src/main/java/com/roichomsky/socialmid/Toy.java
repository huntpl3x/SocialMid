package com.roichomsky.socialmid;

import android.graphics.Bitmap;

public class Toy {
    private int price;
    private String name;
    private Bitmap bitmap;

    public Toy(){}

    public Toy(int price, String name, Bitmap bitmap) {
        this.price = price;
        this.name = name;
        this.bitmap = bitmap;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
