package com.viatom.socketapp

class SocketUtils {

    companion object{

        /***
        是否是外国环境
         *****/
        val isForeign = false
        var SOCKET_URL = ""

    }


}


fun initGlobalParams() {


    var SOCKET_URL_FOREIGN = "http://socket.viatomtech.com.cn/"

    var SOCKET_URL_DOMESTIC ="http://sockettest.viatomtech.com.cn/"

    if (SocketUtils.isForeign) {
        SocketUtils.SOCKET_URL = SOCKET_URL_FOREIGN
    } else {
        SocketUtils.SOCKET_URL = SOCKET_URL_DOMESTIC
    }
}