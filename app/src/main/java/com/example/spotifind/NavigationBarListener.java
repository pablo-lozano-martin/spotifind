package com.example.spotifind;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class NavigationBarListener implements BottomNavigationView.OnNavigationItemSelectedListener {
    private Context mContext;

    public NavigationBarListener(Context context) {
        mContext = context;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.friendlist:
                Intent friendlist = new Intent(mContext, FriendlistActivity.class);
                mContext.startActivity(friendlist);
                break;
            case R.id.radar:
                Intent radar = new Intent(mContext, RadarActivity.class);
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
