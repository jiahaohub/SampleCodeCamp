package com.le.samplecodecamp.eui.update;

import java.util.Map;

/**
 * Created by spring on 16-8-11.
 * 签名算法接口
 */
public interface INetEncrypt {
    String encrypt(Map<String, String> params);
}
