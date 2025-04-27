package com.vinodsharma.soundsaga;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;

import com.vinodsharma.soundsaga.databinding.ActivityMyBooksBinding;

import java.util.ArrayList;
import java.util.List;

public class MyBooksActivity extends AppCompatActivity {

    ActivityMyBooksBinding binding;
    private List<AudioBookProgress> progressList;
    private MyBookAdapter adapter;
    private int spanCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMyBooksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(!isNetworkAvailable()){
            showNoNetworkAlert();
            return;
        }

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = 1;
        } else {
            spanCount = 2;
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

        // Retrieve the progress list from SharedPreferences
        progressList = MyBooksStorage.getProgressList(this);

        if (progressList.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("My Books list is empty")
                    .setMessage("You currently have no audiobooks in progress.")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        } else {
            adapter = new MyBookAdapter(progressList, this);
            binding.myBookRecycler.setLayoutManager(new GridLayoutManager(this, spanCount));
            binding.myBookRecycler.setAdapter(adapter);
        }

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
                "Please check your connection and try again.");
        builder.setPositiveButton("OK", (dialogInterface, i) -> finishAndRemoveTask());
        builder.create().show();
    }

}