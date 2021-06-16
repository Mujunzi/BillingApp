package com.lenovo.billing.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class DeviceStatus implements Serializable{

    private String storeId;
    private String appId;
    private int locationId;
    private int code;
    private boolean isEmpty;
    private ArrayList<Report> data;

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public ArrayList<Report> getData() {
        return data;
    }

    public void setData(ArrayList<Report> data) {
        this.data = data;
    }
}
