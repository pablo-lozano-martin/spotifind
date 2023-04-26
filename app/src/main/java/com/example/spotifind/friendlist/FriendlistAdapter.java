package com.example.spotifind.friendlist;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifind.LocalUser;
import com.example.spotifind.R;
import com.example.spotifind.profile.ProfileActivity;

import java.util.ArrayList;

public class FriendlistAdapter extends RecyclerView.Adapter<FriendlistAdapter.FriendlistViewHolder> {

    public class FriendlistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FriendlistAdapter adapter;
        private TextView name;
        private Button profileButton;
        private Context context;


        public FriendlistViewHolder(View itemView, FriendlistAdapter adapter, Context context) {
            super(itemView);
            this.name = itemView.findViewById(R.id.friendlist_name);
            this.adapter = adapter;
            this.profileButton = itemView.findViewById(R.id.friendlist_profile);
            itemView.setOnClickListener(this);
            this.context = context;
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


    private ArrayList<LocalUser> friendlist;
    private LayoutInflater inflater;

    public FriendlistAdapter(Context context, ArrayList<LocalUser> friendlist) {
        this.inflater = LayoutInflater.from(context);
        this.friendlist = friendlist;
    }
    @Override
    public FriendlistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_item, parent, false);
        return new FriendlistViewHolder(itemView, this, parent.getContext());
    }

    @Override
    public void onBindViewHolder(FriendlistViewHolder holder, int position) {
        holder.name.setText(this.friendlist.get(position).getUsername());
        holder.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (listener != null) {
                    listener.profile(holder.getAdapterPosition());
                }*/
                String uid = friendlist.get(holder.getAdapterPosition()).getUid();
                Intent profileIntent = new Intent(holder.context, ProfileActivity.class);
                profileIntent.putExtra("user_id", uid);
                holder.context.startActivity(profileIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendlist.size();
    }
}
