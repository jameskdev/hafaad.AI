package com.xm.aeclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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
        mViewPager.setUserInputEnabled(false);
    }



}