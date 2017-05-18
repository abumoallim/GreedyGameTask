package com.abdeveloper.abubakker.greedygametask.model;

import java.util.List;

/**
 * Created by abubakker on 18/5/17.
 */

public class ResponseMinute {
    List<ResponseSecond> seconds;
    Boolean isClickable;

    public ResponseMinute(List<ResponseSecond> seconds, Boolean isClickable) {
        this.seconds = seconds;
        this.isClickable = isClickable;
    }

    public List<ResponseSecond> getSeconds() {
        return seconds;
    }

    public void setSeconds(List<ResponseSecond> seconds) {
        this.seconds = seconds;
    }

    public Boolean getClickable() {
        return isClickable;
    }

    public void setClickable(Boolean clickable) {
        isClickable = clickable;
    }
}
