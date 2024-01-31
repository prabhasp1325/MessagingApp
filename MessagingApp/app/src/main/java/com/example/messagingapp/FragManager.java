package com.example.messagingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FragManager extends FragmentPagerAdapter {

    int tabCount;

    public FragManager(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        tabCount = behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0: return new ChatFrag();
            case 1: return new CallFrag();
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}



