package com.weteam.wechat.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.weteam.wechat.fragments.ChatFragment;
import com.weteam.wechat.fragments.PeopleFragment;
import com.weteam.wechat.fragments.SettingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                return new ChatFragment();
            }
            case 1: {
                return new PeopleFragment();
            }
            case 2: {
                return new SettingFragment();
            }
            default: {
                return new ChatFragment();
            }
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
