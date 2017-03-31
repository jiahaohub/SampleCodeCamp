package com.le.samplecodecamp.eui.update;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.le.samplecodecamp.R;
import com.letv.shared.widget.LeBottomSheet;

/**
 * Created by zhangjiahao on 17-2-23.
 */

public class NetworkConfirmFragment extends DialogFragment {

    private static final String ARG_APP_INFO = "arg_app_info";
    private AppInfo mAppInfo;
    private UpdateListener mListener;

    public static NetworkConfirmFragment getInstance(AppInfo appInfo) {
        NetworkConfirmFragment fragment = new NetworkConfirmFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_APP_INFO, appInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mAppInfo = getArguments().getParcelable(ARG_APP_INFO);
        }

        // 没网
        if (!NetStatusUtils.isNetWorkAvailable(getContext())) {
            if (mListener != null) {
                mListener.onFail(mAppInfo.packageName, 1);
            }
            dismissAllowingStateLoss();
            return;
        }

        if (NetStatusUtils.isWifi(getContext())) {
            dismissAllowingStateLoss();
            //TODO download fragment
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UpdateListener) {
            mListener = (UpdateListener) context;
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
        builder.setTitle(getContext().getString(R.string.le_tv_string_use_mobile_data_download_patch))
                .setPositiveBtn(getContext().getString(R.string.le_btn_string_confirm), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissAllowingStateLoss();
                        //TODO download fragment
                    }
                })
                .setNegativeBtn(getContext().getString(R.string.le_btn_string_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissAllowingStateLoss();
                        if (mListener != null && mAppInfo.isForce()) {
                            mListener.onExit();
                        }
                    }
                });
        LeBottomSheet leBottomSheet = builder.build();
        leBottomSheet.setCanceledOnTouchOutside(false);
        return leBottomSheet;
    }

}
