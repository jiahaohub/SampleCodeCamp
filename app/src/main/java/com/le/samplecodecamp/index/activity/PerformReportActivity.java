package com.le.samplecodecamp.index.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.le.samplecodecamp.R;
import com.le.samplecodecamp.utils.LogUtils;
import com.letv.indexreport.api.CollectServiceProxy;
import com.letv.indexreport.api.PerformanceReportUtils;
import com.letv.indexreport.api.ReportConfig;

import java.lang.reflect.Method;

public class PerformReportActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "PerformReportActivity";
    private Button bt_test;
    private EditText et_put;
    private Button bt_put;
    private TextView tv_value;
    private Button bt_get;
    private Button bt_uri;

    private TextView tv_uri;
    private Button bt_init;
    private Button bt_cover;
    private Button bt_append;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perform_report);

        bt_test = (Button) findViewById(R.id.bt_test);
        bt_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Uri uri = Uri.parse("content://com.letv.indexreport.performancereportprovider/collect_records/1");
//                try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
//                    if (cursor == null) {
//                        LogUtils.w(TAG, "cursor is null");
//                        return;
//                    }
//                    LogUtils.i(TAG, "cursor size is %d", cursor.getCount());
//                }
                Uri uri =
                        Uri.parse("content://com.letv.indexreport.performancereportprovider/report_config");
                ContentValues values = new ContentValues();
                values.put("name", "aaa");
                values.put("value", "10010");
                String value = PerformanceReportUtils.getReportConfig(PerformReportActivity.this,
                        "aaa", "");
                if (TextUtils.isEmpty(value)) {
                    getContentResolver().insert(uri, values);
                } else {
                    getContentResolver().update(uri, values, "name = ?", new String[]{"aaa"});
                }
            }
        });

        et_put = (EditText) findViewById(R.id.et_put);
        bt_put = (Button) findViewById(R.id.bt_put);
        bt_put.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable text = et_put.getText();
                Uri uri =
                        Uri.parse("content://com.letv.indexreport.performancereportprovider/report_config");
                ContentValues values = new ContentValues();
                values.put("name", ReportConfig.SYS_BACKUP_PERIOD);
                values.put("value", text.toString());
                int update = getContentResolver().update(uri, values, "name = ?", new String[]{ReportConfig.SYS_BACKUP_PERIOD});
                Toast.makeText(PerformReportActivity.this,
                        update > 0 ? "success" : "fail", Toast.LENGTH_SHORT).show();
            }
        });

        tv_value = (TextView) findViewById(R.id.tv_value);
        bt_get = (Button) findViewById(R.id.bt_get);
        bt_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = PerformanceReportUtils.getReportConfig(PerformReportActivity.this,
                        ReportConfig.SYS_BACKUP_PERIOD, 2 * 60 * 60 * 1000 + "");
//                String value = ReportConfig.getString(getContentResolver(), ReportConfig.SYS_BACKUP_PERIOD);
//                if (TextUtils.isEmpty(value)) {
//                    value = 2 * 60 * 60 * 1000 + ""; // 默认值2小时
//                }
                long sys_backup_period = Long.parseLong(value);
                tv_value.setText("system backup period: " + sys_backup_period);
            }
        });

        bt_uri = (Button) findViewById(R.id.bt_uri);
        bt_uri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PerformanceReportUtils.notifyReport(PerformReportActivity.this);
                } catch (RemoteException e) {
                    LogUtils.w(TAG, e);
                }
            }
        });

        tv_uri = (TextView) findViewById(R.id.tv_uri);
        bt_init = (Button) findViewById(R.id.bt_init);
        bt_init.setOnClickListener(this);
        bt_cover = (Button) findViewById(R.id.bt_cover);
        bt_cover.setOnClickListener(this);
        bt_append = (Button) findViewById(R.id.bt_append);
        bt_append.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int type = -1;
        switch (v.getId()) {
            case R.id.bt_init:
                type = 0;
                break;
            case R.id.bt_cover:
                type = 1;
                break;
            case R.id.bt_append:
                type = 2;
                break;
        }
        if (type != -1) {
            /*new AsyncTask<Integer, Void, Uri>() {
                @Override
                protected Uri doInBackground(Integer... params) {
                    try {
                        Uri uri = PerformanceReportUtils.getBackupFileUri(PerformReportActivity.this, params[0]);
                        LogUtils.i(TAG, "Uri: %s", uri);
                        if (uri != null) {
                            try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w")) {
                                if (pfd != null) {
                                    String out = "Type:" +
                                            params[0] +
                                            ", Uri:" +
                                            uri.toString();
                                    LogUtils.i(TAG, "write to file %s", out);
                                    Source source = Okio.source(new ByteArrayInputStream(out.getBytes()));
                                    BufferedSink sink = Okio.buffer(Okio.sink(new FileOutputStream(pfd.getFileDescriptor())));
                                    sink.writeAll(source);
                                    sink.flush();
                                }
                            } catch (IOException e) {
                                LogUtils.w(TAG, e);
                            }
                            return uri;
                        }
                    } catch (RemoteException e) {
                        LogUtils.w(TAG, e, "fail in get backup file uri.");
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Uri uri) {
                    if (uri != null) {
                        tv_uri.setText(uri.toString());
                    }
                }
            }.execute(type);*/
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        byte[] data = "https://android.googlesource.com/platform/frameworks/volley/+/0e406003b5d434d8f16d7d6ad97d446060b788e6".getBytes();
                        Class<?> clazz = Class.forName("android.os.ParcelFileDescriptor");
                        Method method = clazz.getMethod("fromData", byte[].class, String.class);
                        ParcelFileDescriptor pfd = (ParcelFileDescriptor) method.invoke(null, data, "test");
                        CollectServiceProxy service = new CollectServiceProxy(PerformReportActivity.this);
                        service.notifyReport(1, true, pfd);
                    } catch (Exception e) {
                        LogUtils.w(TAG, e);
                    }
                    return null;
                }
            }.execute();
        }
    }
}
