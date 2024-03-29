package com.weteam.wechat.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.weteam.wechat.database.DataLocalManager;
import com.weteam.wechat.utils.SettingLanguage;

public class AppCompat extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initializing DataLocalManager
        DataLocalManager.init(this);

        //Set theme
        if (DataLocalManager.getTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //Set language
        SettingLanguage.getInstance().changeLanguage(this, DataLocalManager.getLanguage());
    }
}
