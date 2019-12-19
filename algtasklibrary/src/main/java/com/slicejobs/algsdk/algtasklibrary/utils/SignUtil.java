package com.slicejobs.algsdk.algtasklibrary.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nlmartian on 7/25/15.
 */
public class SignUtil {

    public static String md5Params(Map<String, String> paramsMap) {
        List<Map.Entry<String, String>> entryList = new ArrayList<>();
        for (Map.Entry<String, String> keyValuePair : paramsMap.entrySet()) {
            entryList.add(keyValuePair);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : entryList) {
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            sb.append("&");
        }
        sb.delete(sb.length() - 1, sb.length());
        return md5(sb.toString());
    }

    public static String signParams(Map<String, String> paramsMap) {
        List<Map.Entry<String, String>> entryList = new ArrayList<>();
        for (Map.Entry<String, String> keyValuePair : paramsMap.entrySet()) {
            entryList.add(keyValuePair);
        }
        Collections.sort(entryList, (lhs, rhs) -> lhs.getKey().compareToIgnoreCase(rhs.getKey()));
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : entryList) {
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            sb.append("||");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(";alg@201507");
        return md5(sb.toString());
    }

    public static String md5(String value) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(value.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static class SignBuilder {
        private Map<String, String> params = null;

        public SignBuilder() {
            params = new HashMap<>();
        }

        public SignBuilder put(String key, String value) {
            params.put(key, value);
            return this;
        }

        public String build() {
            return SignUtil.signParams(params);
        }

        public String md5Params() {
            return SignUtil.md5Params(params);
        }
    }
}
