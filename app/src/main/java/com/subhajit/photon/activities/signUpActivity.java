package com.subhajit.photon.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.subhajit.photon.R;
import com.subhajit.photon.fragments.profileFrag;

import java.util.Objects;

public class signUpActivity extends AppCompatActivity implements View.OnClickListener{

    EditText editTextEmail, editTextPassword, editTextName;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = (EditText) findViewById(R.id.log_email);
        editTextPassword = (EditText) findViewById(R.id.log_pass);
        editTextName = (EditText) findViewById(R.id.log_name);
        progressBar = (ProgressBar) findViewById(R.id.progressBarLog);

        findViewById(R.id.sign_up_btn).setOnClickListener(this);
        findViewById(R.id.txt_btn_login).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_up_btn:
                registerUser(v);
                break;

            case R.id.txt_btn_login:
                Intent intent = new Intent(this,HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
                break;
        }
    }

    private void registerUser(final View v) {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();

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

        if(editTextName.getText().toString().isEmpty()){
            editTextName.setError("Name required");
            editTextName.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            saveUserInfo();
                            Snackbar.make(v,"Sign Up successful", BaseTransientBottomBar.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            finish();
                            startActivity(intent);
                        }else{
                            if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                Snackbar.make(v,"Email already registered", BaseTransientBottomBar.LENGTH_SHORT).show();
                            }else{
                                Snackbar.make(v, Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()), BaseTransientBottomBar.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void saveUserInfo() {
        String displayName = editTextName.getText().toString();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(Uri.parse("https://firebasestorage.googleapis.com/v0/b/photon-47a0c.appspot.com/o/Default%2Fdpdef.png?alt=media&token=830c2433-77a4-4cc3-998e-16d73d735fe6"))
                    .build();
            user.updateProfile(profileUpdates);
        }
    }
}
