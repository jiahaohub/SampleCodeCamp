package com.le.samplecodecamp.eui.update;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by spring on 16-9-8.
 */
public class AppInfo implements Parcelable {
    public int upgradeType;//1:可选升级 (用户可以取消升级,下次不再提示升级) 2:推荐升级 (用户可以取消升级,下次会提示 升级 ) 3:强制升级(用户不能取消)

    public String apkVersion;//最新版本号

    public String description;//更新日志

    public String fileUrl;//下载地址

    public String packageName;//包名

    public String fileMd5;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.upgradeType);
        dest.writeString(this.apkVersion);
        dest.writeString(this.description);
        dest.writeString(this.fileUrl);
        dest.writeString(this.packageName);
        dest.writeString(this.fileMd5);
    }

    public AppInfo() {
    }

    protected AppInfo(Parcel in) {
        this.upgradeType = in.readInt();
        this.apkVersion = in.readString();
        this.description = in.readString();
        this.fileUrl = in.readString();
        this.packageName = in.readString();
        this.fileMd5 = in.readString();
    }

    public boolean isForce() {
        return upgradeType == 3;
    }

    public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel source) {
            return new AppInfo(source);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    @Override
    public String toString() {
        return packageName + "@" +
                apkVersion + "@" +
                upgradeType + "@" +
                fileUrl + "@" +
                fileMd5;
    }

}
