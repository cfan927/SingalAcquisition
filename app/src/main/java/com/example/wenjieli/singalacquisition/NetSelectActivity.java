package com.example.wenjieli.singalacquisition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.bluetooth.*;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Wenjie Li on 2016/5/27.
 */
public class NetSelectActivity extends AppCompatActivity {
    private Button refresh, ok;
    private ListView wifilv, btlv;
    private WifiManager wifiManager;
    private ArrayList<ScanResult> list;//存放scanresult
    private ArrayList<String> checkedWifiList = new ArrayList<>();//存放选中的wifi
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> devices;//存放所有蓝牙设备
    private ArrayList<String> checkedBtList = new ArrayList<>();//存放选中的蓝牙
    private BluetoothListAdapter btAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        initView();

        //打开wifi
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        //打开蓝牙
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        scanWifi();
        scanBT();
        //扫描wifi和蓝牙
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
                scanBT();
            }
        });

        //跳转activity
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(NetSelectActivity.this, MainActivity.class);
                i.putStringArrayListExtra("wifi", checkedWifiList);
                i.putStringArrayListExtra("bluetooth", checkedBtList);
                startActivity(i);
                NetSelectActivity.this.finish();
            }
        });
    }

    //扫描wifi
    public void scanWifi() {
        wifiManager.startScan();
        list = (ArrayList<ScanResult>) wifiManager.getScanResults();
        WifiListAdapter wifiAdapter = new WifiListAdapter(NetSelectActivity.this, list, checkedWifiList, R.layout.wifi_list_item);
        wifilv.setAdapter(wifiAdapter);
    }

    //扫描蓝牙
    public void scanBT() {
        devices = new ArrayList<>();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);
                //ok.setText(device.toString()+"");
                btAdapter = new BluetoothListAdapter(NetSelectActivity.this, devices, checkedBtList, R.layout.wifi_list_item);
                btlv.setAdapter(btAdapter);
            }
        }
    };

    public void initView() {
        refresh = (Button) findViewById(R.id.refresh);
        ok = (Button) findViewById(R.id.ok);
        wifilv = (ListView) findViewById(R.id.wifi_list);
        btlv = (ListView) findViewById(R.id.bt_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
