package com.example.smarija.mediaplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

public class MainActivity extends FragmentActivity implements FileFragment.OnListFragmentInteractionListener, BlankFragment.OnFragmentInteractionListener {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
Fragment2 fragment2;
FileFragment fragment1;
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
        fragment2=new Fragment2();
        fragment1=new FileFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment2, fragment2, "someTag2");
        ft.add(R.id.fragment1, fragment1, "someTag1");
        ft.commit();

    }


    @Override
    public void sendText(String s, String path) {

       fragment2.updateText(s, path);
        startTransaction(s);

    }

    @SuppressLint("ResourceType")
    private void startTransaction(String text) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        BlankFragment fr=new BlankFragment();
        ft.replace(R.id.fragment1, fr, "detailFragment");
        fr.updateTextView(text);
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
}