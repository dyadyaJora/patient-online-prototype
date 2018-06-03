package com.example.android.patientonline.screen.mydata;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.patientonline.R;
import com.example.android.patientonline.service.BtDataRunPulseService;
import com.example.android.patientonline.service.BtDataRunTempService;
import com.example.android.patientonline.service.BtDataService;

import org.w3c.dom.Text;

import java.util.HashMap;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.BIND_AUTO_CREATE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentMyData.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentMyData#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMyData extends Fragment implements  BtDataService.Callback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    RelativeLayout pulse, temperature;
    TextView tvPulse, tvTemp;

    BtDataRunTempService serviceTemp;
    BtDataRunPulseService servicePulse;

    private ServiceConnection cnctTemp = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(getActivity(), "onServiceConnected called", Toast.LENGTH_SHORT).show();

            BtDataRunTempService.LocalBinder binder = (BtDataRunTempService.LocalBinder) iBinder;
            serviceTemp = (BtDataRunTempService) binder.getServiceInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(getActivity(), "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
            BtDataRunTempService.unregisterActivity(FragmentMyData.this);
        }
    };

    private ServiceConnection cnctPulse = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(getActivity(), "onServiceConnected called", Toast.LENGTH_SHORT).show();

            BtDataRunPulseService.LocalBinder binder = (BtDataRunPulseService.LocalBinder) iBinder;
            servicePulse = (BtDataRunPulseService) binder.getServiceInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(getActivity(), "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
            BtDataRunPulseService.unregisterActivity(FragmentMyData.this);
        }
    };

    public FragmentMyData() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentMyData.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentMyData newInstance(String param1, String param2) {
        FragmentMyData fragment = new FragmentMyData();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_data, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Мои данные");
        pulse = (RelativeLayout) getView().findViewById(R.id.layout_pulse);
        pulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), PulseActivity.class);
                startActivity(i);
            }
        });

        temperature = (RelativeLayout) getView().findViewById(R.id.layout_temperature);
        temperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), TemperatureActivity.class);
                startActivity(i);
            }
        });

        tvPulse = (TextView) getView().findViewById(R.id.text_value_8);
        tvTemp = (TextView) getView().findViewById(R.id.text_value_10);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isMyServiceRunning(BtDataRunPulseService.class)) {
            BtDataRunPulseService.registerActivity(FragmentMyData.this);
            getActivity().bindService(new Intent(getActivity(), BtDataRunPulseService.class), cnctPulse, BIND_AUTO_CREATE);
        }

        if (isMyServiceRunning(BtDataRunTempService.class)) {
            BtDataRunTempService.registerActivity(FragmentMyData.this);
            getActivity().bindService(new Intent(getActivity(), BtDataRunTempService.class), cnctTemp, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            getActivity().unbindService(cnctPulse);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            getActivity().unbindService(cnctTemp);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        BtDataRunPulseService.unregisterActivity(FragmentMyData.this);
        BtDataRunTempService.unregisterActivity(FragmentMyData.this);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            // throw new RuntimeException(context.toString()
            //         + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        Activity x = FragmentMyData.this.getActivity();
    }

    @Override
    public void onStartCallback() {
        // pass
    }

    @Override
    public void onTickCallback(HashMap data) {
        final String value = data.get("main_val").toString();
        int type = (int) data.get("type");

        if (type == 1) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvPulse.setText(value + " уд/мин");
                }
            });
        }

        if (type == 2) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTemp.setText(value + " °C");
                }
            });
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
