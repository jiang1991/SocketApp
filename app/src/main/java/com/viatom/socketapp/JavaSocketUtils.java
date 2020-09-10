package com.viatom.socketapp;

import android.os.Build;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

public class JavaSocketUtils {

    /***

     var SOCKET_URL_FOREIGN = "http://socket.viatomtech.com.cn/"

     var SOCKET_URL_DOMESTIC ="http://sockettest.viatomtech.com.cn/"

     *****/

    private static String SOCKET_URL_FOREIGN = "http://socket.viatomtech.com.cn/";
    private static String SOCKET_URL_DOMESTIC ="http://sockettest.viatomtech.com.cn/";

    public static String SOCKET_URL = "";

    private static boolean isForeign = false;
    public static void initParams(){

        if(isForeign ){
            SOCKET_URL = SOCKET_URL_FOREIGN;
        }else{
            SOCKET_URL =  SOCKET_URL_DOMESTIC ;
        }
        LogUtils.i("SOCKET_URL->"+SOCKET_URL);

    }

}
