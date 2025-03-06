package com.example.watertrackerandroidapp.LoginFunction;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RegisterPagerAdapter extends FragmentStateAdapter {

    public RegisterPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new PhoneLoginFragment() : new EmailLoginFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}