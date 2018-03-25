package com.easycar.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5
{

    public static String md5(String str)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++)
            {
                i = b[offset];
                if (i < 0)
                {
                    i += 256;
                }
                if (i < 16)
                {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            str = buf.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return str;
    }

    /**
     * @param source   需要加密的字符串
     * @param hashType 加密类型 （MD5 和 SHA）
     * @return
     */
    public static String getHash(String source, String hashType)
    {
        StringBuilder sb = new StringBuilder();
        MessageDigest md5;
        try
        {
            md5 = MessageDigest.getInstance(hashType);

            md5.update(source.getBytes("utf-8"));
            for (byte b : md5.digest())
            {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(String source)
    {
        try
        {
            //确定计算方法
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(source.getBytes("utf-8"));

            return DataFormater.byte2hex(md5.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args)
    {
        System.out.println(md5("31119@qq.com" + "123456"));
        System.out.println(md5("mj1"));
    }
}
