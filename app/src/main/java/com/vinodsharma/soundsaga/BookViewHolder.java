package com.vinodsharma.soundsaga;

import androidx.recyclerview.widget.RecyclerView;

import com.vinodsharma.soundsaga.databinding.ActivityAudioBookEntryBinding;

public class BookViewHolder extends RecyclerView.ViewHolder {
    public ActivityAudioBookEntryBinding binding;

    public BookViewHolder(ActivityAudioBookEntryBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
