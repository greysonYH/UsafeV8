package com.example.greyson.test1.entity;

import java.io.Serializable;

/**
 * Created by greyson on 15/4/17.
 */

public class MyMarker implements Serializable {
    private String mkTag;
    private String mkLat;
    private String mkLnt;
    private String mkColor;
    private String mkDescription;

    public MyMarker(String mkTag, String mkLat, String mkLnt, String mkColor, String mkDescription) {
        this.mkTag = mkTag;
        this.mkLat = mkLat;
        this.mkLnt = mkLnt;
        this.mkColor = mkColor;
        this.mkDescription = mkDescription;
    }

    public String getMkTag() {
        return mkTag;
    }

    public void setMkTag(String mkTag) {
        this.mkTag = mkTag;
    }

    public String getMkLat() {
        return mkLat;
    }

    public void setMkLat(String mkLat) {
        this.mkLat = mkLat;
    }

    public String getMkLnt() {
        return mkLnt;
    }

    public void setMkLnt(String mkLnt) {
        this.mkLnt = mkLnt;
    }

    public String getMkColor() {
        return mkColor;
    }

    public void setMkColor(String mkColor) {
        this.mkColor = mkColor;
    }

    public String getMkDescription() {
        return mkDescription;
    }

    public void setMkDescription(String mkDescription) {
        this.mkDescription = mkDescription;
    }
}
