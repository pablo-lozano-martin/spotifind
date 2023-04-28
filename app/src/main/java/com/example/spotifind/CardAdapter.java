package com.example.spotifind;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Context;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.spotifind.Spotify.CustomArtist;
    import com.example.spotifind.Spotify.CustomTrack;
    import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import android.content.ActivityNotFoundException;
    import android.content.Intent;
    import android.net.Uri;
    import android.view.View;
    import android.widget.Toast;


    import java.util.List;

    public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

        private List<CustomArtist> customArtistsList;
        private List<CustomTrack> customTracksList;
        private Context context;

        public CardAdapter(List<CustomArtist> customArtistsList, List<CustomTrack> customTracksList, Context context) {
            this.customArtistsList = customArtistsList;
            this.customTracksList = customTracksList;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
            return new ViewHolder(view);
        }

        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            String spotifyUri = "";
            String imageUrl = "";

            if (customArtistsList != null) {
                CustomArtist customArtist = customArtistsList.get(position);
                holder.textView.setText(customArtist.getName());
                spotifyUri = customArtist.getUri();
                imageUrl = customArtist.getImageUrl();
            } else if (customTracksList != null) {
                CustomTrack customTrack = customTracksList.get(position);
                holder.textView.setText(customTrack.getName());
                spotifyUri = customTrack.getUri();
                imageUrl = customTrack.getImageUrl();
            }

            if (imageUrl!=null) {
                Picasso.get().load(imageUrl).into(holder.imageView);
            }

            String finalSpotifyUri = spotifyUri;
            holder.itemView.setOnClickListener(v -> {
                if (!finalSpotifyUri.isEmpty()) {
                    openSpotifySong(finalSpotifyUri);
                }
            });
        }




        @Override
        public int getItemCount() {
            if (customArtistsList != null) {
                return customArtistsList.size();
            } else if (customTracksList != null) {
                return customTracksList.size();
            } else {
                return 0;
            }
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                textView = itemView.findViewById(R.id.textView);
            }
        }

        private void openSpotifySong(String song) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(song));
            intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + context.getPackageName()));
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "Spotify no está instalado en este dispositivo.", Toast.LENGTH_SHORT).show();
            }
        }
    }

