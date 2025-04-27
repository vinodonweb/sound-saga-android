package com.vinodsharma.soundsaga;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MyBooksStorage {
    private static final String PREFS_NAME = "MyBooksPrefs";
    private static final String KEY_PROGRESS_LIST = "progress_list";

    // Save the list (using Gson)
    public static void saveProgressList(Context context, List<AudioBookProgress> progressList) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(progressList);
        editor.putString(KEY_PROGRESS_LIST, json);
        editor.apply();
    }

    // Retrieve the list (or an empty list if nothing stored yet)
    public static List<AudioBookProgress> getProgressList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PROGRESS_LIST, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<AudioBookProgress>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    // Add a new progress entry or update the existing one for the same book (identified by title)
    public static void addOrUpdateProgress(Context context, AudioBookProgress progress) {
        List<AudioBookProgress> list = getProgressList(context);
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            // Here we use the book title as a unique identifier. You may wish to use an ID instead.
            if (list.get(i).getBook().getTitle().equals(progress.getBook().getTitle())) {
                list.set(i, progress);
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(progress);
        }
        // Sort the list by timestamp descending (most recent first)
        Collections.sort(list, (p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
        saveProgressList(context, list);
    }

    // Remove a progress entry (by matching the book title)
    public static void removeProgress(Context context, AudioBookProgress progress) {
        List<AudioBookProgress> list = getProgressList(context);
        Iterator<AudioBookProgress> it = list.iterator();
        while (it.hasNext()) {
            AudioBookProgress p = it.next();
            if (p.getBook().getTitle().equals(progress.getBook().getTitle())) {
                it.remove();
                break;
            }
        }
        saveProgressList(context, list);
    }

    // Retrieve the progress entry for a specific book (by title)
    public static AudioBookProgress getProgressForBook(Context context, Book book) {
        List<AudioBookProgress> progressList = getProgressList(context);
        for (AudioBookProgress progress : progressList) {
            // Assuming the book title uniquely identifies the book
            if (progress.getBook().getTitle().equals(book.getTitle())) {
                return progress;
            }
        }
        return null;
    }
}

