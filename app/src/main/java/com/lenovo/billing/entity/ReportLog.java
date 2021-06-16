package com.lenovo.billing.entity;

public class ReportLog implements Report{

    private String time;
    private String tag;
    private String text;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    //
    // for debug.
    //

    @Override
    public String toString() {
        return "ReportLog{" +
                "time='" + time + '\'' +
                ", tag='" + tag + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
