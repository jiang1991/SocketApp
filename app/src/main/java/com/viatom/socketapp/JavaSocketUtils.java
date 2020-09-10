package com.viatom.socketapp;

public class JavaSocketUtils {

    /***

     var SOCKET_URL_FOREIGN = "http://socket.viatomtech.com.cn/"

     var SOCKET_URL_DOMESTIC ="http://sockettest.viatomtech.com.cn/"

     *****/

    private static String SOCKET_URL_FOREIGN = "http://socket.viatomtech.com.cn/";
    private static String SOCKET_URL_DOMESTIC ="http://sockettest.viatomtech.com.cn/";

    public static String SOCKET_URL = "http://sockettest.viatomtech.com.cn/";

    /***
    是否是国外
    *****/
    public static boolean isForeign=false;

    public static void initParams(){
        if(isForeign){
            SOCKET_URL = SOCKET_URL_FOREIGN;
        }else{
            SOCKET_URL =  SOCKET_URL_DOMESTIC ;
        }
    }

}
