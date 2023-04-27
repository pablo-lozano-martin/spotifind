package com.example.spotifind.friendlist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.example.spotifind.LocalUser;
import com.example.spotifind.MainActivity;
import com.example.spotifind.NavigationBarListener;
import com.example.spotifind.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class FriendlistActivity extends AppCompatActivity implements FriendlistAdapter.OnItemClickListener {

    private RecyclerView friendlistRecyclerView;
    private BottomNavigationView navBar;
    private FriendlistAdapter adapter;
    private ArrayList<LocalUser> friendlist;

    private String userId;

    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);
        token = getTokenFromCache(getApplicationContext());
        userId = getIntent().getStringExtra("user_id");
        setfriendlist();
        setInterface();
    }

    private void setfriendlist(){
        //TODO set the friendlist
        LocalUser user = new LocalUser(this,token);
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
        NavigationBarListener navigationBarListener = new NavigationBarListener(this,userId);
        navBar.setOnNavigationItemSelectedListener(navigationBarListener);
    }

    @Override
    public void profile(int position) {
        friendlist.get(position);//TODO .viewProfile O ir a la actividad profile con los datos de este user
        Log.d("FrienlistActivity", "Se vería el perfil de " + friendlist.get(position).getUsername());
    }

    private String getTokenFromCache(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }

}