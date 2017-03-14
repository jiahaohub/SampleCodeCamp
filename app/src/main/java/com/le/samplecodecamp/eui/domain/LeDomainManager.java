package com.le.samplecodecamp.eui.domain;

import android.content.AsyncQueryHandler;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhangjiahao on 16-2-29.
 */
public class LeDomainManager {

    private static final String TAG = "LeDomainManager";

    public static boolean DEBUG = Log.isLoggable(TAG, Log.INFO);

    private static final String LEUI_COUNTRY_AREA_REGION_SETTINGS = "leui_country_area_region_settings";
    private final ContentResolver mResolver;
    private final int mPid;
    private final InnerHandler mInnerHandler;

    public LeDomainManager(ContentResolver resolver) {
        mResolver = resolver;
        mPid = android.os.Process.myPid();
        mInnerHandler = new InnerHandler(resolver);
    }

    /**
     * @see #getDomainByRegion
     * @deprecated 该方法获取到的域名有可能与服务器不一致，不是服务器最新的配置，推荐使用异步方法 getDomainByRegion
     */
    @Deprecated
    public Map<String, String> getDomain(String region, String[] labels) {
        if (region == null) {
            region = getCurrentRegion();
        }
        if (labels == null) {
            labels = getLabels(region);
        }
        mResolver.call(DomainContract.BASE_CONTENT_URI, DomainContract.METHOD_TRIGGER, region.toLowerCase(), null);
        Log.i(TAG, "request " + region + " with " + Arrays.toString(labels) + " by " + mPid);
        String[] domains = getDomainByLabel(region, labels);
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < labels.length; i++) {
            if (DEBUG)
                Log.d(TAG, "[" + labels[i] + "=" + domains[i] + "]");
            map.put(labels[i], domains[i]);
        }
        return map;
    }

    /**
     * 获取地区域名，需要在子线程中运行
     * @param region
     * @param labels
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Map<String, String> blockingGetRegionDomain(final String region, final String... labels)
            throws IOException, InterruptedException {
        DomainEngine engine = new DomainEngine() {
            @Override
            public void performRequest(Callback callback) {
                getDomainByRegion(region, callback, labels);
            }
        };
        try {
            return blockingGetDomain(engine);
        }catch (IllegalStateException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * 获取分组域名，需要在子线程中运行
     * @param imei
     * @param labels
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Map<String, String> blockingGetGroupDomain(final String imei, final String... labels)
            throws IOException, InterruptedException {
        DomainEngine engine = new DomainEngine() {
            @Override
            public void performRequest(Callback callback) {
                getDomainByGroup(imei, callback, labels);
            }
        };
        try {
            return blockingGetDomain(engine);
        }catch (IllegalStateException e) {
            return blockingGetRegionDomain("cn", labels);
        }
    }

    /**
     * 通过uid获取域名，需要在子线程中运行
     * @param uid
     * @param labels
     * @return
     * @throws IOException 网络异常
     * @throws InterruptedException
     * @throws IllegalStateException 该uid没有注册地信息
     */
    public Map<String, String> blockingGetUidDomain(final String uid, final String... labels)
            throws IOException, InterruptedException, IllegalStateException {
        DomainEngine engine = new DomainEngine() {
            @Override
            public void performRequest(Callback callback) {
                getDomainByUID(uid, callback, labels);
            }
        };
        return blockingGetDomain(engine);
    }

    /**
     * 通过imei获取设备绑定的账号注册地的域名，需要在子线程中运行
     * @param imei
     * @param labels
     * @return
     * @throws IOException 网络异常
     * @throws InterruptedException
     * @throws IllegalStateException 设备没有绑定任何账号
     */
    public Map<String, String> blockingGetDeviceDomain(final String imei, final String... labels)
            throws IOException, InterruptedException, IllegalStateException {
        DomainEngine engine = new DomainEngine() {
            @Override
            public void performRequest(Callback callback) {
                getDomainByDevice(imei, callback, labels);
            }
        };
        return blockingGetDomain(engine);
    }

    private Map<String, String> blockingGetDomain(DomainEngine engine)
            throws IOException, InterruptedException, IllegalStateException {
        warnInUiThread();
        final CountDownLatch latch = new CountDownLatch(1);
        final DomainResult r = new DomainResult();
        engine.performRequest(new CallbackWrapper() {
            @Override
            public void onSuccess(Map<String, String> result) {
                r.map = result;
                latch.countDown();
            }

            @Override
            public void onFailure(int code, Exception exception) {
                super.onFailure(code, exception);
                r.errorCode = code;
                r.exception = exception;
                latch.countDown();
            }
        });
        latch.await();
        if (r.hasException()) {
            if (r.errorCode >= 2000 && r.errorCode < 3000) {
                throw new IOException(r.exception.getMessage());
            } else {
                throw new IllegalStateException(r.exception.getMessage());
            }
        }
        return r.map;
    }

    interface DomainEngine {
        void performRequest(Callback callback);
    }

    class DomainResult {
        Map<String, String> map;
        int errorCode;
        Exception exception;

        boolean hasException() {
            return exception != null;
        }
    }

    /**
     * 根据region地区，获取label对应的域名
     *
     * @param region 地区，传null则获取当前系统设置地区
     * @param labels 域名标识
     */
    public void getDomainByRegion(String region, Callback callback, String... labels) {
        if (TextUtils.isEmpty(region)) {
            region = getCurrentRegion();
        }
        if (DEBUG) {
            Log.d(TAG, "request domain by region, region [" + region + "]");
        }
        connectDomainServerIfNeed(DomainContract.METHOD_REGION, region.toLowerCase(), callback, labels);
    }

    /**
     * 根据imei分组，获取label对应的域名
     *
     * @param imei   手机imei，不能为null
     * @param labels 域名标识
     */
    public void getDomainByGroup(String imei, Callback callback, String... labels) {
        if (TextUtils.isEmpty(imei)) {
            throw new IllegalArgumentException("imei must not be null or empty.");
        }
        if (imei.length() != 15) {
            throw new IllegalArgumentException(imei + " isn't valid, please checkout again.");
        }
        if (DEBUG) {
            Log.d(TAG, "request domain by group, imei [" + imei + "]");
        }
        connectDomainServerIfNeed(DomainContract.METHOD_GROUP, imei, callback, labels);
    }

    /**
     * 根据uid获取域名
     * @param uid
     * @param callback
     * @param labels
     */
    public void getDomainByUID(String uid, Callback callback, String... labels) {
        if (TextUtils.isEmpty(uid)) {
            throw new IllegalArgumentException("uid must not be null or empty");
        }
        if (DEBUG) {
            Log.d(TAG, "request domain by uid, uid [" + uid + "]");
        }
        connectDomainServerIfNeed(DomainContract.METHOD_UID, uid, callback, labels);
    }

    /**
     * 根据imei获取设备绑定的账号的注册地域名
     * @param imei
     * @param callback
     * @param labels
     */
    public void getDomainByDevice(String imei, Callback callback, String... labels) {
        if (TextUtils.isEmpty(imei)) {
            throw new IllegalArgumentException("imei must not be null or empty.");
        }
        if (imei.length() != 15) {
            throw new IllegalArgumentException(imei + " isn't valid, please checkout again.");
        }
        if (DEBUG) {
            Log.d(TAG, "request domain by device, imei [" + imei + "]");
        }
        connectDomainServerIfNeed(DomainContract.METHOD_DEVICE, "--" + imei, callback, labels);
    }

    private void connectDomainServerIfNeed(String method, String marker, Callback callback, String... labels) {
        QueryParam param = new QueryParam(method, marker, callback, labels);
        mInnerHandler.sendQueryParam(param);
    }

    private String[] getLabels(String region) {
        Cursor cursor = mResolver.query(DomainContract.RegionDomain.CONTENT_URI_VIEW, new String[]{DomainContract.Labels.LABEL},
                DomainContract.Regions.REGION + " = ?", new String[]{region.toLowerCase()}, null);
        List<String> labels = new ArrayList<>();
        while (cursor.moveToNext()) {
            labels.add(cursor.getString(cursor.getColumnIndex(DomainContract.Labels.LABEL)));
        }
        cursor.close();
        return labels.toArray(new String[labels.size()]);
    }

    private String[] getDomainByLabel(String region, String[] labels) {
        Cursor cursor = mResolver.query(DomainContract.RegionDomain.CONTENT_URI_VIEW,
                new String[]{DomainContract.Labels.LABEL, DomainContract.Domains.DOMAIN},
                DomainContract.Regions.REGION + " = ?", new String[]{region.toLowerCase()}, null);
        if (cursor == null) {
            Log.e(TAG, "Cursor is null when getDomainByLabel.");
            return new String[labels.length];
        }
        Map<String, String> map = new HashMap<>();
        try {
            while (cursor.moveToNext()) {
                String label = cursor.getString(cursor.getColumnIndex(DomainContract.Labels.LABEL));
                String domain = cursor.getString(cursor.getColumnIndex(DomainContract.Domains.DOMAIN));
                map.put(label, domain);
            }
        } finally {
            cursor.close();
        }
        String[] domains = new String[labels.length];
        for (int i = 0; i < labels.length; i++) {
            domains[i] = map.get(labels[i]);
        }
        return domains;
    }

    public String getCurrentRegion() {
        String regionValue = Settings.Secure.getString(mResolver, LEUI_COUNTRY_AREA_REGION_SETTINGS);
//        如果获取regionValue为null，可以通过如下方式获取默认值：
        if (TextUtils.isEmpty(regionValue)) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName("com.letv.leui.util.LeSystemProperties");
                Method method = clazz.getMethod("getDefaultCountryCode");
                regionValue = (String) method.invoke(null);
            } catch (Exception e) {
                Log.e(TAG, "failure get current region", e);
                throw new IllegalArgumentException("failure get current region");
            }
        }
        return regionValue;
    }

    private static void warnInUiThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalArgumentException("You must call this method on a background thread");
        }
    }

    private class QueryParam {

        String mMethod;
        String mMarker;
        Callback mCallback;
        List<String> mLabels;
        private String[] labels;

        public QueryParam(String method, String marker, Callback callback, String... labels) {
            mMethod = method;
            mMarker = marker;
            mCallback = callback;
            this.labels = labels;
            Arrays.sort(labels);
            mLabels = Arrays.asList(labels);
        }

        public int createToken() {
            return mMarker.hashCode();
        }

        @Override
        public String toString() {
            return "marker [" + mMarker + "] labels " + Arrays.toString(labels);
        }

    }

    public interface Callback {

        int ERROR_CODE_NULL_CURSOR = -1;
        int ERROR_CODE_NO_DOMAIN = 1000;
        int ERROR_CODE_NO_GROUP = 1001;
        int ERROR_CODE_NO_REGISTRY = 1002;
        int ERROR_CODE_NO_BINDING = 1003;

        void onSuccess(Map<String, String> result);

        void onFailure(Exception exception);
    }

    public abstract static class CallbackWrapper implements Callback {

        @Override
        public void onSuccess(Map<String, String> result) {

        }

        public void onFailure(Exception ex) {

        }

        public void onFailure(int code, Exception exception) {
            onFailure(exception);
        }
    }

    private static class InnerHandler extends Handler {

        private static final int REQUEST = 100;
        private final WeakReference<ContentResolver> mResolverWeakReference;
        private final Map<Integer, List<QueryParam>> mTokenToQueryParams;

        public InnerHandler(ContentResolver resolver) {
            super(Looper.getMainLooper());
            mResolverWeakReference = new WeakReference<>(resolver);
            mTokenToQueryParams = new HashMap<>();
        }

        public void sendQueryParam(QueryParam param) {
            Message message = obtainMessage(REQUEST);
            message.what = REQUEST;
            message.obj = param;
            message.sendToTarget();
        }

        public void sendSuccessResult(int token, Map<String, String> labelToDomains) {
            List<QueryParam> params = mTokenToQueryParams.remove(token);
            for (QueryParam p : params) {
                getDomainFromCache(labelToDomains, p);
            }
        }

        public void sendFailureResult(int token, int errorCode, String errorMsg) {
            List<QueryParam> params = mTokenToQueryParams.remove(token);
            for (QueryParam p : params) {
                LeDomainManager.Callback callback = p.mCallback;
                if (callback instanceof CallbackWrapper) {
                    ((CallbackWrapper) callback).onFailure(errorCode, new IllegalStateException(errorMsg));
                } else {
                    p.mCallback.onFailure(new IllegalStateException(errorMsg));
                }
            }
        }

        private void getDomainFromCache(Map<String, String> labelToDomains, QueryParam p) {
            if (p.mLabels.size() == 0) {
                Log.d(TAG, String.format("query %s, result is empty.", p.toString()));
                p.mCallback.onSuccess(labelToDomains);
            } else {
                HashMap<String, String> map = new HashMap<>();
                for (String label : p.mLabels) {
                    if (labelToDomains.containsKey(label)) {
                        map.put(label, labelToDomains.get(label));
                    } else {
                        map.put(label, null);
                    }
                }
                Log.d(TAG, String.format("query %s, result is %s.", p.toString(), map.toString()));
                p.mCallback.onSuccess(map);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            ContentResolver resolver = mResolverWeakReference.get();
            if (resolver == null) {
                Log.i(TAG, "content resolver is null");
                return;
            }
            int token;
            switch (msg.what) {
                case REQUEST: {
                    QueryParam param = (QueryParam) msg.obj;
                    if (DEBUG)
                        Log.d(TAG, "handle " + param.toString());
                    token = param.createToken();
                    if (mTokenToQueryParams.containsKey(token)) {
                        if (DEBUG)
                            Log.d(TAG, param.mMarker + " is already in flight");
                        List<QueryParam> params = mTokenToQueryParams.get(token);
                        params.add(param);
                        break;
                    } else {
                        if (DEBUG)
                            Log.d(TAG, "connect to domain and valid " + param.mMarker + ", the token is " + token);
                        ArrayList<QueryParam> params = new ArrayList<>();
                        params.add(param);
                        mTokenToQueryParams.put(token, params);
                    }

                    Messenger messenger = new Messenger(this);
                    Bundle bundle = new Bundle(3);
                    bundle.putInt(DomainContract.BUNDLE_PID, android.os.Process.myPid());
                    bundle.putInt(DomainContract.BUNDLE_TOKEN, token);
                    bundle.putParcelable(DomainContract.BUNDLE_MESSENGER, messenger);

                    // use call() pass Messenger to content provider, wait callback.
                    // if fail acquire client, send error message.
                    // in order to avoid Unknown URI exception, use ContentProviderClient instead of ContentResolver.
                    ContentProviderClient client = resolver.acquireContentProviderClient(DomainContract.BASE_CONTENT_URI);
                    Bundle call;
                    if (client != null) {
                        try {
                            call = client.call(param.mMethod, param.mMarker, bundle);
                            if (call != null) {
                                String version = call.getString(DomainContract.BUNDLE_VERSION);
                                if (version != null) {
                                    Log.i(TAG, "connected to domain server, version is " + version);
                                } else {
                                    Log.i(TAG, "success connected domain server");
                                }
                            }
                            break;
                        } catch (RemoteException e) {
                            Log.w(TAG, "Remote exception when invoke call method.", e);
                        } finally {
                            client.release();
                        }
                    }

                    Message message = obtainMessage(DomainContract.FAILURE);
                    message.arg1 = token;
                    bundle = new Bundle(2);
                    bundle.putString(DomainContract.BUNDLE_ERROR_MSG, "failure connected domain server");
                    message.obj = bundle;
                    message.sendToTarget();
                    break;
                }
                case DomainContract.FAILURE: {
                    token = msg.arg1;
                    String errorMsg = ((Bundle) msg.obj).getString(DomainContract.BUNDLE_ERROR_MSG);
                    int errorCode = ((Bundle) msg.obj).getInt(DomainContract.BUNDLE_ERROR_CODE);
                    if (DEBUG)
                        Log.d(TAG, "get failure result from domain server, the token is " + token + ", error is " + errorMsg);
                    sendFailureResult(token, errorCode, errorMsg);
                    break;
                }
                case DomainContract.READY: {
                    token = msg.arg1;
                    String marker = ((Bundle) msg.obj).getString(DomainContract.BUNDLE_MARKER);
                    if (DEBUG)
                        Log.d(TAG, "get success result from domain server, the token is " + token);
                    QueryHandler queryHandler = new QueryHandler(resolver, this, marker);
                    queryHandler.onDomainReady(token);
                    break;
                }
            }

        }
    }

    private static class QueryHandler extends AsyncQueryHandler {

        private final InnerHandler mTarget;
        private final String mMarker;

        public QueryHandler(ContentResolver resolver, InnerHandler target, String marker) {
            super(resolver);
            mTarget = target;
            mMarker = marker;
        }

        public void onDomainReady(int token) {
            Log.d(TAG, "start query region domain view, marker is " + mMarker);
            startQuery(token, null, DomainContract.RegionDomain.CONTENT_URI_VIEW,
                    new String[]{DomainContract.Labels.LABEL, DomainContract.Domains.DOMAIN},
                    DomainContract.Regions.REGION + " = ?", new String[]{mMarker}, null);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor == null) {
                Log.e(TAG, "cursor is null when query region domain view");
                mTarget.sendFailureResult(token, CallbackWrapper.ERROR_CODE_NULL_CURSOR, null);
                return;
            }
            try {
                HashMap<String, String> map = new HashMap<>();
                while (cursor.moveToNext()) {
                    String label = cursor.getString(0);
                    String domain = cursor.getString(1);
                    map.put(label, domain);
                }
                if (false)
                    Log.d(TAG, "finish query [" + mMarker + "] result is " + map.toString());
                mTarget.sendSuccessResult(token, map);
            } finally {
                cursor.close();
            }
        }
    }

}

class DomainContract {

    public static final int READY = 0;
    public static final int FAILURE = -1;

    public static final String BUNDLE_PID = "pid";
    public static final String BUNDLE_TOKEN = "token";
    public static final String BUNDLE_MESSENGER = "messenger";
    public static final String BUNDLE_ERROR_MSG = "errorMsg";
    public static final String BUNDLE_MARKER = "marker";
    public static final String BUNDLE_ERROR_CODE = "errorCode";
    public static final String BUNDLE_VERSION = "version";

    public static final String METHOD_TRIGGER = "trigger";
    public static final String METHOD_REGION = "region";
    public static final String METHOD_GROUP = "group";
    public static final String METHOD_UID = "uid";
    public static final String METHOD_DEVICE = "device";

    public static final int ERROR_CODE_NO_DOMAIN = 1000;
    public static final int ERROR_CODE_NO_GROUP = 1001;
    public static final int ERROR_CODE_NO_REGISTRY = 1002;
    public static final int ERROR_CODE_NO_BINDING = 1003;

    public static final String AUTHORITY = "com.letv.domain.domainprovider";

    public static final Uri BASE_CONTENT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .build();

    private interface LabelsColumns extends BaseColumns {

        String LABEL = "label";
    }

    public static class Labels implements LabelsColumns{

        public static final String TABLE = "labels";
        public static final Uri CONTENT_URI
                = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE);
    }

    private interface RegionsColumns extends BaseColumns {

        String REGION = "region";

        String STATE = "state";

        String TYPE = "type";

        String LAST_SYNC_STAMP = "last_sync_stamp";

        String SYNC_PRIORITY = "sync_priority";
    }

    public static class Regions implements RegionsColumns {

        public static final String TABLE = "regions";
        public static final Uri CONTENT_URI
                = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE);

        public static final int STATE_INVALID = 0;
        public static final int STATE_DEFAULT = 1;
        public static final int STATE_OUT_OF_DATE = 2;
        public static final int STATE_UP_TO_DATE = 3;
    }

    private interface DomainsColumns extends BaseColumns {

        String DOMAIN = "domain";

        String LABEL_ID = "label_id";
    }

    public static class Domains implements DomainsColumns {

        public static final String TABLE = "domains";
        public static final Uri CONTENT_URI
                = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE);
    }

    private interface RegionDomainColumns extends BaseColumns {

        String REGION_ID = "region_id";

        String DOMAIN_ID = "domain_id";
    }

    public static class RegionDomain implements RegionDomainColumns {

        public static final String TABLE = "region_domain";
        public static final String VIEW = "view_region_domain";
        public static final Uri CONTENT_URI
                = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE);
        public static final Uri CONTENT_URI_VIEW
                = Uri.withAppendedPath(BASE_CONTENT_URI, VIEW);
    }

}
