package com.vinodsharma.soundsaga;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public class Book implements Serializable {
    private final String title;
    private final String author;
    private final String date;
    private final String language;
    private final String duration;
    private final String image;
    private ArrayList<Chapter> chapters;

    public Book(String title, String author, String date, String language, String duration, String image, ArrayList<Chapter> chapters) {
        this.title = title;
        this.author = author;
        this.date = date;
        this.language = language;
        this.duration = duration;
        this.image = image;
        this.chapters = chapters;
    }

    // Add a method to add chapters to the audio book
    public void addChapter(Chapter chapter) {
        chapters.add(chapter);
    }

    // Getter for chapters
    public ArrayList<Chapter> getChapters() {
        return chapters;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getLanguage() {
        return language;
    }

    public String getDuration() {
        return duration;
    }

    public String getImage() {
        return image;
    }

    @NonNull
    @Override
    public String toString() {
        return "Book title=" + title;
    }
}


