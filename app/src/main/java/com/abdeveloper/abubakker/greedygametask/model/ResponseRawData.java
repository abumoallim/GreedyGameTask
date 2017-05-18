package com.abdeveloper.abubakker.greedygametask.model;

/**
 * Created by abubakker on 18/5/17.
 */

public class ResponseRawData {

    String ipaddress;
    String time;

    public ResponseRawData(String ipaddress, String time) {
        this.ipaddress = ipaddress;
        this.time = time;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
