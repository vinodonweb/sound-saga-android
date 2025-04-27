package com.vinodsharma.soundsaga;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.vinodsharma.soundsaga.databinding.ActivityAudioBookBinding;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AudioBookActivity extends AppCompatActivity {

    public MediaPlayer player;
    private int startTime;
    private float speed;
    private String url;
    private Timer timer;
    private PopupMenu popupMenu;
    private int mediaCounter = 1;
    private Book audio;
    private static final String TAG = "AudioBookActivity";
    ActivityAudioBookBinding binding;
    // Flag to determine if we are loading the saved state for the first time.
    private boolean isInitialLoad = true;
    private CountDownTimer sleepTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //check network
        if(!isNetworkAvailable()){
            showNoNetworkAlert();
            return;
        }

        if (savedInstanceState != null) {
            updateFromBundle(savedInstanceState);
        }

        //sleep timer
        binding.sleepTimerIndicator.setOnClickListener(v -> showSleepTimerOptions());


        //set gradiant background
        String startColor = "#FF510001";
        String endColor = "#B0510001";
        int startColorInt = Color.parseColor(startColor);
        int endColorInt = Color.parseColor(endColor);

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] { startColorInt, endColorInt });
        binding.getRoot().setBackground(gradient);


        // Get the Book object from Intent
        Intent intent = getIntent();
        audio = (Book) intent.getSerializableExtra("BOOK");
        if (audio == null) {
            Log.e(TAG, "Book object is null");
            showPlaybackErrorAndReturn();
        } else {
            Log.d(TAG, "Received Book: " + audio.getTitle() + " with chapters: " + audio.getChapters().size());
        }

        // Initialize MediaPlayer early so it's ready for playback.
        player = new MediaPlayer();
        player.setOnErrorListener((mp, what, extra) -> {
            Log.d(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
            showPlaybackErrorAndReturn();
            return true;
        });
        player.setOnCompletionListener(mediaPlayer -> {
            if (timer != null) timer.cancel();
            int currentIndex = binding.viewPager.getCurrentItem();
            if (currentIndex < audio.getChapters().size() - 1) {
                binding.viewPager.setCurrentItem(currentIndex + 1, true);
            } else {
                showChapterFinishedAlert();
            }
        });

        // Set up ViewPager2 with its adapter.
        viewpagerAdapter adapter = new viewpagerAdapter(audio.getChapters(), audio.getImage());
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateNavigationButtons();
                Chapter chapter = audio.getChapters().get(position);
                url = chapter.getUrl();
                // If this isn’t the very first load (i.e. after resuming saved progress),
                // reset startTime to 0.
                if (!isInitialLoad) {
                    startTime = 0;
                }
                playIt();
            }
        });

        // Marquee title for audioPlayingTitle
        binding.audioPlayingTitle.setSelected(true);

        // Set up speed menu and seek bar.
        setupSpeedMenu();
        setupSeekBar();

        // Restore saved state from the Intent extras.
        presetVars();


            // Handle saved instance state to restore playback position and speed
        if (savedInstanceState != null) {
            updateFromBundle(savedInstanceState);
        }

    }

    //sleep timer
    public void sleepTimerClick(View v) {
        showSleepTimerOptions();
    }

    private void showSleepTimerOptions() {
        if (sleepTimer != null) {
            // A sleep timer is active: allow user to change or cancel it.
            new AlertDialog.Builder(this)
                    .setTitle("Sleep Timer")
                    .setMessage("A sleep timer is active. What would you like to do?")
                    .setPositiveButton("Change Timer", (dialog, which) -> showSleepTimerSelectionDialog())
                    .setNegativeButton("Cancel Timer", (dialog, which) -> cancelSleepTimer())
                    .setNeutralButton("Keep Timer", null)
                    .show();
        } else {
            // No timer active: show the selection dialog.
            showSleepTimerSelectionDialog();
        }
    }


//      Displays a dialog for the user to select a sleep timer duration
    private void showSleepTimerSelectionDialog() {
        final String[] options = {
               "5 minutes", "10 minutes", "20 minutes", "30 minutes", "45 minutes", "60 minutes", "Until End of Chapter"
        };

        new AlertDialog.Builder(this)
                .setTitle("Set Sleep Timer")
                .setItems(options, (dialog, which) -> {
                    long millis;
                    if (which == options.length - 1) {
                        // "Until End of Chapter" option.
                        if (player != null && player.isPlaying()) {
                            int remaining = player.getDuration() - player.getCurrentPosition();
                            if (remaining <= 0) {
                                Toast.makeText(AudioBookActivity.this, "Chapter is nearly finished.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            millis = remaining;
                        } else {
                            Toast.makeText(AudioBookActivity.this, "Player not active.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        // Parse minutes from the option string.
                        String minutesStr = options[which].split(" ")[0];
                        int minutes = Integer.parseInt(minutesStr);
                        millis = minutes * 60 * 1000L;
                    }
                    startSleepTimer(millis);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Starts (or restarts) the sleep timer with the specified duration.
     *
     * @param millis Duration in milliseconds.
     */
    private void startSleepTimer(long millis) {
        // Cancel any existing timer.
        if (sleepTimer != null) {
            sleepTimer.cancel();
        }

        sleepTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the sleep timer indicator (mm:ss format).
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                int minutes = (int) (millisUntilFinished / (1000 * 60));
                binding.sleepTimerIndicator.setText(String.format("Sleep Timer: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                binding.sleepTimerIndicator.setText("Sleep Timer: Off");
                sleepTimer = null;
                if (player != null && player.isPlaying()) {
                    player.pause();
                    binding.playPauseBtn.setImageResource(R.drawable.play);
                    Toast.makeText(AudioBookActivity.this, "Sleep Timer finished – playback paused.", Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    /**
     * Cancels the active sleep timer and updates the UI.
     */
    private void cancelSleepTimer() {
        if (sleepTimer != null) {
            sleepTimer.cancel();
            sleepTimer = null;
            binding.sleepTimerIndicator.setText("Sleep Timer: Off");
            Toast.makeText(AudioBookActivity.this, "Sleep Timer cancelled.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChapterFinishedAlert(){

        int colorforAlert = (R.color.splashBg);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo);
        SpannableString title = new SpannableString("Chapter Finished");
        title.setSpan(new ForegroundColorSpan(getResources().getColor(colorforAlert)), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setTitle(title);
        SpannableString message = new SpannableString("You have reached the end of this chapter. you will be redirected to Home screen..");
        message.setSpan(new ForegroundColorSpan(getResources().getColor(colorforAlert)), 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", (dialog, which) -> {
            // Go back to the main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        builder.create().show();
    }


    @Override
    protected void onPause() {
        super.onPause();
//        savePlayedBook();  // Save the played book information when leaving the activity
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (timer != null) {
            timer.cancel();
        }

        // Cancel sleep timer if active.
        if (sleepTimer != null) {
            sleepTimer.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (player != null && player.isPlaying()) {
            player.stop();
        }
        savePlayedBook();
        super.onBackPressed();
    }

    private void savePlayedBook() {
        int currentIndex = binding.viewPager.getCurrentItem();
        if (currentIndex == -1) return;
        // If on last chapter and playback finished, skip saving.
        if (currentIndex == audio.getChapters().size() - 1 && player.getCurrentPosition() == player.getDuration()) {
            return;
        }
        Chapter currentChapter = audio.getChapters().get(currentIndex);
        AudioBookProgress progress = new AudioBookProgress(
                audio,
                currentChapter.getTitle(),
                currentChapter.getNumber(),
                player.getCurrentPosition(),
                System.currentTimeMillis(),
                speed,
                player.getDuration()
        );
        MyBooksStorage.addOrUpdateProgress(this, progress);
    }

    private String getPrev() {
        // Get the current chapter index
        int currentChapterIndex = getCurrentChapterIndex();

        // Check if the current chapter is the first one
        if (currentChapterIndex > 0) {
            // Return the URL of the previous chapter
            return audio.getChapters().get(currentChapterIndex - 1).getUrl();
        } else {
            // If the first chapter, you can loop back to the last chapter or handle accordingly
            return audio.getChapters().get(audio.getChapters().size() - 1).getUrl();  // or show a message indicating it's the first chapter
        }
    }

    private int getCurrentChapterIndex() {
        // Iterate over the chapters to find the index of the current URL
        for (int i = 0; i < audio.getChapters().size(); i++) {
            if (audio.getChapters().get(i).getUrl().equals(url)) {
                return i;
            }
        }
        return -1; // URL not found
    }

    private void updateFromBundle(Bundle bundle) {
        url = bundle.getString("URL");
        startTime = bundle.getInt("TIME");
        speed = bundle.getFloat("SPEED");
    }

    public void playIt() {
        if (audio == null || audio.getChapters() == null || audio.getChapters().isEmpty()) {
            Log.e(TAG, "playIt: Book or chapters are null/empty");
            showPlaybackErrorAndReturn();
            return;
        }

        binding.audioPlayingTitle.setText(audio.getTitle());
        binding.audioPlayingTitle.setSelected(true);

        try {
            if (player.isPlaying()) {
                player.stop();
            }
            player.reset();
            if (url != null) {
                player.setDataSource(url);
            } else {
                showPlaybackErrorAndReturn();
                return;
            }
            player.prepare();
            int dur = player.getDuration();
            binding.seekBar.setMax(dur);

            player.seekTo(startTime);
            player.start();
            player.setPlaybackParams(player.getPlaybackParams().setSpeed(speed));

            updateNavigationButtons();
            timerCounter();

            // After the first load, mark that we are no longer resuming from saved progress.
            isInitialLoad = false;
        } catch (Exception e) {
            Log.d(TAG, "playIt error: " + e.getMessage());
            showPlaybackErrorAndReturn();
        }
    }


    private void presetVars() {
        Intent intent = getIntent();

        // Set playback speed and seek time if provided.
        speed = intent.getFloatExtra("PlaybackSpeed", 1.0f);
        if (intent.hasExtra("SeekTime")) {
            startTime = intent.getIntExtra("SeekTime", 0);
        } else {
            startTime = 0;
        }

        // If a chapter number was saved, set the ViewPager2 to that chapter.
        if (intent.hasExtra("ChapterNumber")) {
            int chapterNumber = intent.getIntExtra("ChapterNumber", 1);
            int index = chapterNumber - 1; // assuming chapters are 1-indexed
            if (index >= 0 && index < audio.getChapters().size()) {
                binding.viewPager.setCurrentItem(index, false);
                url = audio.getChapters().get(index).getUrl();
            } else {
                binding.viewPager.setCurrentItem(0, false);
                url = audio.getChapters().get(0).getUrl();
            }
        } else {
            binding.viewPager.setCurrentItem(0, false);
            url = audio.getChapters().get(0).getUrl();
        }
        binding.speed.setText(String.valueOf(speed));
        updateNavigationButtons();
    }

    public void goBack(View v) {
        if (player != null && player.isPlaying()) {
            int pos = player.getCurrentPosition();
            pos -= 15000;
            if (pos < 0)
                pos = 0;
            player.seekTo(pos);
        }
    }

    public void goForward(View v) {
        if (player != null && player.isPlaying()) {
            int pos = player.getCurrentPosition();
            pos += 15000;
            if (pos > player.getDuration())
                pos = player.getDuration();
            player.seekTo(pos);
        }
    }

    public void speedClick(View v) {
        popupMenu.show();
    }

    private void setupSpeedMenu() {
        popupMenu = new PopupMenu(this, binding.speed);
        popupMenu.getMenuInflater().inflate(R.menu.speed_popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_075) {
                speed = 0.75f;
            } else if (menuItem.getItemId() == R.id.menu_1) {
                speed = 1f;
            } else if (menuItem.getItemId() == R.id.menu_11) {
                speed = 1.1f;
            } else if (menuItem.getItemId() == R.id.menu_125) {
                speed = 1.25f;
            } else if (menuItem.getItemId() == R.id.menu_15) {
                speed = 1.5f;
            } else if (menuItem.getItemId() == R.id.menu_175) {
                speed = 1.75f;
            } else if (menuItem.getItemId() == R.id.menu_2) {
                speed = 2f;
            }

            binding.speed.setText(menuItem.getTitle());

            try {
                player.setPlaybackParams(player.getPlaybackParams().setSpeed(speed));
            } catch (Exception e) {
                Log.d(TAG, "Error setting playback speed: " + e.getMessage());
                showPlaybackErrorAndReturn();
            }
            return true;
        });
    }

    public void doPlayPause(View v) {
        if (player.isPlaying()) {
            player.pause();
            binding.playPauseBtn.setImageResource(R.drawable.play);
        } else {
            player.start();
            binding.playPauseBtn.setImageResource(R.drawable.pause);
        }
    }

    private void updateNavigationButtons() {
        int currentIndex = binding.viewPager.getCurrentItem();
        if (currentIndex == 0) {
            binding.preTrack.setVisibility(View.INVISIBLE);
        } else {
            binding.preTrack.setVisibility(View.VISIBLE);
        }

        if (currentIndex == audio.getChapters().size() - 1) {
            binding.nextTrack.setVisibility(View.INVISIBLE);
        } else {
            binding.nextTrack.setVisibility(View.VISIBLE);
        }
    }

    public void doNext(View v) {
        int currentIndex = binding.viewPager.getCurrentItem();
        if (currentIndex < audio.getChapters().size() - 1) {
            binding.viewPager.setCurrentItem(currentIndex + 1, true);
        }
    }

    public void doPrev(View v) {
        int currentIndex = binding.viewPager.getCurrentItem();
        if (currentIndex > 0) {
            binding.viewPager.setCurrentItem(currentIndex - 1, true);
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("URL", url);
        outState.putInt("TIME", player.getCurrentPosition());
        outState.putFloat("SPEED", speed);
        super.onSaveInstanceState(outState);
    }

    private void timerCounter() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (player != null && player.isPlaying()) {
                        binding.seekBar.setProgress(player.getCurrentPosition());
                        binding.startPlayTime.setText(getTimeFormat(player.getCurrentPosition()));
                        binding.endTime.setText(getTimeFormat(player.getDuration()));
                    }
                });
            }
        };
        timer.schedule(task, 0, 1000);
    }

    private String getTimeFormat(int ms) {
        int t = ms;
        int h = ms / 3600000;
        t -= (h * 3600000);
        int m = t / 60000;
        t -= (m * 60000);
        int s = t / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }

    private void setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                        // Don't need
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // Don't need
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int progress = seekBar.getProgress();
                        player.seekTo(progress);
                    }
                });
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


    private void showPlaybackErrorAndReturn() {
        runOnUiThread(() -> {
            new android.app.AlertDialog.Builder(AudioBookActivity.this)
                    .setTitle("Playback Error")
                    .setMessage("Unable to play the audiobook chapter. Returning to the main menu.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        Intent intent = new Intent(AudioBookActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        });
    }

}