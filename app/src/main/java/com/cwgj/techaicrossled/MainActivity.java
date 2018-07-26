package com.cwgj.techaicrossled;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.cwgj.led_lib.LedDriverManager;
import com.cwgj.techaicrossled.device.CameraDeviceInfo;
import com.cwgj.techaicrossled.device.CameraDeviceManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText et_led_line, et_led_info;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_led_line = findViewById(R.id.et_led_line);

        et_led_info = findViewById(R.id.et_led_info);

        et_led_line.setText("1");
        et_led_line.setSelection(1);
        et_led_info.setText("哈");
        et_led_info.setSelection(1);

        findViewById(R.id.btn_send_msg).setOnClickListener(this);
        findViewById(R.id.btn_send_forever_msg).setOnClickListener(this);
        findViewById(R.id.btn_send_current_time_5s).setOnClickListener(this);
        findViewById(R.id.btn_send_current_time_0s).setOnClickListener(this);
        findViewById(R.id.btn_send_update_time).setOnClickListener(this);

        CameraDeviceManager.getInstance().setUp();
//        //以下应该放在登录成功之后
        CameraDeviceInfo cameraDeviceInfo = new CameraDeviceInfo();
        cameraDeviceInfo.ip =  "192.168.7.99";
        CameraDeviceManager.getInstance().setDevice(cameraDeviceInfo);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                boolean flag = CameraDeviceManager.getInstance().openDevice();
                Log.d("camera", flag?"连接成功":"连接失败");
//            }
//        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        CameraDeviceManager.getInstance().closeDevice();
//        CameraDeviceManager.getInstance().cleanUp();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int line = Integer.parseInt(et_led_line.getText().toString());
        String info = et_led_info.getText().toString();
        byte[] textBytes = null;
        if(id == R.id.btn_send_msg){
            textBytes = LedDriverManager.getInstance().packTextCommad(line, info);
        }else if(id == R.id.btn_send_forever_msg){
            textBytes = LedDriverManager.getInstance().packForeverTextCommad(line, info);
        }else if(id == R.id.btn_send_current_time_5s){
            textBytes = LedDriverManager.getInstance().packLocalDateTextCommad(line, 5);
        }else if(id == R.id.btn_send_current_time_0s){
            textBytes = LedDriverManager.getInstance().packLocalDateTextCommad(line, 0);
        }else if(id == R.id.btn_send_update_time){
            textBytes = LedDriverManager.getInstance().packUpdateDateTextCommand(System.currentTimeMillis());
        }
        CameraDeviceManager.getInstance().serialSend(textBytes);
    }
}
