package com.vinodsharma.soundsaga;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vinodsharma.soundsaga.databinding.ViewpagerEntryBinding;

import java.util.ArrayList;

public class viewpagerAdapter  extends RecyclerView.Adapter<viewpagerViewHolder> {

    private final ArrayList<Chapter> chapters;
    private final String bookImageUrl;

    public viewpagerAdapter(ArrayList<Chapter> chapters, String bookImageUrl) {
        this.chapters = chapters;
        this.bookImageUrl = bookImageUrl;
    }

    @NonNull
    @Override
    public viewpagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewpagerEntryBinding binding = ViewpagerEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new viewpagerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull viewpagerViewHolder holder , int position) {


        Chapter chapter = chapters.get(position);

        Glide.with(holder.binding.getRoot()).load(bookImageUrl).into(holder.binding.audioImage);

        @SuppressLint("DefaultLocale")
        String chapterText = String.format("%s (%d of %d)",
                chapter.getTitle(),
                chapter.getNumber(),
                chapters.size());
        holder.binding.chapterCount.setSelected(true);
        holder.binding.chapterCount.setText(chapterText);
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

}
