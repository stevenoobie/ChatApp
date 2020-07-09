package com.example.testo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PageAdapter extends FragmentPagerAdapter {
    int itemsNumber;
    PageAdapter(FragmentManager fm,int itemsNumber){
        super(fm);
        this.itemsNumber=itemsNumber;

    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ChatFragment();
            case 1:
                return new StatusFragment();
            case 2:
                return new CallsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return itemsNumber;
    }
}
