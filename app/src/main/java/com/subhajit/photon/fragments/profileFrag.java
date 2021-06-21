package com.subhajit.photon.fragments;

import android.animation.Animator;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.internal.SignInButtonImpl;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.subhajit.photon.R;
import com.subhajit.photon.activities.signUpActivity;

import java.io.IOException;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;


public class profileFrag extends Fragment implements View.OnClickListener {

    private static final int GOOGLE_SIGN_IN_CODE = 715;
    private static final int CHOOSE_IMAGE = 855;
    private GoogleSignInClient googleSignInClient;
    private ImageView profileView;
    private SignInButton signInButton;
    FirebaseAuth mAuth;
    EditText editTextEmail, editTextPassword;
    ProgressBar progressBar;
    ProgressBar progressBarDp;
    Uri uriProfileDp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).hide();
            return inflater.inflate(R.layout.fragment_profile_default, container, false);
        }
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();
        return inflater.inflate(R.layout.fragment_profile_logged_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = (EditText) getActivity().findViewById(R.id.log_email);
        editTextPassword = (EditText) getActivity().findViewById(R.id.log_pass);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBarLog);
        progressBarDp = (ProgressBar) getActivity().findViewById(R.id.progressBarDp);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(getActivity(),signInOptions);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            //Logged In UI (profile)
            TextView txtName = view.findViewById(R.id.txt_name);
            TextView txtEmail = view.findViewById(R.id.txt_email);
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            //Load user info for all provider
            if (user != null) {
                String name = null, email = null;
                Uri photoUrl = null;
                for (UserInfo profile : user.getProviderData()) {
                    // Id of the provider (ex: google.com)
                    String providerId = profile.getProviderId();
                    // UID specific to the provider
                    String uid = profile.getUid();
                    // Name, email address, and profile photo Url
                    name = profile.getDisplayName();
                    email = profile.getEmail();
                    photoUrl = user.getPhotoUrl();
                }
                assert name != null;
                txtName.setText(name.toLowerCase());
                txtEmail.setText(email);
                profileView = view.findViewById(R.id.profile_view);
                assert photoUrl != null;
                Glide.with(getActivity())
                        .load(photoUrl.toString())
                        .circleCrop().into(profileView);

            }

            //change Dp
            ImageView changeDp = view.findViewById(R.id.btnChangeDp);
            changeDp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showImageChooser();
                }
            });

            //Logout Button
            view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container,new profileFrag()).commit();
                        }
                    });
                }
            });

        }else {
            //Log In UI (default)
            view.findViewById(R.id.txt_btn_signup).setOnClickListener(this);
            view.findViewById(R.id.login_btn).setOnClickListener(this);

            signInButton = view.findViewById(R.id.btn_google_sign_in);
            signInButton.setSize(SignInButton.SIZE_WIDE);
            signInButton.setColorScheme(SignInButton.COLOR_LIGHT);
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = googleSignInClient.getSignInIntent();
                    startActivityForResult(intent,GOOGLE_SIGN_IN_CODE);
                }
            });
        }
    }

    private void updateUserInfo(String pURL) {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(pURL))
                    .build();
            user.updateProfile(profileUpdates);
        }
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image")
        ,CHOOSE_IMAGE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_SIGN_IN_CODE){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
                Snackbar.make(getView(),e.getMessage(), BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        }

        if(requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            uriProfileDp = data.getData();
            Glide.with(this).load(uriProfileDp).circleCrop().into(profileView);
            uploadImage();
        }
    }

    private void uploadImage() {
        final StorageReference dpRef = FirebaseStorage.getInstance().getReference(
              "profilePics/"+System.currentTimeMillis()+".jpg"
        );
        if(uriProfileDp!=null){
            progressBarDp.setVisibility(View.VISIBLE);
            dpRef.putFile(uriProfileDp)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBarDp.setVisibility(View.GONE);
                            dpRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    updateUserInfo(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBarDp.setVisibility(View.GONE);
                            Snackbar.make(getView(),e.getMessage(),BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    });
        }

    }
    private void AuthGoogle(GoogleSignInAccount account) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(getActivity(),
        new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new profileFrag()).commit();
                            Snackbar.make(getView(), "Login Successful ...", BaseTransientBottomBar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(getView(), "Authentication Failed ...", BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txt_btn_signup:
                startActivity(new Intent(getActivity(), signUpActivity.class));
                break;

            case R.id.login_btn:
                userLogin();
                break;
        }
    }
    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString();

        if(email.isEmpty()){
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please enter a valid Email");
            editTextEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if(password.length()<6){
            editTextPassword.setError("Minimum length of password should be six");
            editTextPassword.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new profileFrag()).commit();
                            Snackbar.make(getView(), "Login Successful ...", BaseTransientBottomBar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(getView(), task.getException().getMessage(), BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
