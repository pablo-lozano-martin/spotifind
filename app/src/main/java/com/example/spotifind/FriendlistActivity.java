package com.example.spotifind;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class FriendlistActivity extends AppCompatActivity implements FriendlistAdapter.OnItemClickListener {

    private RecyclerView friendlistRecyclerView;
    private FriendlistAdapter adapter;
    private ArrayList<LocalUser> friendlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);

        setfriendlist();
        setInterface();
    }

    private void setfriendlist(){
        //TODO set the friendlist
        this.friendlist = new ArrayList<LocalUser>();
    }

    private void setInterface(){
        //Recycler view config
        this.friendlistRecyclerView = findViewById(R.id.friendlist_recyclerview);
        this.adapter = new FriendlistAdapter(this, this.friendlist);
        this.adapter.setOnItemClickListener(this);
        this.friendlistRecyclerView.setAdapter(this.adapter);
        this.friendlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void profile(int position) {
        friendlist.get(position);//TODO .viewProfile O ir a la actividad profile con los datos de este user
        Log.d("FrienlistActivity", "Se vería el perfil de " + friendlist.get(position).getUsername());
    }
}