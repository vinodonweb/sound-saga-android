package com.vinodsharma.soundsaga;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.vinodsharma.soundsaga.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.GridLayoutManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    //splash screen
    private boolean keepOn = true;
    private BookAdapter audiobookAdapter;
    private ArrayList<Book> audioArrayList = new ArrayList<>();
    private static final long minSplashTime = 2000;
    private long startTime;


    //gridlayout variables
    private int spanCount;

    ActivityMainBinding binding;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        //splash screen setup
        startTime = System.currentTimeMillis();
        SplashScreen.installSplashScreen(this)
                .setKeepOnScreenCondition(() -> System.currentTimeMillis() - startTime < minSplashTime);


        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = 2;
        } else {
            spanCount = 4;
        }

        //set gradiant background
        String startColor = "#FF510001";
        String endColor = "#B0510001";
        int startColorInt = Color.parseColor(startColor);
        int endColorInt = Color.parseColor(endColor);

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] { startColorInt, endColorInt });
        binding.getRoot().setBackground(gradient);

        //download the audio volley
        DownloadAudioVolley.downloadAudioBook(this);

        //set the onClick listener and onLongClick listener
        binding.audioBookRecycler.setOnClickListener(this);
        binding.audioBookRecycler.setOnLongClickListener(this);

        //set the audiobooks adapter and recyclerView view
        audiobookAdapter = new BookAdapter(audioArrayList, this);
        binding.audioBookRecycler.setAdapter(audiobookAdapter);
        binding.audioBookRecycler.setLayoutManager(new GridLayoutManager(this, spanCount));

        //check if internet is available
        if(!isNetworkAvailable()){
            showNoNetworkAlert();
            return;

        }


        //open myBooksActivity
        binding.myBooksIcon.setOnClickListener(v -> {
            // Retrieve the progress list from SharedPreferences
            List<AudioBookProgress> progressList = MyBooksStorage.getProgressList(MainActivity.this);

            if (progressList.isEmpty()) {
                // If empty, show the alert dialog in MainActivity and do NOT launch MyBooksActivity
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                int colorforAlert = getResources().getColor(R.color.splashBg);

                SpannableString title = new SpannableString("My Books self is empty");
                title.setSpan(new ForegroundColorSpan(colorforAlert), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setTitle(title);
                builder.setIcon(R.drawable.logo);
                SpannableString message = new SpannableString("You currently have no audiobooks in progress.");
                message.setSpan(new ForegroundColorSpan(colorforAlert), 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setMessage(message);
                builder.setPositiveButton("OK", (dialogInterface, i) -> {});
                builder.create().show();

            } else {
                // Otherwise, start the MyBooksActivity
                Intent intent = new Intent(MainActivity.this, MyBooksActivity.class);
                startActivity(intent);
            }
        });

    }

    public void updateAudioList(ArrayList<Book> audioArray){
        audioArrayList.clear();;
        audioArrayList.addAll(audioArray);

        audiobookAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        int position = binding.audioBookRecycler.getChildLayoutPosition(view);
        Book selectedBook  = audioArrayList.get(position);

        // Check if there is saved progress for this book.
        AudioBookProgress progress = MyBooksStorage.getProgressForBook(this, selectedBook);

        // Create the intent to launch AudioBookActivity
        Intent intent = new Intent(this, AudioBookActivity.class);
        intent.putExtra("BOOK", selectedBook);

        // If progress exists, pass resume details; otherwise, playback starts from the beginning.
        if (progress != null) {
            intent.putExtra("SeekTime", progress.getSeekTime());
            intent.putExtra("PlaybackSpeed", progress.getPlaybackSpeed());
            intent.putExtra("ChapterNumber", progress.getChapterNumber());
        }

        startActivity(intent);


//        Intent intent = new Intent(this, AudioBookActivity.class);
//        intent.putExtra("BOOK", audio);
//        Log.d(TAG, "Intent sending data: " + audio);
//        startActivity(intent);

    }

    @Override
    public boolean onLongClick(View view){
        int position = binding.audioBookRecycler.getChildLayoutPosition(view);
        Book b = audioArrayList.get(position);

        //get color from Colors folder
        int colorforAlert = getResources().getColor(R.color.splashBg);

        //Alert dialog to show selected book details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Load image
        Glide.with(this)
                .load(b.getImage())
                .into(new SimpleTarget<Drawable>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        builder.setIcon(resource);

                        // Set Title with custom color
                        SpannableString title = new SpannableString(b.getTitle() + " (" + b.getDate() + ") " + b.getAuthor());
                        title.setSpan(new ForegroundColorSpan(colorforAlert), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        builder.setTitle(title);

                        // Set Message with custom color
                        String message = b.getChapters().size() + " Chapters" + "\n\nDuration: " + b.getDuration() + "\nLanguage: " + b.getLanguage();
                        SpannableString messageText = new SpannableString(message);
                        messageText.setSpan(new ForegroundColorSpan(colorforAlert), 0, messageText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        builder.setMessage(messageText);

                        builder.setPositiveButton("OK", (dialogInterface, i) -> {});
                        builder.create().show();
                    }
                });

        return true;
    }

    public void dataNotAvailableAlert(){
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setIcon(R.drawable.logo);

      SpannableString title = new SpannableString("SoundSaga");
        title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.splashBg)), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setTitle(title);
        SpannableString message = new SpannableString("Audio library is not available. Please try again later.");
        message.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.splashBg)), 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialogInterface, i) -> finishAndRemoveTask());
        builder.create().show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

            return networkCapabilities != null &&
                    (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
        }
        return false;
    }

    //network alert
    public void showNoNetworkAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SoundSaga");
        builder.setMessage("Unable to contact the Sound Saga API due to a network problem. " +
                "Please check your connection.");
        builder.setPositiveButton("OK", (dialogInterface, i) -> finishAndRemoveTask());
        builder.create().show();
    }

}