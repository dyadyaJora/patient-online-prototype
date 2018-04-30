package com.example.android.patientonline.screen.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import com.example.android.patientonline.R;

/**
 * Created by user on 26.03.2018.
 */

public class FragmentHomePage extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, null);
    }
}
