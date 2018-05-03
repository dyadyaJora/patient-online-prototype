package com.example.android.patientonline.screen.devices;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;

import com.example.android.patientonline.R;

/**
 * Created by user on 26.03.2018.
 */

public class FragmentDevicesPage extends Fragment implements View.OnClickListener {

    Button addBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_devices, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        addBtn = (Button) getView().findViewById(R.id.addNewDevice);
        addBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addNewDevice:
                Intent intent = new Intent(getActivity(), ActivityFindDevicePage.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
