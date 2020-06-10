package com.example.echatbackend.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class EncodeUtil {
    public static String getEncoding(String str){
        String[] encodes = {"Hex","ISO-8859-1","UTF-8","UTF-16","ASCII","GB2312"};
        for(String encode:encodes){
            try
            {
                if(str.equals(new String(str.getBytes(), encode)))
                    return encode;
            }
            catch(Exception ignored) {}
        }
        return null;
    }
    public static String toUTF8(String str) throws UnsupportedEncodingException {
        return new String (str.toString().getBytes ( "ISO8859-1" ), "UTF-8" );
//        String encode = getEncoding(str);
//        switch (encode){
//            case "ISO-8859-1":
//                String utf8 = new String (str.getBytes ( "ISO8859-1" ), "utf-8" );
//                return utf8;
//            case  "UTF-8":
//                return str;
//            default:
//                return str;
//        }
    }

    public static String str2Hex(String str) throws UnsupportedEncodingException {
        String hexRaw = String.format("%x", new BigInteger(1, str.getBytes("UTF-8")));
        char[] hexRawArr = hexRaw.toCharArray();
        StringBuilder hexFmtStr = new StringBuilder();
        final String SEP = "\\x";
        for (int i = 0; i < hexRawArr.length; i++) {
            hexFmtStr.append(SEP).append(hexRawArr[i]).append(hexRawArr[++i]);
        }
        return hexFmtStr.toString();
    }

    public static String hex2Str(String str) throws UnsupportedEncodingException {
        String strArr[] = str.split("\\\\"); // 分割拿到形如 xE9 的16进制数据
        byte[] byteArr = new byte[strArr.length - 1];
        for (int i = 1; i < strArr.length; i++) {
            Integer hexInt = Integer.decode("0" + strArr[i]);
            byteArr[i - 1] = hexInt.byteValue();
        }

        return new String(byteArr, "UTF-8");
    }
}
