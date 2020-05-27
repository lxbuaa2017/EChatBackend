package com.example.echatbackend.util;

import java.io.UnsupportedEncodingException;

public class EncodeUtil {
    public static String getEncoding(String str){
        String[] encodes = {"ISO-8859-1","UTF-8","UTF-16","ASCII","GB2312"};
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
        String encode = getEncoding(str);
        switch (encode){
            case "ISO-8859-1":
                String utf8 = new String (str.getBytes ( "ISO8859-1" ), "utf-8" );
                return utf8;
            case  "UTF-8":
                return str;
            default:
                return str;
        }
    }
}
