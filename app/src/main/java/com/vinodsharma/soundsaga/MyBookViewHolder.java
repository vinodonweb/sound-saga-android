package com.vinodsharma.soundsaga;

import androidx.recyclerview.widget.RecyclerView;

import com.vinodsharma.soundsaga.databinding.ActivityMyBookEntryBinding;

public class MyBookViewHolder extends RecyclerView.ViewHolder {

    ActivityMyBookEntryBinding binding;

    public MyBookViewHolder(ActivityMyBookEntryBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
