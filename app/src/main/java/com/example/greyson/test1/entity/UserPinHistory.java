package com.example.greyson.test1.entity;

import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;
import java.util.List;

/**
 * Created by greyson on 13/4/17.
 */

public class UserPinHistory implements Serializable{
    private String username;
    private List<MyMarker> mmk;

    public UserPinHistory(String username, List<MyMarker> mmk) {
        this.username = username;
        this.mmk = mmk;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<MyMarker> getMmk() {
        return mmk;
    }

    public void setMmk(List<MyMarker> mmk) {
        this.mmk = mmk;
    }
}
