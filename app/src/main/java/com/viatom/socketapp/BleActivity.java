package com.viatom.socketapp;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class BleActivity extends AppCompatActivity {

    // pro
    private UUID write_uuid = UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3");
    private UUID notify_uuid = UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57");

    // er1
//    private UUID write_uuid = UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3");
//    private UUID notify_uuid = UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57");

    private String macAddrOxi = "";
    // "DB:AA:6C:51:E4:73"     "F2:31:30:30:30:36" => 0006         "D3:E5:B4:5E:1F:76" => er1 2126   "D4:7E:CB:63:91:B6" => 0000
    // bm88 "CA:FC:47:3E:2B:F7"
    // lepu 0449 "F1:30:30:34:34:39"
    private String macAddrPro = "F2:31:30:30:30:36";
    private String macAddrEr1 = "D3:A1:B3:75:E8:54";

    /****是否是全速跑*******/
    private boolean isFullSpeed = false;

    /****设备类型
     * 0-没有设备
     * 1-ER1
     * 2-checkme
     * *******/
    private int typeOfDevice = 0;


    private static RxBleClient rxBleClient;
    private static RxBleDevice rxBleDevice;
    private static RxBleConnection rxBleConnection;
    private static Disposable disposable;
    private static Disposable connectionDisposeable;

    private ArrayList<String> logs = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    private int sum = 0;
    private long start, current = 0;

    private long tryConnect, connectFinsihed, cmdSent, responseReceived = 0;
    private boolean connected = false;

    private byte[] cmd_pro = new byte[8];
    private byte[] cmd_er1 = new byte[8];

    @BindView(R.id.check_er1)
    CheckBox checkEr1;
    @BindView(R.id.check_pro)
    CheckBox checkPro;

    @BindView(R.id.msg)
    TextView msg;

    @BindView(R.id.summary)
    TextView summary;

    @BindView(R.id.logs)
    ListView logView;

    String TAG = "BleActivity";

    @OnClick(R.id.ble_connect)
    void bleConnect() {
        connect();
    }

    @OnClick(R.id.ble_run)
    void bleRun() {
        sum = 0;
        run();
    }

    @OnCheckedChanged(R.id.check_er1)
    void oxiChecked(CompoundButton button, boolean checked) {
        if (checked) {
            checkPro.setChecked(false);
            typeOfDevice = 1;
        }
    }

    @OnCheckedChanged(R.id.check_is_full_speed)
    void isFullSpeedChecked(CompoundButton button, boolean checked) {
        Log.i(TAG,"isFullSpeedChecked -> "+checked);
        if (checked) {
            isFullSpeed = true;
        } else {
            isFullSpeed = false;
        }
    }

    @OnCheckedChanged(R.id.check_pro)
    void proChecked(CompoundButton button, boolean checked) {
        if (checked) {
            checkEr1.setChecked(false);
            typeOfDevice = 2;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        int REQUEST_ENABLE_BT = 1;
        this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        rxBleClient = RxBleClient.create(this);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, logs);
        logView.setAdapter(adapter);

        setCmd();
    }


    private void connect() {
        // connect
        if (checkPro.isChecked()) {
            rxBleDevice = rxBleClient.getBleDevice(macAddrPro);
        } else {
            rxBleDevice = rxBleClient.getBleDevice(macAddrEr1);
        }

        addLogs("try connect: " + rxBleDevice.getMacAddress());
        tryConnect++;

        connectionDisposeable = rxBleDevice.observeConnectionStateChanges()
                .subscribe(
                        connectionState -> {
                            if (connectionState == RxBleConnection.RxBleConnectionState.CONNECTED) {
                                connected = true;
                                handler.sendEmptyMessageDelayed(0, 500);
                            } else if (connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
                                connected = false;
                                handler.sendEmptyMessageDelayed(1, 500);
                                disConnect();
                            }
                        },
                        throwable -> {
                            // Handle an error here.
                        }
                );

        disposable = rxBleDevice.establishConnection(false)
                .subscribe(
                        rxBleConnection -> {
                            addLogs("connected");
                            connectFinsihed++;
                            connected = true;
                            this.rxBleConnection = rxBleConnection;

                            rxBleConnection.setupNotification(notify_uuid)
                                    .doOnNext(
                                            notificationObservable -> {
                                                // Notification has been set up
                                                addLogs("notify setup");
                                            }
                                    )
                                    .flatMap(notificationObservable -> notificationObservable)
                                    .subscribe(
                                            bytes -> {
                                                responseReceived++;
                                                addLogs("RECEIVED: " + bytes.length + " " + HexString.bytesToHex(bytes));
                                                getBytes(bytes);
                                            },
                                            throwable -> {
                                                addLogs(throwable.getMessage());
                                            }
                                    );


                        },
                        throwable -> {
                            connected = false;
                            addLogs(throwable.getMessage());
                        }
                );

        // set notify

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    if (connected) {
                        run();
                        if (isFullSpeed) {
                            /****全速跑*******/
                        } else {
                            /****定时跑*******/
                            handler.sendEmptyMessageDelayed(0, 500);
                        }

                    }
                    break;
                case 1:
                    connect();
                    break;
            }


        }
    };

    private void run() {
        byte[] bytesToWrite;
        if (checkPro.isChecked()) {
            bytesToWrite = cmd_pro;
        } else if (checkEr1.isChecked()) {
            bytesToWrite = cmd_er1;
        } else {
            runOnUiThread(() -> {
                Toast.makeText(this, "please choose device type", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        if (start == 0) {
            start = System.currentTimeMillis();
        }

        rxBleConnection.writeCharacteristic(write_uuid, bytesToWrite)
                .subscribe(
                        bytes -> {
                            cmdSent++;
                            addLogs("SEND: " + HexString.bytesToHex(bytes));
                        },
                        throwable -> addLogs(throwable.getMessage())
                );
    }

    private void disConnect() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }

        if (connectionDisposeable != null) {
            connectionDisposeable.dispose();
            connectionDisposeable = null;
        }
    }

    private int getDeviceParam() {
        if (typeOfDevice == 1) {
            /****ER1*******/
            return 68;
        } else if (typeOfDevice == 2) {
            /****checkme*******/
            return 264;
        }

        return 0;

    }

    private void addLogs(String s) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String time = format.format(System.currentTimeMillis());

        runOnUiThread(() -> {
            String sumText = String.format("尝试连接次数：%1$d \n连接成功次数：%2$d \n发送命令次数：%3$d \n收到响应次数：%4$d", tryConnect, connectFinsihed, cmdSent, sum / getDeviceParam());
            summary.setText(sumText);
            logs.add(0, time + " " + s);
            adapter.notifyDataSetChanged();

        });
    }

    private void setCmd() {
        cmd_pro[0] = (byte) 0xaa;
        cmd_pro[1] = (byte) 0x14;
        cmd_pro[2] = (byte) 0xeb;
        cmd_pro[3] = (byte) 0x00;
        cmd_pro[4] = (byte) 0x00;
        cmd_pro[5] = (byte) 0x00;
        cmd_pro[6] = (byte) 0x00;
        cmd_pro[7] = (byte) 0xc6;

        cmd_er1[0] = (byte) 0xa5;
        cmd_er1[1] = (byte) 0xe1;
        cmd_er1[2] = (byte) 0x1e;
        cmd_er1[3] = (byte) 0x00;
        cmd_er1[4] = (byte) 0x00;
        cmd_er1[5] = (byte) 0x00;
        cmd_er1[6] = (byte) 0x00;
        cmd_er1[7] = (byte) 0x69;
    }

    private void getBytes(byte[] bytes) {
        sum += bytes.length;
        /****全速跑*******/
        if (isFullSpeed) {
            if (sum % getDeviceParam() == 0) {
                run();
            }
//            if (sum % 68 == 0 || sum % 264 == 0) {
//                run();
//            }
        } else {

        }

        current = System.currentTimeMillis();
        runOnUiThread(() -> {
            long duration = current - start;
            float speed = (float) sum / (current - start);
            msg.setText("sum: " + sum + " duration: " + duration + " speed: " + speed + "kb/s");
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
