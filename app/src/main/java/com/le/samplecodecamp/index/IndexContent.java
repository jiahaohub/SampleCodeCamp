package com.le.samplecodecamp.index;

import com.le.samplecodecamp.index.activity.LogFileActivity;
import com.le.samplecodecamp.index.activity.LogMonitorActivity;
import com.le.samplecodecamp.index.activity.PerformReportActivity;
import com.le.samplecodecamp.index.activity.PerformUpgradeActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangjiahao on 17-2-20.
 */

public class IndexContent {

    public static final List<IndexItem> ITEMS = new ArrayList<>();

    static {
        addItem(new IndexItem("1", "LogFile", LogFileActivity.class));
        addItem(new IndexItem("2", "show me the fucking log", LogMonitorActivity.class));
        addItem(new IndexItem("3", "Update Apk", PerformUpgradeActivity.class));
        addItem(new IndexItem("4", "Performance report test", PerformReportActivity.class));
    }

    private static void addItem(IndexItem item) {
        ITEMS.add(item);
    }

    public static class IndexItem {
        public final String id;
        public final String content;
        public final Class targetClass;

        public IndexItem(String id, String content, Class targetClass) {
            this.id = id;
            this.content = content;
            this.targetClass = targetClass;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
