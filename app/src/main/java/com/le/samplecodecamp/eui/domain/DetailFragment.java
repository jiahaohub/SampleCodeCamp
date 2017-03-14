package com.le.samplecodecamp.eui.domain;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.le.samplecodecamp.R;

/**
 * Created by zhangjiahao on 17-3-13.
 */

public class DetailFragment extends DialogFragment {

    private static final String ARG_DETAIL = "detail";
    private String mDetail;

    public static DialogFragment newInstance(String detail) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DETAIL, detail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen);

        if (getArguments() != null) {
            mDetail = getArguments().getString(ARG_DETAIL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        TextView detailView = (TextView) view.findViewById(R.id.detail);
        detailView.setText(mDetail);
        return view;
    }

}
