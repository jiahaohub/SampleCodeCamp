package com.le.samplecodecamp.eui.update;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.le.samplecodecamp.R;

public class AnnouncementFragment extends DialogFragment {

    private static final String ARG_APP_INFO = "arg_app_info";
    private AppInfo mAppInfo;
    private DialogListener mListener;

    public interface DialogListener {

        void onAcceptUpdate();

        void onRejectUpdate();
    }

    public static AnnouncementFragment newInstance(AppInfo appInfo) {
        AnnouncementFragment fragment = new AnnouncementFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_APP_INFO, appInfo);
        fragment.setArguments(args);
        fragment.setCancelable(false);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAppInfo = getArguments().getParcelable(ARG_APP_INFO);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DialogListener) {
            mListener = (DialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement UpdateListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LeUpgradeDialogBuilder builder = new LeUpgradeDialogBuilder(getContext());
        // 标题和内容，屏蔽返回键
        builder.setTitle(getContext().getString(R.string.le_tv_string_found_new_version))
                .setContent(mAppInfo.description);

        // 按键事件
        builder.setPositiveBtn(getContext().getString(R.string.le_btn_string_upgrade_now), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onAcceptUpdate();
                }
            }
        }).setNegativeBtn(getContext().getString(R.string.le_btn_string_exit), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onRejectUpdate();
                }
            }
        });
        return builder.build();
    }

}
