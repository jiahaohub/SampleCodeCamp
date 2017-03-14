package com.le.samplecodecamp.eui.domain;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.le.samplecodecamp.R;
import com.le.samplecodecamp.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class LogFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<LogContent.LogItem>> {

    private static final String TAG = "LogFragment";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_URI = "uri";
    private static final int LOADER_ID = 0x12;
    private int mColumnCount = 1;
    private ArrayList<Uri> mUris;
    private OnListFragmentInteractionListener mListener;
    private MyLogRecyclerViewAdapter mAdapter;

    public LogFragment() {
    }

    public static LogFragment newInstance(int columnCount, ArrayList<Uri> uris) {
        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putParcelableArrayList(ARG_URI, uris);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mUris = getArguments().getParcelableArrayList(ARG_URI);

            // 加载日志文件
            getLoaderManager()
                    .initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_list, container, false);

        // Set the mAdapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setHasFixedSize(true);
            mAdapter = new MyLogRecyclerViewAdapter(LogContent.ITEMS, mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<List<LogContent.LogItem>> onCreateLoader(int i, Bundle bundle) {
        LogLoader loader = new LogLoader(getContext(), mUris);
        loader.setUpdateThrottle(500);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<LogContent.LogItem>> loader, List<LogContent.LogItem> items) {
        if (items == null) {
            LogUtils.w(TAG, "finish load but result is empty.");
            return;
        }
        LogUtils.i(TAG, "finish load and get %d items.", items.size());
        mAdapter.swapContent(items);
    }

    @Override
    public void onLoaderReset(Loader<List<LogContent.LogItem>> loader) {
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(LogContent.LogItem item);
    }
}
