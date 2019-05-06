package com.example.googlesignin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AfterSignin extends AppCompatActivity {
     private FirebaseAuth mAuth;
    private Button mlogoutBtn;
    private Button mprofileSetupBtn;
    private GoogleSignInClient mGoogleSignInClient;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_signin);


        mlogoutBtn = findViewById(R.id.logoutBtn);
        mprofileSetupBtn = findViewById(R.id.profileBtn);
        mAuth = FirebaseAuth.getInstance();



        //profile btn
        mprofileSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ProfileIntent = new Intent(AfterSignin.this,Profile.class);
                startActivity(ProfileIntent);
            }
        });


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //log out function
        mlogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuth.getCurrentUser()!=null){
                    signOut();

                    Intent MainActivityIntent = new Intent(AfterSignin.this,MainActivity.class);
                    startActivity(MainActivityIntent);
                    finish();
                }else{
                    Toast.makeText(AfterSignin.this, "Already logged out.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            Intent MainActivityIntent = new Intent(AfterSignin.this,MainActivity.class);
            startActivity(MainActivityIntent);
            finish();


        }





    }


    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(AfterSignin.this, "Successfully signed out.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
