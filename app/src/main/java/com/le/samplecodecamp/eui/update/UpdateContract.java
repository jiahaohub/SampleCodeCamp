package com.le.samplecodecamp.eui.update;

import android.net.Uri;

/**
 * Created by zhangjiahao on 17-4-19.
 */

public interface UpdateContract {

    interface View extends BaseView<Presenter> {

        void installApk(Uri uri);

        void showUpdateAnnouncement();

        void showNetworkConfirm();

        void requestDownloadUri();

        void showProgress();

        void progress(long total, long current);

        void dismiss();

        void finishSelf();
    }

    interface Presenter extends BasePresenter {

        void update(boolean accept);

        void installed(boolean success);

        void download(boolean accept);

        void cancelDownload();

        void receivedUri(Uri uri);
    }

}
