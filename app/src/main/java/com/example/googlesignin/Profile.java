package com.example.googlesignin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.GetChars;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Profile extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "Name";
    private static final int REQUEST_CODE = 123;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStoragereference;

    private ImageView mUserImageView;

    private EditText mUserNameText;
    private HashMap<String,String> usermap ;
    private BroadcastReceiver mNetworkReceiver;
    private Contact contact = new Contact();
    private byte[] imageByte= new byte[1000000];
   private DatabaseHandler databaseHandler  = new DatabaseHandler(this);;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d("kaushal", "onCreate: ");





        mUserImageView = findViewById(R.id.user_image);

        mUserNameText = findViewById(R.id.text_name);
        usermap = new HashMap<>();

       //firbase refrence
        mAuth = FirebaseAuth.getInstance();
        mStoragereference = FirebaseStorage.getInstance().getReference().child("image");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


       mUserImageView.setOnClickListener(this);




        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                boolean ifdataExits =databaseHandler.CheckIsDataAlreadyInDBorNot("users","id","1");

                if (ifdataExits){
                    try
                    {    final Contact mContact = databaseHandler.getContact(1);

                        byte[] image = mContact.getImage();

                        //uploading image to firebase and getting the url back.
                        mStoragereference.putBytes(image).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()){
                                    throw task.getException();
                                }
                                return mStoragereference.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    Uri downUri = task.getResult();

                                    HashMap<String,String> usermap = new HashMap<>();
                                    usermap.put("name",mContact.getName());
                                    usermap.put("image",downUri.toString());

                                    mDatabase.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(Profile.this, "Realtime database updated", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }
                        });






                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }


            }
        };


        //broadcast reciever.
        mNetworkReceiver = broadcastReceiver;
        registerNetworkBroadcastForNougat();

    }

    @Override
    public void onClick(View v) {

     String username = mUserNameText.getText().toString().trim();

        if(username.matches("")) {


            Toast.makeText(this, "Please fill Complete form", Toast.LENGTH_SHORT).show();



                }
                else {
                Log.d(TAG, "onClick: musername:"+username);
               usermap.put("name",username);

                    //created intent for getting image
                    Intent imageIntent = new Intent();
                    imageIntent.setType("image/*");
                    imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(imageIntent, REQUEST_CODE);
                }




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== REQUEST_CODE && resultCode==RESULT_OK && data != null){

            Uri file =  data.getData();



            //Getting byte Array from image Uri.



            InputStream iStream = null;
            try {
                iStream = getContentResolver().openInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                 imageByte = getBytes(iStream);



            } catch (IOException e) {
                e.printStackTrace();
            }


            //checking if value exits or not;
            boolean ifdataExits =databaseHandler.CheckIsDataAlreadyInDBorNot("users","id","1");
            if(ifdataExits){




                databaseHandler.updateContact(usermap.get("name"),imageByte);

                Toast.makeText(this, "System Database Updated", Toast.LENGTH_SHORT).show();


            }
            else {

                String name = usermap.get("name");

                databaseHandler.addContact(new Contact(1,name,imageByte));
            }



            // converting byte array to bitmap.

            Bitmap bmp = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);

            mUserNameText.setText(contact.getName());
            mUserImageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, mUserImageView.getWidth(),
                    mUserImageView.getHeight(), false));





        }

    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    //registering the broadcast.

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }



    //unregistering the broadcast.
    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    //to check if user is online

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterNetworkChanges();
    }

    


    @Override
    protected void onStart() {
        super.onStart();


        byte[] mimageByte;

        boolean ifdataExits =databaseHandler.CheckIsDataAlreadyInDBorNot("users","id","1");
        if(ifdataExits){



            Contact mcontact = databaseHandler.getContact(1);
            mUserNameText.setText(mcontact.getName());
            mimageByte = mcontact.getImage();

            Bitmap bmp = BitmapFactory.decodeByteArray(mimageByte, 0, mimageByte.length);
            mUserImageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 250,
                   250, false));



    }
        else {

        }

        
    }




}
