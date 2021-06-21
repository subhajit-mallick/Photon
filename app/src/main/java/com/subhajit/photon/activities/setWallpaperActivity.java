package com.subhajit.photon.activities;


import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhajit.photon.BuildConfig;
import com.subhajit.photon.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class setWallpaperActivity extends AppCompatActivity {

    ImageView wallView;
    private String imageURL;
    private FloatingActionButton fab_main, fab_share, fab_set_wall;
    private Float translationY = 100f;
    private boolean isOpen;
    OvershootInterpolator interpolator = new OvershootInterpolator();
    private DatabaseReference shareTxt;
    private String shareExtraText;
    Bitmap myBitmap;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wallpaper);

        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);

        fab_main = findViewById(R.id.fab_main);
        fab_share = findViewById(R.id.fab_share);
        fab_set_wall = findViewById(R.id.fab_set_wall);
        fab_share.setAlpha(0f);
        fab_set_wall.setAlpha(0f);
        fab_share.setTranslationY(translationY);
        fab_set_wall.setTranslationY(translationY);
        isOpen = false;
        wallView = (ImageView) findViewById(R.id.wallpaper);

        ImageButton imageButton = findViewById(R.id.btn_backII);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMainAct();
            }
        });
        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabShareAct();
            }
        });
        fab_set_wall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWallPopup();
            }
        });
        fab_share.setEnabled(false);
        fab_set_wall.setEnabled(false);

        getIncomingIntent();
        getShareTxt();
        loadWall(imageURL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

    }

    private void goBack() {
        Intent intentBack = new Intent(this, HomeActivity.class);
        startActivity(intentBack);
    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra("img_url")) {
            imageURL = getIntent().getStringExtra("img_url");
        }
    }
    private void getShareTxt() {
        shareTxt = FirebaseDatabase.getInstance().getReference("strData");
        shareTxt.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    shareExtraText = Objects.requireNonNull(snapshot.child("shareTxt").getValue()).toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                shareExtraText = "Shared via Photon ...";
            }
        });
    }
    private void loadWall(String imageURL) {
        ImageView image = findViewById(R.id.wallpaper);
        Glide.with(this).load(imageURL).centerCrop().listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(image);

    }
    private void fabMainAct() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        if (isOpen) {
            fab_main.animate().setInterpolator(interpolator).rotation(0f).setDuration(300).start();
            fab_share.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
            fab_set_wall.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
            fab_share.setEnabled(false);
            fab_set_wall.setEnabled(false);
            isOpen = false;
        } else {
            fab_main.animate().setInterpolator(interpolator).rotation(45f).setDuration(300).start();
            fab_share.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
            fab_set_wall.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
            fab_share.setEnabled(true);
            fab_set_wall.setEnabled(true);
            isOpen = true;
        }
    }
    private void fabShareAct() {
        Drawable drawable= wallView.getDrawable();
        Bitmap bitmap=((BitmapDrawable)drawable).getBitmap();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        try {
            File file = new File(this.getExternalCacheDir(), "photon.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(Intent.EXTRA_STREAM, photoURI);
            getShareTxt();
            intent.putExtra(Intent.EXTRA_TEXT, shareExtraText);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");
            startActivity(Intent.createChooser(intent, "share via ..."));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setWall(final int i) {
        progressBar.setVisibility(View.VISIBLE);

        class setWallTask extends AsyncTask<Void, Void, Bitmap> {

            private String url;
            Drawable drawable= wallView.getDrawable();

            private setWallTask(String url) {
                this.url = url;
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                wallView = (ImageView) findViewById(R.id.wallpaper);
                Bitmap myBitmap=((BitmapDrawable)drawable).getBitmap();
//                myBitmap = getBitmapFromView(wallView);
                return myBitmap;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
//                Toast.makeText(getApplicationContext(), "Ack-setWallStart", Toast.LENGTH_LONG).show();
                WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());

                if(i==0) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            myWallpaperManager.setBitmap(result,null,true,myWallpaperManager.FLAG_SYSTEM);//For Home screen
                            Snackbar.make(wallView,"Your Home Screen Wallpaper is set", BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                        else{
                            myWallpaperManager.setBitmap(result);
                            Toast.makeText(getApplicationContext(), "Your Wallpaper is set",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else if (i==1){
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            myWallpaperManager.setBitmap(result,null,true,myWallpaperManager.FLAG_LOCK);//For Lock screen
                            Snackbar.make(wallView,"Your Lock Screen Wallpaper is set", BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Lock screen walpaper not supported",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
                progressBar.setVisibility(View.GONE);
            }

        }

        setWallTask obj = new setWallTask(imageURL);
        obj.execute();
    }
    public void showWallPopup(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                setWallpaperActivity.this,R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_sheet,
                        (LinearLayout)findViewById(R.id.bottomSheetContainer)
                );

        bottomSheetView.findViewById(R.id.home_sheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                setWall(0);
            }
        });
        bottomSheetView.findViewById(R.id.lock_sheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                setWall(1);
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }
}




