package com.le.samplecodecamp.eui.update;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.le.samplecodecamp.R;

/**
 * Created by zhangjiahao on 17-2-23.
 */

public class NetworkConfirmFragment extends DialogFragment {

    private static final String ARG_APP_INFO = "arg_app_info";
    private AppInfo mAppInfo;
    private DialogListener mListener;

    public interface DialogListener {

        void onAcceptDownload();

        void onRejectDownload();
    }

    public static NetworkConfirmFragment newInstance(AppInfo appInfo) {
        NetworkConfirmFragment fragment = new NetworkConfirmFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_APP_INFO, appInfo);
        fragment.setArguments(args);
        fragment.setCancelable(false);
        return fragment;
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
        builder.setTitle(getContext().getString(R.string.le_tv_string_use_mobile_data_download_patch))
                .setPositiveBtn(getContext().getString(R.string.le_btn_string_confirm), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onAcceptDownload();
                        }
                    }
                })
                .setNegativeBtn(getContext().getString(R.string.le_btn_string_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onRejectDownload();
                        }
                    }
                });
        return builder.build();
    }

}
