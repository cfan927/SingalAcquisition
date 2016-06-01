package com.example.wenjieli.singalacquisition;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Wenjie Li on 2016/5/27.
 */
public class BluetoothListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<BluetoothDevice> mDevices;
    private List<String> mCheckedList;//保存选中的bt
    private int mResource;
    private LayoutInflater mInflater;
    private Boolean[] isChecked;

    public BluetoothListAdapter(Context context, ArrayList<BluetoothDevice> devices, List<String> checkedList, int resource) {
        mContext = context;
        mDevices = devices;
        mCheckedList = checkedList;
        mResource = resource;
        mInflater = LayoutInflater.from(mContext);
        isChecked = new Boolean[mDevices.size()];
        for (int i = 0; i < mDevices.size(); i++) {
            isChecked[i] = false;
        }
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(mResource, viewGroup, false);
        }
        final TextView btName = (TextView) view.findViewById(R.id.wifi_name);
        final CheckBox btCheck = (CheckBox) view.findViewById(R.id.wifi_check);
        btName.setText(mDevices.get(i).getName());
        btCheck.setChecked(isChecked[i]);
        btCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isChecked[i] = btCheck.isChecked();
                if (btCheck.isChecked()) {
                    //wifiName.setText("OK");
                    mCheckedList.add(mDevices.get(i).getName());
                } else {
                    //wifiName.setText("NO");
                    mCheckedList.remove(mDevices.get(i).getName());
                }
                //Toast.makeText(mContext, mCheckedList.size() + "", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}
