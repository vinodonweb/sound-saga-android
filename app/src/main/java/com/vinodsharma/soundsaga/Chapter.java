package com.vinodsharma.soundsaga;

import java.io.Serializable;

public class Chapter implements Serializable {
    private int number;
    private String title;
    private String url;

    public Chapter(int number, String title, String url) {
        this.number = number;
        this.title = title;
        this.url = url;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "number=" + number +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
