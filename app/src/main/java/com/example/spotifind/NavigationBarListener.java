package com.example.spotifind;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.spotifind.friendlist.FriendlistActivity;
import com.example.spotifind.radar.RadarActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationBarListener implements BottomNavigationView.OnNavigationItemSelectedListener {
    private String uid;
    private Context mContext;

    public NavigationBarListener(Context context,String uid) {
        mContext = context;
        this.uid= uid;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.friendlist:
                Intent friendlist = new Intent(mContext, FriendlistActivity.class);
                friendlist.putExtra("user_id",uid);
                mContext.startActivity(friendlist);
                break;
            case R.id.radar:
                Intent radar = new Intent(mContext, RadarActivity.class);
                radar.putExtra("user_id",uid);
                mContext.startActivity(radar);
                break;
            case R.id.profile:
                // Navegar a la actividad de perfil
                /*Intent profile = new Intent(mContext, ProfileActivity.class);
                mContext.startActivity(profile);*/
                Log.d("NavigationBarListener", "Se ha pulsado perfil");
                break;
        }

        // Cerrar el menú de navegación después de seleccionar un elemento
        //((Activity) mContext).findViewById(R.id.drawer_layout).closeDrawer(GravityCompat.START);

        return true;
    }
}
