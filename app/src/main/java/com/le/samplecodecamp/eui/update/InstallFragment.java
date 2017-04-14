package com.le.samplecodecamp.eui.update;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.le.samplecodecamp.utils.LogUtils;

public class InstallFragment extends Fragment {

    private static final String TAG = "InstallFragment";
    private static final String ARG_URI = "arg_uri";
    private static final String ARG_APP_INFO = "arg_app_info";
    private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";
    private static final int REQ_CODE = 0x01;

    private Uri mUri;
    private AppInfo mAppInfo;

    public InstallFragment() {
        // Required empty public constructor
    }

    public static InstallFragment getInstance(Uri uri, AppInfo appInfo) {
        InstallFragment fragment = new InstallFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        args.putParcelable(ARG_APP_INFO, appInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUri = getArguments().getParcelable(ARG_URI);
            mAppInfo = getArguments().getParcelable(ARG_APP_INFO);
            installApk(mUri);
        }
    }

    private void installApk(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, PACKAGE_MIME_TYPE);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQ_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.i(TAG, "request code %d, result code %d, data %s", requestCode, resultCode, data);
        if (REQ_CODE == requestCode) {
            switch (resultCode) {
                case Activity.RESULT_CANCELED:
                    if (mAppInfo.isForce()) {
                        AnnouncementFragment.getInstance(mAppInfo).
                                show(getFragmentManager(), mAppInfo.packageName);
                    }
                    break;
            }
            getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        }
    }
}
