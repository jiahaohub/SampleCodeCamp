package com.le.samplecodecamp.eui.domain;

import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.le.samplecodecamp.R;
import com.le.samplecodecamp.eui.domain.LogFragment.OnListFragmentInteractionListener;

import java.util.List;

public class MyLogRecyclerViewAdapter extends RecyclerView.Adapter<MyLogRecyclerViewAdapter.ViewHolder> {

    private List<LogContent.LogItem> mItems;
    private final OnListFragmentInteractionListener mListener;

    public MyLogRecyclerViewAdapter(List<LogContent.LogItem> items, OnListFragmentInteractionListener listener) {
        mItems = items;
        mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mItems.get(position);
        holder.mNameView.setText(mItems.get(position).name);
        holder.mSizeView.setText(
                Formatter.formatFileSize(holder.mView.getContext(), mItems.get(position).size));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void swapContent(List<LogContent.LogItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mSizeView;
        public LogContent.LogItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mSizeView = (TextView) view.findViewById(R.id.size);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
