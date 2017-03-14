package com.le.samplecodecamp.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.le.samplecodecamp.R;
import com.le.samplecodecamp.utils.LogUtils;

public class LogFileActivity extends AppCompatActivity {

    private Button btn_log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_file);

        btn_log = (Button) findViewById(R.id.btn_log);

        btn_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.wtf("zjh", new IllegalStateException("somewhere has an error."), "boom boom boom!!!");
            }
        });
    }

}
