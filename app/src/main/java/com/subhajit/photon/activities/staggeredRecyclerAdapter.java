package com.subhajit.photon.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.subhajit.photon.R;
import com.subhajit.photon.models.row;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class staggeredRecyclerAdapter extends RecyclerView.Adapter<staggeredRecyclerAdapter.ImageViewHolder>{

    private Context mContext;
    private List<row> mdata;
    private RequestOptions option;


    public staggeredRecyclerAdapter(Context mContext, List<row> mdata) {
        this.mContext = mContext;
        this.mdata = mdata;
        //centreInside()-original
        option = new RequestOptions().centerCrop().override(300,500).placeholder(android.R.color.transparent).error(android.R.color.transparent);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_item,viewGroup,false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder imageViewHolder, final int position) {
//        imageViewHolder.imgP.setImageResource(mdata.get(position).getImg()); //for static images loaded in drawable
        Glide.with(mContext).load(mdata.get(position).getThumb_url()).thumbnail(0.8f).apply(option).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                imageViewHolder.checkBoxFav.setVisibility(View.VISIBLE);
                return false;
            }
        }).into(imageViewHolder.imgP);
        row r = mdata.get(position);

        imageViewHolder.checkBoxFav.setChecked(false);
        if(r.isFavourite){
            imageViewHolder.checkBoxFav.setChecked(true);
        }
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //delay
//                imageViewHolder.checkBoxFav.setVisibility(View.VISIBLE);
//            }
//        }, 1000);
        imageViewHolder.imgP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, setWallpaperActivity.class);
                intent.putExtra("img_url",mdata.get(position).getImage_url());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        ImageView imgP;
        public CheckBox checkBoxFav;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgP = itemView.findViewById(R.id.imgViewNew);
            checkBoxFav = itemView.findViewById(R.id.chk_fav);
            checkBoxFav.setOnCheckedChangeListener(this);
//            this.setIsRecyclable(false);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(FirebaseAuth.getInstance().getCurrentUser() == null){
                Snackbar notSignedIn = Snackbar.make(itemView,"Please Sign In ...", BaseTransientBottomBar.LENGTH_LONG)
                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
                notSignedIn.setAction("SIGN IN", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChipNavigationBar chipNavigationBar = checkBoxFav.getRootView().findViewById(R.id.bottom_nav_menu);
                        chipNavigationBar.setItemSelected(R.id.bottom_nav_profile,true);
                    }
                });
                notSignedIn.show();
                buttonView.setChecked(false);
                return;
            }
                DatabaseReference dbFavs = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites");
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(buttonView.getContext());
            int position = getAdapterPosition();
            row r = mdata.get(position);

            if(isChecked){
                dbFavs.child(r.id).setValue(r);
            }else{
                dbFavs.child(r.id).setValue(null);
            }
        }
    }
}
