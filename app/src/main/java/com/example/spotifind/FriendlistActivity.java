package com.example.spotifind;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;

public class FriendlistActivity extends AppCompatActivity implements FriendlistAdapter.OnItemClickListener {

    private RecyclerView friendlistRecyclerView;
    private BottomNavigationView navBar;
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

    private void setInterface() {
        //Recycler view config
        this.friendlistRecyclerView = findViewById(R.id.friendlist_recyclerview);
        this.adapter = new FriendlistAdapter(this, this.friendlist);
        this.adapter.setOnItemClickListener(this);
        this.friendlistRecyclerView.setAdapter(this.adapter);
        this.friendlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        navBar = findViewById(R.id.navbar);
        navBar.setSelectedItemId(R.id.friendlist);
        NavigationBarListener navigationBarListener = new NavigationBarListener(this);
        navBar.setOnNavigationItemSelectedListener(navigationBarListener);
    }

    @Override
    public void profile(int position) {
        friendlist.get(position);//TODO .viewProfile O ir a la actividad profile con los datos de este user
        Log.d("FrienlistActivity", "Se vería el perfil de " + friendlist.get(position).get_username());
    }
}