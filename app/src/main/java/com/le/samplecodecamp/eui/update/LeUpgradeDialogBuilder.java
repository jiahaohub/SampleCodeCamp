package com.le.samplecodecamp.eui.update;

import android.content.Context;
import android.graphics.Color;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.letv.shared.widget.LeBottomSheet;

/**
 * Created by zhangjiahao on 16-9-8.
 */
public class LeUpgradeDialogBuilder {

    private final Context mContext;
    private final LeUpgradeBottomSheet mLeBottomSheet;

    private View.OnClickListener mPositiveListener, mNegativeListener;
    private String mPositiveBtnText, mNegativeBtnText, mTitle, mContent;

    public LeUpgradeDialogBuilder(Context context) {
        mContext = context;
        mLeBottomSheet = new LeUpgradeBottomSheet(context);
    }

    public LeBottomSheet getLeBottomSheet() {
        return mLeBottomSheet;
    }

    public LeUpgradeDialogBuilder setTitle(String title) {
        mTitle = title;
        return this;
    }

    public LeUpgradeDialogBuilder setContent(String content) {
        mContent = content;
        return this;
    }

    public LeUpgradeDialogBuilder setPositiveBtn(String desc, View.OnClickListener listener) {
        mPositiveBtnText = desc;
        mPositiveListener = listener;
        return this;
    }

    public LeUpgradeDialogBuilder setNegativeBtn(String desc, View.OnClickListener listener) {
        mNegativeBtnText = desc;
        mNegativeListener = listener;
        return this;
    }

    public LeUpgradeDialogBuilder setIgnoreBackPress(boolean ignoreBackPress) {
        mLeBottomSheet.setIgnoreBackPress(ignoreBackPress);
        return this;
    }

    public LeBottomSheet build() {
        mLeBottomSheet.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                mPositiveListener,
                mNegativeListener,
                null,
                new String[]{mPositiveBtnText, mNegativeBtnText},
                mTitle,
                mContent,
                null,
                0xff2395ee,
                false);
        initContentView();
        return mLeBottomSheet;
    }

    private void initContentView() {
        TextView content = mLeBottomSheet.getContent();
        content.setMovementMethod(ScrollingMovementMethod.getInstance());
        content.setTextColor(Color.parseColor("#898b94"));
        content.setTextSize(14);
        content.setGravity(Gravity.START);
        content.setMaxHeight(800);
    }

    private static int dip2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

}
