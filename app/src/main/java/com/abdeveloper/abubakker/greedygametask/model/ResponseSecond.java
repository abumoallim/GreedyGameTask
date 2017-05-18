package com.abdeveloper.abubakker.greedygametask.model;

import java.util.List;

/**
 * Created by abubakker on 18/5/17.
 */

public class ResponseSecond {

    List<String> latLongs;

    public ResponseSecond(List<String> latLongs) {
        this.latLongs = latLongs;
    }

    public List<String> getLatLongs() {
        return latLongs;
    }

    public void setLatLongs(List<String> latLongs) {
        this.latLongs = latLongs;
    }
}
