package com.viatom.socketapp;

public class JavaSocketUtils {

    public int socketConnectCount = 0;
    public int socketDisconnectCount = 0;

    public int socketTimeoutCount = 0;
    public int socketErrorCount = 0;

    public void addOneConnectCount() {
        socketConnectCount += 1;

    }

    public void addOneDisconnectCount() {
        socketDisconnectCount += 1;
    }

    public void addOneTimeoutCount() {
        socketTimeoutCount += 1;
    }

    public void addOneErrorCount() {
        socketErrorCount += 1;
    }

    public String getString() {
        return "socket connect->" + socketConnectCount + "  disconnect->" + socketDisconnectCount + " error->" + socketErrorCount + " "
                + "timeout->" + socketTimeoutCount;
    }


}
