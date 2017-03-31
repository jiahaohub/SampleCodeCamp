package com.le.samplecodecamp.eui.update;

import android.text.TextUtils;

import com.eui.sdk.independent.util.ByteUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by spring on 16-8-11.
 * 遵循API安全规范１的规则
 */
public class INetEncryptImpl implements INetEncrypt {
    public static INetEncryptImpl mINetEncryptImpl = new INetEncryptImpl();
    //签名
    public static final String SK = "sk_xVU5x7uvHXse38f8axKN";
    public static final String AK = "ak_lMHsCi32Wgyaqg23g9YL";

    //SEPARATOR
    public static final String SEP = "&";

    //字符编码
    public static final String KEY_TIME = "_time";
    public static final String KEY_AK = "_ak";
    public static final String KEY_SIGN = "_sign";

    //字符编码
    public static final String REQUEST_CHARSET = "UTF-8";

    public static INetEncryptImpl getInstance() {
        return mINetEncryptImpl;
    }

    @Override
    public String encrypt(Map<String, String> params) {
        return getSignUrl(params);
    }

    /**
     * 对参数进行加密处理的步骤
     * 返回最终的url参数
     * @param params
     * @return
     */
    private String getSignUrl(Map<String, String> params) {
        //1. 把当前时间戳加入请求GET参数，字段名为_time
        long time = System.currentTimeMillis() / 1000;
        params.put(INetEncryptImpl.KEY_TIME, String.valueOf(time));

        //2. 把公钥加入请求GET参数，字段名为_ak
        params.put(INetEncryptImpl.KEY_AK, INetEncryptImpl.AK);

        //3. 把所有需要传递的参数的key按字母顺序进行排序(升序)，空的参数不参与校验
        SortedSet<String> paramsSort = sort(params);

        //4. 拼接字符串
        String paramsString = join(paramsSort, SEP);

        //5. 计算MD5值
        String MD5String = computeMD5(paramsString);

        //6. 生成的字符串加到_sign参数里
        String result = paramsString + SEP + KEY_SIGN + "=" + MD5String;

        return result;
    }

    /**
     * 计算MD5值
     *
     * @param paramsString
     * @return
     */
    private String computeMD5(String paramsString) {
        try {
            String str2Sign = paramsString + SK;
            MessageDigest digest;
            digest = MessageDigest.getInstance("MD5");
            digest.update(str2Sign.getBytes(REQUEST_CHARSET));
            return ByteUtil.toHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 把所有需要传递的参数的key按字母顺序进行排序(升序)，空的参数不参与校验
     *
     * @param params
     * @return
     */
    public SortedSet<String> sort(Map<String, String> params) {
        SortedSet<String> set = new TreeSet<String>();
        if (params != null && params.size() > 0) {
            for (String param : params.keySet()) {
                String value = params.get(param);
                if (!TextUtils.isEmpty(value)) {
                    try {
                        set.add(param + "=" + URLEncoder.encode(value, REQUEST_CHARSET));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return set;
    }

    /**
     * 拼接字符串
     *
     * @param strings
     * @param sep
     * @return
     */
    private String join(Iterable<String> strings, String sep) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : strings) {
            if (first)
                first = false;
            else
                sb.append(sep);
            sb.append(item);
        }
        return sb.toString();
    }
}