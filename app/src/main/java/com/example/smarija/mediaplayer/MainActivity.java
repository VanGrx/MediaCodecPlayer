package com.example.smarija.mediaplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

public class MainActivity extends FragmentActivity implements FileFragment.OnListFragmentInteractionListener, InfoFragment.OnFragmentInteractionListener,
MovieFragment.OnFragmentInteractionListener{

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
MovieFragment fragment2;
FileFragment fragment1;
String title;
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        fragment2=new MovieFragment();
        fragment1=new FileFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment2, fragment2, "someTag2");
        ft.add(R.id.fragment1, fragment1, "someTag1");
        ft.commit();

    }


    @Override
    public void sendText(String titleVideo, String path) {
    title=titleVideo;
       fragment2.updateSelectedFile(path);


    }

    @SuppressLint("ResourceType")
    private void startTransaction(String title, int videoWidth, int videoHeight, long videoDuration, String videoFormat) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        InfoFragment fr=new InfoFragment();
        ft.replace(R.id.fragment1, fr, "detailFragment");
        fr.updateTextView(title, videoWidth, videoHeight, videoDuration, videoFormat);
        ft.commit();

    }

    @Override
    public void goBack() {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        FileFragment fr=new FileFragment();
        ft.replace(R.id.fragment1, fr, "fileFragment");
        ft.commit();

    }

    @Override
    public void sendVideoInfo(int videoWidth, int videoHeight,  long videoDuration, String videoFormat) {
        startTransaction(title, videoWidth, videoHeight, videoDuration, videoFormat);
    }
}