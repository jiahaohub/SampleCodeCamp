package com.le.samplecodecamp.eui.update;

import android.content.Context;

import com.letv.shared.widget.LeBottomSheet;

/**
 * Created by zhangjiahao on 16-9-22.
 */
public class LeUpgradeBottomSheet extends LeBottomSheet {

    private boolean mIgnoreBackPress;

    public LeUpgradeBottomSheet(Context context) {
        super(context);
    }

    public void setIgnoreBackPress(boolean ignoreBackPress) {
        mIgnoreBackPress = ignoreBackPress;
    }

    @Override
    public void onBackPressed() {
        if (!mIgnoreBackPress) {
            super.onBackPressed();
        }
    }

}
