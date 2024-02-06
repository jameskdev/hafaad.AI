package com.xm.aeclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

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
import com.google.android.material.tabs.TabLayoutMediator;

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
    ViewPager2 mViewPager;
    TabLayout mTl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        avm = new ViewModelProvider(this).get(AEViewModel.class);
        mViewPager = (ViewPager2) findViewById(R.id.frag_pager);
        mTl = findViewById(R.id.top_tabs);
        mViewPager.setAdapter(new FragmentStateAdapter(this) {

            @Override
            public int getItemCount() {
                return 2;
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return new ImageGeneratorFragment();
                } else if (position == 1) {
                    return new AppSettings();
                } else {
                    Log.wtf("MainActivity", "Unknown fragment position received!");
                    throw new RuntimeException("Unknown Fragment ID");
                }
            }
        });
        new TabLayoutMediator(mTl, mViewPager,
                (tab, position) -> {if (position == 0) {tab.setText(R.string.main_btn);}
                else {tab.setText(R.string.open_settings_btn);}}
        ).attach();
    }



}