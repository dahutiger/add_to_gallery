package com.griends.myapplication;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.griends.myapplication.picturescan.ImagePopup;
import com.griends.myapplication.picturescan.core.BasePopupView;
import com.griends.myapplication.picturescan.util.PopupImageLoader;
import com.griends.myapplication.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    
    
    private List<String> urls = new ArrayList<>();
    private BasePopupView popupView;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtil.init(this);
        setContentView(R.layout.activity_main2);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);
        
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        
        urls.add("https://sf-app-1257989287.cos.ap-shanghai.myqcloud.com/pic/receipt/2020-01-13/10000066-10-42-1578898203829.jpg");
        urls.add("https://sf-app-1257989287.cos.ap-shanghai.myqcloud.com/pic/receipt/2020-01-13/10000066-10-42-1578896366146.jpg");
        urls.add("https://sf-app-1257989287.cos.ap-shanghai.myqcloud.com/pic/certification/2018-11-28/10000066-1543387137053.jpg");
        popupView = new ImagePopup.Builder(this)
                .asImageViewer(null, 0, urls, null, new PopupImageLoader())
                .isShowSaveButton(true)
                .isShowIndicator(true)
                .show();
        
        checkPermission();
    }
    
    @Override
    public void onBackPressed() {
        if (popupView.isShowing()) {
            popupView.dismiss();
            return;
        }
        super.onBackPressed();
    }
    
    private void checkPermission() {
        List<String> permissions = PermissionUtil.getPermissions();
        List<String> denied = new ArrayList<>();
        for (String permission : permissions) {
            if (PermissionUtil.isRuntime(permission) && !PermissionUtil.isGranted(permission)) {
                denied.add(permission);
            }
        }
        
        if (denied.size() > 0) {
            String[] deniedPermissions = new String[denied.size()];
            denied.toArray(deniedPermissions);
            ActivityCompat.requestPermissions(this, deniedPermissions, 1);
        }
    }
}