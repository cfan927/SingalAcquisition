package com.example.wenjieli.singalacquisition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.bluetooth.*;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAcc;//加速度计
    private Sensor mGyr;//陀螺仪
    private Sensor mMag;//磁场

    private float[] accValues = new float[3], gyrValues = new float[3], magValues = new float[3];//存放传感器获取的值

    private TextView acctv, gyrtv, magtv, wifitv, bttv;
    private Button save5;

    private int writeSQL = 0;//写入数据库开关：0否1是
    private File file = new File("//sdcard/mydb.db");
    private SQLiteDatabase db;
    private long timeToSave;//记录开始写入数据的时间，作为数据库表的名称
    private WifiManager wifiManager;
    private Map<String, Integer> map = new HashMap<>();//存放要显示的wifi列表
    private List<String> dbList;
    private ArrayList<String> checkedWifiList;//传入的选中wifi列表
    private ArrayList<String> checkedBtList;//传入的选中蓝牙列表

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> btDevices = new HashSet<>();
    private Map<String, Short> btMap = new HashMap<>();//存放要显示的蓝牙列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        db = SQLiteDatabase.openOrCreateDatabase(file, null);//打开或创建数据库

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);//加速度计
        mGyr = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);//陀螺仪
        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);//磁场

        save5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (writeSQL == 0) {
                    writeSQL = 1;
                    timeToSave = System.currentTimeMillis();
                    dbList = new ArrayList<String>();
                } else {
                    //loading...
                    writeSQL = 0;
                    db.execSQL("CREATE TABLE t" + timeToSave + " (time INT, acc1 REAL, acc2 REAL, acc3 REAL, gyr1 REAL, gyr2 REAL, gyr3 REAL, mag1 REAL, mag2 REAL, mag3 REAL, wifi TEXT, bluetooth TEXT)");
                    save5.setClickable(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < dbList.size(); i++) {
                                db.execSQL(dbList.get(i));
                                //Log.i("AAA",dbList.get(i).toString());
                            }
                            Message msg1 = new Message();
                            msg1.what = 1;
                            mHandler.sendMessage(msg1);
                        }
                    }).start();
                    save5.setText("正在写入...");
                    //loading vanish
                }
            }
        });
        wifiThread.start();
        btThread.start();
    }

    public void initView() {
        acctv = (TextView) findViewById(R.id.acc);
        gyrtv = (TextView) findViewById(R.id.gyr);
        magtv = (TextView) findViewById(R.id.mag);
        save5 = (Button) findViewById(R.id.save5);
        wifitv = (TextView) findViewById(R.id.wifi);
        bttv = (TextView) findViewById(R.id.bluetooth);
        checkedWifiList = getIntent().getStringArrayListExtra("wifi");
        checkedBtList = getIntent().getStringArrayListExtra("bluetooth");
        //bttv.setText(checkedBtList.toString() + "");
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION://加速度计
                accValues[0] = sensorEvent.values[0];
                accValues[1] = sensorEvent.values[1];
                accValues[2] = sensorEvent.values[2];
                acctv.setText("加速度计：\n" + accValues[0] + "\n" + accValues[1] + "\n" + accValues[2] + "\n");
                if (writeSQL == 1) {
                    save5.setText((int) ((System.currentTimeMillis() - timeToSave) / 1000) + "s");
                    dbList.add("INSERT INTO t" + timeToSave + " VALUES(" + System.currentTimeMillis() + "," + accValues[0] + "," + accValues[1] + "," + accValues[2] + "," + gyrValues[0] + "," + gyrValues[1] + "," + gyrValues[2] + "," + magValues[0] + "," + magValues[1] + "," + magValues[2] + ",'" + map.toString() + "','" + btMap.toString() + "')");
                    //Log.i("BBB","INSERT INTO t" + timeToSave + " VALUES(" + System.currentTimeMillis() + "," + accValues[0] + "," + accValues[1] + "," + accValues[2] + "," + gyrValues[0] + "," + gyrValues[1] + "," + gyrValues[2] + "," + magValues[0] + "," + magValues[1] + "," + magValues[2] + ",'" + map.toString() + "','" + btMap.toString() + "')");
                }
                break;
            case Sensor.TYPE_GYROSCOPE://陀螺仪
                gyrValues[0] = sensorEvent.values[0];
                gyrValues[1] = sensorEvent.values[1];
                gyrValues[2] = sensorEvent.values[2];
                gyrtv.setText("陀螺仪：\n" + gyrValues[0] + "\n" + gyrValues[1] + "\n" + gyrValues[2] + "\n");
                break;
            case Sensor.TYPE_MAGNETIC_FIELD://磁场
                magValues[0] = sensorEvent.values[0];
                magValues[1] = sensorEvent.values[1];
                magValues[2] = sensorEvent.values[2];
                magtv.setText("磁场：\n" + magValues[0] + "\n" + magValues[1] + "\n" + magValues[2] + "\n");
                break;
            default:
                break;
        }
    }

    //扫描wifi
    Thread wifiThread = new Thread(new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                while (true) {
                    //map.clear();
                    wifiManager.startScan();
                    ArrayList<ScanResult> list = (ArrayList<ScanResult>) wifiManager.getScanResults();
                    for (int i = 0; i < list.size(); i++) {
                        for (int j = 0; j < checkedWifiList.size(); j++) {
                            if (list.get(i).SSID.equals(checkedWifiList.get(j))) {
                                map.put(list.get(i).SSID, list.get(i).level);
                            }
                        }
                    }
                    Message msg = new Message();
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                }
            }
        }
    });
    //扫描蓝牙
    Thread btThread = new Thread(new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                while (true) {
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    mBluetoothAdapter.startDiscovery();
                }
            }
        }
    });
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDevices.add(device);
                short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                for(int i=0;i<checkedBtList.size();i++){
                    if(device.getName().equals(checkedBtList.get(i))){
                        btMap.put(device.getName() + "", rssi);
                    }
                }

                Message msg = new Message();
                msg.what = 2;
                mHandler.sendMessage(msg);
            }
        }
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    wifitv.setText("WIFI：\n" + map.toString()+"\n");
                    break;
                case 1:
                    save5.setText("保存数据");
                    save5.setClickable(true);
                    break;
                case 2:
                    bttv.setText("Bluetooth：\n" + btMap.toString());
                    break;
            }
        }
    };
    //定时任务
    private Handler timerHandler = new Handler();
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            timerHandler.postDelayed(this,50);//50ms
        }
    };

    /********************************************************************************/
    /********************************* 生命周期函数 *********************************/
    /********************************************************************************/
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //传感器获取精度变化时触发
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);//加速度计
        mSensorManager.registerListener(this, mGyr, SensorManager.SENSOR_DELAY_NORMAL);//陀螺仪
        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_NORMAL);//磁场
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    //再按一次返回键退出
    private long temptime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            if (System.currentTimeMillis() - temptime > 2000) // 2s内再次选择back键有效
            {
                System.out.println(Toast.LENGTH_LONG);
                Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_LONG).show();
                temptime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0); //凡是非零都表示异常退出!0表示正常退出!
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
