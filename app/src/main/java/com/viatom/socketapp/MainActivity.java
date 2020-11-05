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
import android.view.WindowManager;
import android.widget.ArrayAdapter;
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

    private String CHANNEL = "18664972432";
    private String MSG = "";
    private String VAL_PKG = "";

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

    @OnClick(R.id.signin)
    void setVal() {
        if (mSocket != null && mSocket.connected()) {

            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "Signin");
                obj.put("channel", CHANNEL);
                obj.put("linkId", 12);
                obj.put("clientSn", CHANNEL);
                obj.put("clientName", "O2 1234");
                obj.put("clientType", "Master");
                obj.put("branchCode", "1231231231");
                obj.put("version", "0.0.1");
                obj.put("versionCode", "20");
            } catch (JSONException e) {
                e.printStackTrace();
                addLogs(e.toString());
            }
            String signin = obj.toString();


            mSocket.emit("Signin", signin);
            addLogs("Signin: " + signin);
        }
    }

    @OnClick(R.id.update_state)
    void getVal() {
        if (mSocket != null && mSocket.connected()) {

            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "UpdateState");
                obj.put("channel", CHANNEL);
                obj.put("linkId", 12);
                obj.put("wifiState", 1);
                obj.put("bleState", 1);
                obj.put("fingerDetect", 1);
            } catch (JSONException e) {
                e.printStackTrace();
                addLogs(e.toString());
            }
            String query = obj.toString();


            mSocket.emit("UpdateState", query);
            addLogs("UpdateState: " + query);
        }
    }

    @OnClick(R.id.query_info)
    void query_info() {
        if (mSocket != null && mSocket.connected()) {

            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "QueryInfo");
                obj.put("channel", CHANNEL);
            } catch (JSONException e) {
                e.printStackTrace();
                addLogs(e.toString());
            }
            String query = obj.toString();


            mSocket.emit("QueryInfo", query);
            addLogs("QueryInfo: " + query);
        }
    }
    @OnClick(R.id.send_command)
    void sendCommand() {
        if (mSocket != null && mSocket.connected()) {

            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "ControlSend");
                obj.put("channel", CHANNEL);
                obj.put("command", 1);
                obj.put("detail", "!23123123");
            } catch (JSONException e) {
                e.printStackTrace();
                addLogs(e.toString());
            }
            String query = obj.toString();


            mSocket.emit("ControlSend", query);
            addLogs("ControlSend: " + query);
        }
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
        iniHeartBeat();
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

    private void iniHeartBeat() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("name", "get val");
//            obj.put("sn", "123456");
            obj.put("dataType", "lalalala");
        } catch (JSONException e) {
            e.printStackTrace();
            addLogs(e.toString());
        }
        VAL_PKG = obj.toString();
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
                    runOnUiThread(() -> oxiView.invalidate());
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

        runOnUiThread(() -> {
            logs.add(0, time + " " + s);
            if (logs.size() > 20) {
                logs.remove(logs.size()-1);
            }
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
            mSocket = IO.socket("http://sockettest.viatomtech.com.cn/", opts);
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
        mSocket.on("Info", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                String s = (String) args[0];
                addLogs("Info: " + s);
            }

        });
        mSocket.on("ControlSend", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                String s = (String) args[0];
                addLogs("ControlSend: " + s);

                if (mSocket != null && mSocket.connected()) {

                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("type", "ControlResponse");
                        obj.put("channel", CHANNEL);
                        obj.put("command", 1);
                        obj.put("state", "ok");
                        obj.put("detail", "i am response");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        addLogs(e.toString());
                    }
                    String query = obj.toString();


                    mSocket.emit("ControlResponse", query);
                    addLogs("ControlResponse: " + query);
                }
            }

        });
        mSocket.on("ControlResponse", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                String s = (String) args[0];
                addLogs("ControlResponse: " + s);
            }

        });
        mSocket.on("get",  new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                String s = (String) args[0];
                addLogs("getVal: " + s);
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
