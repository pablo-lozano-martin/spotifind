package com.example.spotifind;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class FriendlistAdapter extends RecyclerView.Adapter<FriendlistAdapter.FriendlistViewHolder> {

    public class FriendlistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FriendlistAdapter adapter;
        private TextView name;
        private Button profileButton;


        public FriendlistViewHolder(View itemView, FriendlistAdapter adapter) {
            super(itemView);
            this.name = itemView.findViewById(R.id.friendlist_name);
            this.adapter = adapter;
            this.profileButton = itemView.findViewById(R.id.friendlist_profile);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }

    public interface OnItemClickListener {
        void profile(int position);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    private ArrayList<User> friendlist;
    private LayoutInflater inflater;

    public FriendlistAdapter(Context context, ArrayList<User> friendlist) {
        this.inflater = LayoutInflater.from(context);
        this.friendlist = friendlist;
    }
    @Override
    public FriendlistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_item, parent, false);
        return new FriendlistViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(FriendlistViewHolder holder, int position) {
        holder.name.setText(this.friendlist.get(position).getUsername());
        holder.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.profile(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendlist.size();
    }
}
