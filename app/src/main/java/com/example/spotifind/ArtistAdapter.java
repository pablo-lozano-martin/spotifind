package com.example.spotifind;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    private List<Artist> artistList;
    private OnItemClickListener onItemClickListener;

    public ArtistAdapter(List<Artist> artistList, OnItemClickListener onItemClickListener) {
        this.artistList = artistList;
        this.onItemClickListener = onItemClickListener;
    }

    public void setArtistList(List<Artist> artistList) {
        this.artistList = artistList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Artist artist = artistList.get(position);
        holder.bind(artist);
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView artistImage;
        private TextView artistName;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            artistImage = itemView.findViewById(R.id.artistImage);
            artistName = itemView.findViewById(R.id.artistName);
            itemView.setOnClickListener(this);
        }

        public void bind(Artist artist) {
            artistName.setText(artist.getName());
            String imageUrl = artist.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(artistImage);
            } else {
                artistImage.setImageResource(R.drawable.default_image);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        }
    }

}
