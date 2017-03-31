package com.le.samplecodecamp.eui.update;

import android.content.Context;
import android.view.View;

import com.letv.shared.widget.LeBottomSheet;

/**
 * Created by zhangjiahao on 16-9-18.
 */
public class LeUpgradeProgressDialogBuilder {

    private final LeUpgradeBottomSheet mLeBottomSheet;
    private View.OnClickListener mCancellationListener;
    private String mContent, mCancellationBtnText;

    public LeUpgradeProgressDialogBuilder(Context context) {
        mLeBottomSheet = new LeUpgradeBottomSheet(context);
    }

    public LeUpgradeProgressDialogBuilder setCancellationListener(String desc, View.OnClickListener listener) {
        mCancellationBtnText = desc;
        mCancellationListener = listener;
        return this;
    }

    public LeUpgradeProgressDialogBuilder setContent(String content) {
        mContent = content;
        return this;
    }

    public LeUpgradeProgressDialogBuilder setIgnoreBackPress(boolean ignoreBackPress) {
        mLeBottomSheet.setIgnoreBackPress(ignoreBackPress);
        return this;
    }

    public LeUpgradeBottomSheet build() {
        mLeBottomSheet.setStyle(LeBottomSheet.BUTTON_PROGRESS,
                mCancellationListener,
                null,
                null,
                mContent == null ? null : new String[]{mCancellationBtnText},
                mContent,
                null,
                null,
                0xff518ef1,
                true);
        return mLeBottomSheet;
    }
}
