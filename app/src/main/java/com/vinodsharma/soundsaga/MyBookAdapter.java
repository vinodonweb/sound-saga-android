package com.vinodsharma.soundsaga;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.vinodsharma.soundsaga.databinding.ActivityMyBookEntryBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyBookAdapter extends RecyclerView.Adapter<MyBookViewHolder> {

    private final List<AudioBookProgress> progressList;
    private final Context context;

    public MyBookAdapter(List<AudioBookProgress> progressList, Context context) {
        this.progressList = progressList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ActivityMyBookEntryBinding binding = ActivityMyBookEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new MyBookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyBookViewHolder holder, int position) {
        AudioBookProgress progress = progressList.get(position);
        Book book = progress.getBook();

        int chapterIndex = progress.getChapterNumber();
        if (chapterIndex < 0 || chapterIndex >= book.getChapters().size()) {
            // Handle the error, for example, by setting a default text or using the last chapter
            chapterIndex = book.getChapters().size() - 1;
        }

        // Bind data to views
        holder.binding.mybookTitle.setText(book.getTitle());
        holder.binding.mybookAuthor.setText(book.getAuthor());
//        holder.binding.mybookCurrentChap.setText(book.getChapters().get(progress.getChapterNumber()).getTitle());
        holder.binding.mybookCurrentChap.setText((book.getChapters().get(chapterIndex).getTitle()));

        Glide.with(context).load(book.getImage()).into(holder.binding.mybookImage);

        // Format current playback time and chapter duration (assumed to be in ms)
        String currentTime = formatTime(progress.getSeekTime());
        String totalTime = formatTime(progress.getChapterDuration());
        holder.binding.currentPlyTime.setText(String.format("%s of %s", currentTime, totalTime));

        // Format last played date/time from the saved timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy, HH:mm", Locale.getDefault());
        String lastPlayedStr = sdf.format(new Date(progress.getTimestamp()));
        holder.binding.dateTimeLastPlayed.setText(lastPlayedStr);

        // Click listener: resume playback from saved progress.
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AudioBookActivity.class);
            // Pass the full Book object as before.
            intent.putExtra("BOOK", book);
            intent.putExtra("SeekTime", progress.getSeekTime());
            intent.putExtra("PlaybackSpeed", progress.getPlaybackSpeed());
            intent.putExtra("ChapterNumber", progress.getChapterNumber());
            context.startActivity(intent);
        });

        //set marqueee test
        holder.binding.mybookCurrentChap.setSelected(true);

        // Long click listener: ask for confirmation to delete the entry.
        holder.itemView.setOnLongClickListener(v -> {
            // Capture the book's title at the current position
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return false;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            Glide.with(context)
                    .load(book.getImage())
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                            builder.setIcon(resource);
                            SpannableString title = new SpannableString("Remove your book history for " + book.getTitle() + "?");
                            title.setSpan(new ForegroundColorSpan(context.getColor(R.color.splashBg)),
                                    0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            builder.setTitle(title);
                            builder.setNegativeButton("Cancel", null);
                            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                                // Use the current adapter position here as well
                                int posToDelete = holder.getAdapterPosition();
                                if (posToDelete != RecyclerView.NO_POSITION) {
                                    deleteItem(posToDelete);
                                }
                            });
                            builder.show();
                        }
                    });
            return true;
        });
}

    // Remove an entry and update SharedPreferences.
    private void deleteItem(int position) {
        if(position < 0 || position >= progressList.size()) {
            return;
        }

        AudioBookProgress progress = progressList.get(position);
        progressList.remove(position);
        notifyItemRemoved(position);
        // Update SharedPreferences after deletion
        MyBooksStorage.removeProgress(context, progress);
    }


    private String formatTime(int ms) {
        int seconds = (ms / 1000) % 60;
        int minutes = (ms / (1000 * 60)) % 60;
//        int hours = ms / (1000 * 60 * 60);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return progressList.size();
    }
}
