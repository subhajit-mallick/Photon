package com.subhajit.photon.models;

import com.google.firebase.database.Exclude;

public class row {


    private String img;

    @Exclude
    public String id,thumb;
    @Exclude
    public boolean isFavourite;

    public row(String img, String thumb, String id) {
        this.img = img;
        this.id = id;
        this.thumb = thumb;
    }

    public String getImage_url() {
        return img;
    }

    public String getThumb_url() {
        return thumb;
    }

}