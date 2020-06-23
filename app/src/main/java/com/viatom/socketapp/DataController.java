package com.viatom.socketapp;

public class DataController {

    public static int index = 0;
    // for wave -> data source
    public volatile static float[] dataSrc;
    // data received
    public volatile static float[] dataRec = new float[0];

    // add to dataSrc
    public static void feed(float[] fs) {
        if (fs == null || fs.length == 0) {
            fs = new float[5];
        }

        for (int i = 0; i<fs.length; i++) {
            int tempIndex = (index + i) % dataSrc.length;
            dataSrc[tempIndex] = fs[i];
        }

        index = (index + fs.length) % dataSrc.length;
    }

    public static void receive(float[] fs) {
        if (fs == null || fs.length == 0) {
            return;
        }

//        Log.d("FILTER: ", StringUtils.fs2str(fs));

        float[] temp = new float[dataRec.length + fs.length];
        System.arraycopy(dataRec, 0, temp, 0, dataRec.length);
        System.arraycopy(fs, 0, temp, dataRec.length, fs.length);

        dataRec = temp;

//        LogUtils.d("received: " + fs.length + " total pool: " + dataRec.length);

    }

    public static float[] draw(int n) {
        if (n == 0 || n > dataRec.length) {
            return null;
        }

        float[] res = new float[n];
        float[] temp = new float[dataRec.length - n];
        System.arraycopy(dataRec, 0, res, 0, n);
        System.arraycopy(dataRec, n, temp, 0, dataRec.length-n);
        dataRec = temp;

        return res;
    }
}
