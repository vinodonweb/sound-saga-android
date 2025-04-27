package com.vinodsharma.soundsaga;


import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;

public class DownloadAudioVolley {

    public static final String audioBook = "https://christopherhield.com/ABooks/abook_contents.json";
    private static final String TAG = "DownloadAudioVolley";

    public static void downloadAudioBook(MainActivity mainActivity) {
        RequestQueue queue = Volley.newRequestQueue(mainActivity);

        Uri.Builder buildURL = Uri.parse(audioBook).buildUpon();
        String urlToUse = buildURL.build().toString();
        Log.d(TAG, "downloadStops: " + urlToUse);

        Response.Listener<JSONArray> listener = response -> {
            handleSuccess(mainActivity, response.toString());
        };

        Response.ErrorListener error = error1 -> handleFail(mainActivity, error1);

        // Request a JSON array response from the provided URL.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, urlToUse, null, listener, error);

        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }

    private static void handleSuccess(MainActivity mainActivity,  String response) {
        try {

            JSONArray responseArray = new JSONArray(response);

            ArrayList<Book> audioArrayList = new ArrayList<>();

            // Loop through each item in the JSON array
            for (int i = 0; i < responseArray.length(); i++) {
                JSONObject audioBookJson = responseArray.getJSONObject(i);

                // Extract the fields for each audio book
                String title = audioBookJson.getString("title");
                String author = audioBookJson.getString("author");
                String date = audioBookJson.getString("date");
                String language = audioBookJson.getString("language");
                String duration = audioBookJson.getString("duration");
                String imageUrl = audioBookJson.getString("image");

                // Log the data for debugging
                Log.d(TAG, "Book Book: " + title + " by " + author);


                // Get the chapters array
                JSONArray chaptersArray = audioBookJson.getJSONArray("contents");
                ArrayList<Chapter> chapterArrayList = new ArrayList<>();

                // Loop through the chapters and create Chapter objects
                for (int j = 0; j < chaptersArray.length(); j++) {
                    JSONObject chapterJson = chaptersArray.getJSONObject(j);

                    int chapterNumber = chapterJson.getInt("number");
                    String chapterTitle = chapterJson.getString("title");
                    String chapterUrl = chapterJson.getString("url");

                    // Create a Chapter object and add it to the Book book
                    Chapter chapter = new Chapter(chapterNumber, chapterTitle, chapterUrl);
                    chapterArrayList.add(chapter);
                }
                //create audio object with the fetched data
                Book audioBook = new Book(title, author, date, language, duration, imageUrl, chapterArrayList);

                // Add the audio book to the list
                audioArrayList.add(audioBook);
            }

            mainActivity.updateAudioList(audioArrayList);

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing audio book response", e);
            mainActivity.dataNotAvailableAlert();
        }

    }

    private static void handleFail(MainActivity mainActivity, VolleyError error) {
        Log.d(TAG, "handleFail: " + error.getMessage());
    }
}
