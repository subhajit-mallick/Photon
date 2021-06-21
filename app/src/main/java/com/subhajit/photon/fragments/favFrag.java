package com.subhajit.photon.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
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
import com.subhajit.photon.activities.staggeredRecyclerAdapter;
import com.subhajit.photon.models.row;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class favFrag extends Fragment {

    private List<row> favList;
    private staggeredRecyclerAdapter adapter;
    private DatabaseReference databaseReferenceFav;
    private ChipNavigationBar chipNavigationBar;
    private LottieAnimationView lottieProgressBar;
    private TextView textViewFav;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();
        return inflater.inflate(R.layout.fragment_fav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView staggeredRv = view.findViewById(R.id.staggered_rv_fav);
//        staggeredRv.setHasFixedSize(true);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredRv.setLayoutManager(manager);
        favList = new ArrayList<>();
        adapter = new staggeredRecyclerAdapter(getContext(),favList);
        staggeredRv.setAdapter(adapter);
        lottieProgressBar = view.findViewById(R.id.progress_bar);
        textViewFav = view.findViewById(R.id.txt_no_fav);

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            databaseReferenceFav = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites");
            fetchFav();
        }else{
            Snackbar snackbar = Snackbar.make(getView(),"Please Sign In ...", BaseTransientBottomBar.LENGTH_LONG);
            snackbar.setAction("SIGN IN", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chipNavigationBar = Objects.requireNonNull(getActivity()).findViewById(R.id.bottom_nav_menu);
                    chipNavigationBar.setItemSelected(R.id.bottom_nav_profile,true);
                }
            });
            snackbar.show();
        }

    }

    public void fetchFav(){
        lottieProgressBar.setVisibility(View.VISIBLE);
        textViewFav.setVisibility(View.VISIBLE);
        databaseReferenceFav.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    lottieProgressBar.setVisibility(View.GONE);
                    textViewFav.setVisibility(View.GONE);
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String fireURL = ds.child("image_url").getValue(String.class);
                        String thumbURL = ds.child("thumb_url").getValue(String.class);
                        String id = ds.getKey();

                        row r = new row(fireURL,thumbURL, id);
                        r.isFavourite = true;
                        favList.add(0,r);
                    }

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

