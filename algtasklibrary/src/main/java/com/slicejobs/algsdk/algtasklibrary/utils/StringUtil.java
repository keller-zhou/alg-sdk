package com.slicejobs.algsdk.algtasklibrary.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jgzhu on 5/7/14.
 */
public class StringUtil {

    public static boolean isNotBlank(String str) {
        if (TextUtils.isEmpty(str)) return false;
        return !TextUtils.isEmpty(str.trim());
    }

    public static boolean isBlank(String str) {
        return !StringUtil.isNotBlank(str);
    }

    public static boolean isMobilePhone(String strPhoneNumber) {
        if (strPhoneNumber.length() < 1) {
            return false;
        }
        boolean res = true;//手机号码格式验证，关闭，让服务器验证
//        if (PhoneNumberUtils.isGlobalPhoneNumber(strPhoneNumber)) {
//            try {
//                String MOBILE_PATTERN = "(^(13\\d|14[57]|15[^4,\\D]|17[678]|18\\d)\\d{8}|170[059]\\d{7})$";
//                Pattern p = Pattern.compile(MOBILE_PATTERN);
//                Matcher m = p.matcher(strPhoneNumber);
//                res = m.matches();
//            } catch (Exception e) {
//                res = false;
//            }
//        }
        return res;
    }

    private static String getString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(b[i]);
        }
        return sb.toString();
    }

    /**
     * 半角字符转全角字符
     * @param input
     * @return
     */
    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i< c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }if (c[i]> 65280&& c[i]< 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }


    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("^[0-9]*$");
        Matcher match=pattern.matcher(str);
        if(match.matches()==false){
            return false;
        }else{
            return true;
        }
    };




    //小数点金额格式验证
    public static boolean isHaveDecimal(String str){
        //Pattern pattern= Pattern.compile("^\\d+\\.\\d{1,9}?$");小数
        //Pattern pattern= Pattern.compile("^[0-9]+([.]{1}[0-9]+){0,1}$");//整数加小数
        Pattern pattern = Pattern.compile("(^\\d+\\.\\d{1,9}$)|(^\\d+$)");//浮点数
        Matcher match=pattern.matcher(str);
        if(match.matches()==false){
            return false;
        }else{
            return true;
        }
    }

    public static boolean isSamplePassword(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");//浮点数
        Matcher match=pattern.matcher(str);
        if(match.matches()==false){
            return false;
        }else{
            return true;
        }
    }

    /**
     * 提取字符串中的数字
     *
     * @param number
     * @return
     * @throws Exception
     */
    public static String numberIntercept(String number) throws Exception {

        return Pattern.compile("[^0-9]").matcher(number).replaceAll("");

    }

    /**
     * 过滤设置的特殊符号
     *
     * @param str
     * @return
     * @throws Exception
     */
    public static String filtration(String str) throws Exception {
        String regEx = "[`~!@#$%^&*()+=|{}:;\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        return Pattern.compile(regEx).matcher(str).replaceAll("").trim();
    }

    //是否为汉字
    public static boolean isContainEnglish(String str){

        Pattern p= Pattern.compile("[a-zA-z]");
        return p.matcher(str).find();
    }
}