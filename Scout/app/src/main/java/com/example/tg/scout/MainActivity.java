package com.example.tg.scout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.example.tg.scout.Fragment.InfoFragment;
import com.example.tg.scout.Fragment.MapFragment;
import com.example.tg.scout.Fragment.WebFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mBv;
    private NoScrollViewPager mVp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        }





    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        mBv = findViewById(R.id.navigation);
        mVp = findViewById(R.id.vp);

        mVp.setScroll(false);

        mBv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.navigation_dashboard:
                        mVp.setCurrentItem(1);
                        return true;

                    case R.id.navigation_home:
                        mVp.setCurrentItem(0);
                        return true;

                    case R.id.navigation_notifications:
                        mVp.setCurrentItem(2);
                        return true;
                }
                return false;
            }
        });
        //mBv.setSelectedItemId();

        //数据填充
        setupViewPager(mVp);
        /*
        mVp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {

                return true;
            }
        });
        */


    }


    private void setupViewPager(ViewPager viewPager) {
        BottomAdapter adapter = new BottomAdapter(getSupportFragmentManager());
        adapter.addFragment(new InfoFragment());
        adapter.addFragment(new MapFragment());
        adapter.addFragment(new WebFragment());
        viewPager.setAdapter(adapter);
    }

}
