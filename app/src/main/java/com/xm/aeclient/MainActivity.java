package com.xm.aeclient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.os.Bundle;
import android.os.Environment;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;

public class MainActivity extends AppCompatActivity {
    AEViewModel avm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TabLayout) findViewById(R.id.top_tabs)).addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e("AEClient", "Settings clicked");
                if (tab.getPosition() == 1) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.pref_container, new AppSettings())
                            .setReorderingAllowed(true)
                            .commit();
                } else {
                    Log.e("AEClient", "onClickMain");
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.pref_container, new ImageGeneratorFragment())
                            .setReorderingAllowed(true)
                            .commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        avm = new ViewModelProvider(this).get(AEViewModel.class);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.pref_container, new ImageGeneratorFragment())
                    .setReorderingAllowed(true)
                    .commit();
        }
    }



}