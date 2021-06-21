package com.subhajit.photon.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.subhajit.photon.R;
import com.subhajit.photon.activities.About;
import com.subhajit.photon.activities.staggeredRecyclerAdapter;
import com.subhajit.photon.models.row;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//HomeFrag is for the actual Home with wallpapers shown as default
public class homeFrag extends Fragment {

    private staggeredRecyclerAdapter adapter;
    private DatabaseReference databaseReferenceAll;
    private DatabaseReference databaseReferenceFav;
    private List<row> allList;
    private List<row> favList;
    private ProgressBar progressBar;
    private ChipGroup chipGroup;
    private String shareLink;
    //For SharedPreference:
    private static final String SHARED_PREFS = "SharedPrefs";
    private static final String NOTIFY_CHECKED_STATE = "Notify_Checked_State";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        RecyclerView staggeredRv = view.findViewById(R.id.staggered_rv);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredRv.setLayoutManager(manager);
        //staggeredRv.setLayoutManager(new GridLayoutManager(getContext(),2));

        allList = new ArrayList<>();
        favList = new ArrayList<>();
        adapter = new staggeredRecyclerAdapter(getContext(), allList);
        staggeredRv.setAdapter(adapter);
        databaseReferenceAll = FirebaseDatabase.getInstance().getReference("imgData");

        preFetchOperation("");

        firebaseNotify();

        chipGroup = Objects.requireNonNull(getView()).findViewById(R.id.tag_group);
        chipGroup.setVisibility(View.GONE);

        DatabaseReference dbTagTxt = FirebaseDatabase.getInstance().getReference().child("strData");
        dbTagTxt.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String s = Objects.requireNonNull(snapshot.child("Tag").getValue()).toString();
                toLoadChips(s);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void firebaseNotify() {
        SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        boolean isChecked = sharedPreferences.getBoolean(NOTIFY_CHECKED_STATE, true);
        if(isChecked){
            FirebaseMessaging.getInstance().subscribeToTopic("updates");
        }else{
            FirebaseMessaging.getInstance().unsubscribeFromTopic("updates");
        }
    }
    private void toLoadChips(String s){
        String[] genres = s.split(" ");
        for (String genre : genres) {
            final Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item,null,false);
            chip.setText(genre);
            chipGroup.addView(chip);

            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String searchTerm;
                    if(chip.isChecked()) {
                        chipGroup.clearCheck();
                        chip.setChecked(true);
                        searchTerm = chip.getText().toString().toLowerCase();
                        closeKeyboard();

                        preFetchOperation(searchTerm);
                        Snackbar.make(Objects.requireNonNull(getView()), searchTerm, BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    private void closeKeyboard() {
        View view = Objects.requireNonNull(getActivity()).getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputManager != null;
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    private void preFetchOperation(String searchTxt) {
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            databaseReferenceFav = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites");
            fetchFavourites(searchTxt);
        }else{
            fetchWallpapers(searchTxt);
        }
    }
    private void fetchFavourites(final String data){
        favList.clear();
        progressBar.setVisibility(View.VISIBLE);
        databaseReferenceFav.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String fireURL = ds.child("url").getValue(String.class);
                        String thumbURL = ds.child("thumb").getValue(String.class);
                        String id = ds.getKey();
                        row r = new row(fireURL, thumbURL, id);
                        favList.add(0,r);
                    }
                }
                fetchWallpapers(data);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {

            }
        });
    }
    private void fetchWallpapers(final String data){
        allList.clear();
        progressBar.setVisibility(View.VISIBLE);
        databaseReferenceAll.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String fireURL = ds.child("url").getValue(String.class);
                        String thumbURL = ds.child("thumb").getValue(String.class);
                        String tag = ds.child("tag").getValue(String.class);
                        String id = ds.getKey();
                        assert tag != null;
                        if (tag.contains(data.toLowerCase())) {
                            row r = new row(fireURL,thumbURL, id);
                            if (isFavourite(r)) {
                                r.isFavourite = true;
                            }
                            allList.add(0,r);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {

            }
        });
    }
    private boolean isFavourite(row r){
        for(row f :favList){
            if(f.id.equals(r.id)){
                return true;
            }
        }
        return false;
    }
    private void getAppLink(final int f) {
        DatabaseReference shareAppRef = FirebaseDatabase.getInstance().getReference("strData");
        shareAppRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    shareLink = (Objects.requireNonNull(snapshot.child("shareLink").getValue())).toString();
                    switch (f){
                        case 0:
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(shareLink));
                            startActivity(browserIntent);
                            break;

                        case 1:
                            final Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, "Try this amazing wallpaper app: \n[photon]:\n"+shareLink);
                            intent.setType("text/plain");
                            startActivity(Intent.createChooser(intent, null));
                            break;

                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                shareLink = "Link Error";
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.three_dot_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();

        SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        boolean isChecked = sharedPreferences.getBoolean(NOTIFY_CHECKED_STATE, true);
        menu.findItem(R.id.notification_3dot).setChecked(isChecked);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                preFetchOperation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                chipGroup.setVisibility(View.VISIBLE);
                return true; // KEEP IT TO TRUE OR IT DOESN'T OPEN !!
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                preFetchOperation("");
                chipGroup.setVisibility(View.GONE);
                return true; // OR FALSE IF DIDN'T WANT IT TO CLOSE!
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.notification_3dot:
                item.setChecked(!item.isChecked());
                SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(NOTIFY_CHECKED_STATE, item.isChecked());
                editor.apply();
                firebaseNotify();
                return true;
            case R.id.sendFeedback_3dot:
                Intent Email = new Intent(Intent.ACTION_SEND);
                Email.setType("text/email");
                Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "subhajit98mallick@gmail.com" });
                Email.putExtra(Intent.EXTRA_SUBJECT, "FEEDBACK: [PHOTON]");
                Email.putExtra(Intent.EXTRA_TEXT, "Write your feedback below:" + "\n\n");
                startActivity(Intent.createChooser(Email, "Send Feedback:"));
                return true;

            case R.id.reportBug_3dot:
                Intent Report = new Intent(Intent.ACTION_SEND);
                Report.setType("text/email");
                Report.putExtra(Intent.EXTRA_EMAIL, new String[] { "subhajit98mallick@gmail.com" });
                Report.putExtra(Intent.EXTRA_SUBJECT, "REPORT-BUGS: [PHOTON]");
                Report.putExtra(Intent.EXTRA_TEXT, "Describe the issue here:"+"\n(you can also attach screen shots)" + "\n\n");
                startActivity(Intent.createChooser(Report, "Send Bug Report:"));
                return true;

            case R.id.rate_3dot:
                getAppLink(0);
                return true;

            case R.id.share_3dot:
                getAppLink(1);
                return true;

            case R.id.About_3dot:
                Intent about = new Intent(getActivity(), About.class);
                startActivity(about);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}