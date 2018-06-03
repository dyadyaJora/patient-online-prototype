package com.example.android.patientonline.screen.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;

import com.example.android.patientonline.R;
import com.example.android.patientonline.screen.login.LoginActivity;
import com.example.android.patientonline.screen.mydata.FragmentMyData;

/**
 * Created by user on 26.03.2018.
 */

public class FragmentHomePage extends Fragment implements View.OnClickListener {

    Button loginBtn, myDataBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        loginBtn = (Button) getView().findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(this);

        myDataBtn = (Button) getView().findViewById(R.id.mydataBtn);
        myDataBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginBtn:
                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivity(i);
                break;
            case R.id.mydataBtn:
                Fragment fg = new FragmentMyData();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.screen_area, fg);
                fragmentTransaction.commit();

                NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.nav_mydata);
                break;
        }
    }
}
