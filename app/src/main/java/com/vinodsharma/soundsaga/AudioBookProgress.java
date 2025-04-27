package com.vinodsharma.soundsaga;

import java.io.Serializable;
import java.util.Date;

public class AudioBookProgress implements Serializable {
    private final Book book;           // Full Book object (with chapters)
    private final String chapterTitle;
    private final int chapterNumber;
    private final int seekTime;        // in milliseconds
    private final long timestamp;      // when the progress was saved (millis)
    private final float playbackSpeed;
    private final int chapterDuration; // in milliseconds

    public AudioBookProgress(Book book, String chapterTitle, int chapterNumber, int seekTime, long timestamp, float playbackSpeed, int chapterDuration) {
        this.book = book;
        this.chapterTitle = chapterTitle;
        this.chapterNumber = chapterNumber;
        this.seekTime = seekTime;
        this.timestamp = timestamp;
        this.playbackSpeed = playbackSpeed;
        this.chapterDuration = chapterDuration;
    }

    public Book getBook() {
        return book;
    }
    public String getChapterTitle() {
        return chapterTitle;
    }
    public int getChapterNumber() {
        return chapterNumber;
    }
    public int getSeekTime() {
        return seekTime;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public float getPlaybackSpeed() {
        return playbackSpeed;
    }
    public int getChapterDuration() {
        return chapterDuration;
    }
}