package com.example.watertrackerandroidapp.LoginFunction;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class LoginPagerAdapter extends FragmentStateAdapter {
    public LoginPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PhoneLoginFragment();
        } else {
            return new EmailLoginFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Tổng số tab
    }
}