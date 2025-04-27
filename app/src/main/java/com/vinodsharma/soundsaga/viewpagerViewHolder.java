package com.vinodsharma.soundsaga;

import androidx.recyclerview.widget.RecyclerView;

import com.vinodsharma.soundsaga.databinding.ViewpagerEntryBinding;

public class viewpagerViewHolder extends RecyclerView.ViewHolder {

    ViewpagerEntryBinding binding;

    public viewpagerViewHolder(ViewpagerEntryBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
