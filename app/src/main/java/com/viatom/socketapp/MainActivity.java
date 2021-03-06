package com.viatom.socketapp;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.util.Base64;
import android.widget.RelativeLayout;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;

    @BindView(R.id.logs)
    ListView logView;

//    @BindView(R.id.oxiview)
//    OxiView oxiView;

    private OxiView oxiView;

    private ArrayList<String> logs = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    private String CHANNEL = "SleepO2 0001";
    private String MSG = "";

    private static Timer waveTimer;
    private static TimerTask waveTask;

    @OnClick(R.id.send)
    void sendMsg() {
        sendMsg(MSG);
    }

    @OnClick(R.id.join)
    void join() {
        joinChannel();
    }

    @OnClick(R.id.connect)
    void connect() {
        socketConnect();
    }

    @OnClick(R.id.bt)
    void intoBle() {
        startActivity(new Intent(this, BleActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ButterKnife.bind(this);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, logs);
        logView.setAdapter(adapter);

        iniMsg();
        loadOxiView();
    }

    private void onDataReceived(JSONObject obj) {
        String channel = obj.optString("channel");
        String name = obj.optString("name");
        String dataTyoe = obj.optString("dataType");
        String b64str = obj.optString("data");

        byte[] waveBytes = Base64.decode(b64str, Base64.DEFAULT);
        Wavedata data = new Wavedata(waveBytes);
        DataController.receive(data.getFs());
    }

    private void loadOxiView() {
//        LayoutInflater inflater = LayoutInflater.from(this);
//        RelativeLayout ecgLayout = (RelativeLayout) inflater.inflate(R.layout.activity_main, null);
//
//        RelativeLayout rootLayout = (RelativeLayout) findViewById(R.layout.activity_main);
//        rootLayout.addView(ecgLayout);

        RelativeLayout waveLayout = findViewById(R.id.rl_wave);
        waveLayout.measure(0, 0);

        oxiView = new OxiView(this);
        waveLayout.addView(oxiView);
    }

    private void iniMsg() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("channel", CHANNEL);
//            obj.put("sn", "123456");
            obj.put("name", CHANNEL);
            obj.put("dataType", "pulse wave");
            obj.put("data", "I am base64string");
        } catch (JSONException e) {
            e.printStackTrace();
            addLogs(e.toString());
        }
        MSG = obj.toString();
    }

    private void startTimer() {
        stopTimer();
        waveTimer = new Timer();
        waveTask = new TimerTask() {
            @Override
            public void run() {
                float[] temp = DataController.draw(5);
                if (temp != null) {
                    DataController.feed(temp);
                    runOnUiThread(()->oxiView.invalidate());
                }
            }
        };
        int delay = 50;
//        if (DataController.dataRec.length > 100) {
//            delay = 48;
//        }
        waveTimer.schedule(waveTask, delay, 40);
    }

    private void stopTimer() {
        if (waveTask != null) {
            waveTask.cancel();
            waveTask = null;
        }

        if (waveTimer != null) {
            waveTimer.cancel();
            waveTimer = null;
        }
    }


    private void addLogs(String s) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String time = format.format(System.currentTimeMillis());

        runOnUiThread(()->{
            logs.add(0,time + " " + s);
            adapter.notifyDataSetChanged();
        });
    }

    private void sendMsg(String s) {
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit("data", s);
            addLogs("send: " + s);
        }

    }

    private void joinChannel() {
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit("join", CHANNEL);
            addLogs("join: " + CHANNEL);

            startTimer();
        }
    }

    public void socketConnect() {
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = false;

        try {
            // http://chat.socket.io  => http://socket.viatomtech.com.cn/socket.io/
            mSocket = IO.socket("http://socket.viatomtech.com.cn/", opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            addLogs(e.toString());
        }

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
//                mSocket.emit("foo", "hi");
                addLogs("connected");
            }

        });
        mSocket.on("data", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                String s = (String) args[0];
                try {
                    JSONObject object = new JSONObject(s);
                    onDataReceived(object);
                } catch (JSONException e) {
                    addLogs(e.toString());
                }
//                addLogs("receive: " + object.toString());
                addLogs("receive: " + args[0]);
            }

        });
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                addLogs("disconnected");
            }

        });
        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                addLogs("connect error");
            }
        });
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                addLogs("connect timeout");
            }
        });

        mSocket.connect();
    }
}
