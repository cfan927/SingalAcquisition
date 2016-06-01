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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wenjie Li on 2016/5/27.
 */
public class WifiListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<ScanResult> mList;
    private List<String> mCheckedList;//保存选中的wifi
    private int mResource;
    private LayoutInflater mInflater;
    private Boolean[] isChecked;

    public WifiListAdapter(Context context, ArrayList<ScanResult> list, List<String> checkedList, int resource) {
        mContext = context;
        mList = list;
        mCheckedList = checkedList;
        mResource = resource;
        mInflater = LayoutInflater.from(mContext);
        isChecked = new Boolean[mList.size()];
        for (int i = 0; i < mList.size(); i++) {
            isChecked[i] = false;
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
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
        final TextView wifiName = (TextView) view.findViewById(R.id.wifi_name);
        final CheckBox wifiCheck = (CheckBox) view.findViewById(R.id.wifi_check);
        wifiName.setText(mList.get(i).SSID);
        wifiCheck.setChecked(isChecked[i]);
        wifiCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isChecked[i] = wifiCheck.isChecked();
                if (wifiCheck.isChecked()) {
                    //wifiName.setText("OK");
                    mCheckedList.add(mList.get(i).SSID);
                } else {
                    //wifiName.setText("NO");
                    mCheckedList.remove(mList.get(i).SSID);
                }
                //Toast.makeText(mContext, mCheckedList.size() + "", Toast.LENGTH_SHORT).show();
                //Toast.makeText(mContext, i + "", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}
