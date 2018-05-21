package com.example.android.patientonline.screen.devices;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.patientonline.R;
import com.example.android.patientonline.data.DataBaseHelper;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 26.03.2018.
 */

public class FragmentDevicesPage extends Fragment implements View.OnClickListener {

    Button addBtn;
    TextView noDev;
    ListView lv;
    ArrayList<HashMap<String, Object>> array;
    SimpleAdapter devicesAdapter;
    HashMap<String, Object> map;

    DataBaseHelper dbHelper;
    SQLiteDatabase db;

    final int REQUEST_CODE_FIND_DEVICES = 17;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_devices, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        addBtn = (Button) getView().findViewById(R.id.addNewDevice);
        addBtn.setOnClickListener(this);

        lv = (ListView) getView().findViewById(R.id.listDiveces);

        noDev = (TextView) getView().findViewById(R.id.noDevices);

        dbHelper = new DataBaseHelper(getContext());

        db = dbHelper.getWritableDatabase();
        array = new ArrayList<>();

        devicesAdapter = new SimpleAdapter(getContext(), array, android.R.layout.activity_list_item,
                new String[] { "Name", "Icon"},
                new int[] { android.R.id.text1, android.R.id.icon });
        lv.setAdapter(devicesAdapter);

        Cursor cur = db.query(DataBaseHelper.TABLE_DEVICES, null, null, null, null, null, null);
        if (cur.getCount() == 0) {
            //ContentValues cv = new ContentValues();
            noDev.setVisibility(View.VISIBLE);
        } else {
            if (cur.moveToFirst()) {
                String str;
                do {
                    str = cur.getString(cur.getColumnIndex(DataBaseHelper.COL_NAME));
                    map = new HashMap<>();
                    map.put("Name", str);
                    map.put("Icon", R.drawable.ic_menu_manage);
                    array.add(map);

                } while (cur.moveToNext());
            }

            devicesAdapter.notifyDataSetChanged();
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(intent);
                }
            });
        }
        cur.close();
        db.close();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addNewDevice:
                Intent intent = new Intent(getActivity(), ActivityFindDevicePage.class);
                startActivityForResult(intent, REQUEST_CODE_FIND_DEVICES);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_FIND_DEVICES){

            if(resultCode == Activity.RESULT_OK && data.hasExtra("name")) {
                String str = data.getStringExtra("name");
                map = new HashMap<>();
                map.put("Name", str);
                map.put("Icon", R.drawable.ic_menu_manage);
                array.add(map);
                devicesAdapter.notifyDataSetChanged();
                noDev.setVisibility(View.INVISIBLE);
            }
        }
    }
}
