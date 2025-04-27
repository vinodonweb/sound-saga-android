package com.vinodsharma.soundsaga;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vinodsharma.soundsaga.databinding.ActivityAudioBookEntryBinding;

import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookViewHolder> {

    private final ArrayList<Book> audiobooks;
    private MainActivity mainActivity;
    private static final String TAG = "BookAdapter";

   public BookAdapter(ArrayList<Book> audiobooks, MainActivity mainActivity) {
        this.audiobooks = audiobooks;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ActivityAudioBookEntryBinding binding = ActivityAudioBookEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        // Set the click listener for the book
        binding.getRoot().setOnClickListener(mainActivity);
        binding.getRoot().setOnLongClickListener(mainActivity);

        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position){
        Book audio = audiobooks.get(position);

        holder.binding.bookTitle.setText(audio.getTitle());
        holder.binding.bookAuthor.setText(audio.getAuthor());

        String url = audio.getImage();

      Glide
              .with(mainActivity)
              .load(url)
              .into(holder.binding.bookImg);

      //    //for marquee
        holder.binding.bookTitle.setSelected(true);
        holder.binding.bookAuthor.setSelected(true);


    }

    @Override
    public int getItemCount() {
        return audiobooks.size();
    }


}
