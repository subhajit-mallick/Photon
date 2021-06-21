package com.subhajit.photon.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.subhajit.photon.R;
import com.subhajit.photon.fragments.favFrag;
import com.subhajit.photon.fragments.homeFrag;
import com.subhajit.photon.fragments.profileFrag;

import java.util.Objects;

import static java.security.AccessController.getContext;

public class HomeActivity extends AppCompatActivity{

    public static final String CHANNEL_ID = "PHOTON_UPDATES";
    private static final String CHANNEL_NAME = "PHOTON";
    private static final String CHANNEL_DESC = "PHOTON NOTIFICATION";

    public ChipNavigationBar chipNavigationBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID
                    , CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager notificationManager = Objects.requireNonNull(this)
                    .getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        chipNavigationBar = findViewById(R.id.bottom_nav_menu);
        chipNavigationBar.setItemSelected(R.id.bottom_nav_home,true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new homeFrag()).commit();
        bottomMenu();
    }
    private void bottomMenu() {

        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment= null;
                switch(i){
                    case R.id.bottom_nav_home:
                    fragment= new homeFrag();
                    break;

                    case R.id.bottom_nav_fav:
                    fragment= new favFrag();
                    break;

                    case R.id.bottom_nav_profile:
                    fragment= new profileFrag();
                    break;

                    default:
                    fragment= new homeFrag();
                    break;

                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment).commit();
            }
        });
    }
}
