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


    public static void initParams(){
        boolean isForeign = Utils.getApp().getApplicationInfo().metaData.getBoolean("isForeign") ;
        LogUtils.i("isForeign->"+isForeign);
        if(isForeign){
            SOCKET_URL = SOCKET_URL_FOREIGN;
        }else{
            SOCKET_URL =  SOCKET_URL_DOMESTIC ;
        }


    }

}
