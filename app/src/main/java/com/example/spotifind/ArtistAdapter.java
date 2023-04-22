package com.example.spotifind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistViewHolder> {
    private List<Artist> artistList;

    public ArtistAdapter(List<Artist> artistList) {
        this.artistList = artistList;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_card, parent, false);
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

    public void setArtistList(List<Artist> artistList) {
    }
}

public class ArtistViewHolder extends RecyclerView.ViewHolder {
    private ImageView artistImage;
    private TextView artistName;

    public ArtistViewHolder(View itemView) {
        super(itemView);
        artistImage = itemView.findViewById(R.id.artistImage);
        artistName = itemView.findViewById(R.id.artistName);
    }

    public void bind(Artist artist) {
        artistImage.setImageResource(artist.getImageResId());
        artistName.setText(artist.getName());
    }
}
