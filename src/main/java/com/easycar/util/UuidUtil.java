package com.easycar.util;

import java.util.UUID;

public class UuidUtil
{

    public static String get32UUID()
    {
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        System.out.println(uuid);
        return uuid;
    }

    public static String getUuidOfStr(String str)
    {
        return UUID.fromString(str).toString();
    }

    public static void main(String[] args)
    {
        System.out.println(get32UUID());
        //System.out.println(getUuidOfStr("asd"));
//        System.out.println(UUID.fromString("123"));
    }
}

