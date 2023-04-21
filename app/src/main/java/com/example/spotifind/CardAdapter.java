package com.example.spotifind;

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

    import com.spotify.protocol.types.Item;

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

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (customArtistsList != null) {
                CustomArtist customArtist = customArtistsList.get(position);
                holder.textView.setText(customArtist.getName());
                Glide.with(context).load(customArtist.getImageUrl()).into(holder.imageView);
                holder.itemView.setOnClickListener(v -> {
                    // implementa aquí la acción al hacer click en el item de artista
                });
            } else if (customTracksList != null) {
                CustomTrack customTrack = customTracksList.get(position);
                holder.textView.setText(customTrack.getName());
                Glide.with(context).load(customTrack.getImageUrl()).into(holder.imageView);
                holder.itemView.setOnClickListener(v -> {
                    // implementa aquí la acción al hacer click en el item de canción
                });
            }
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
    }
