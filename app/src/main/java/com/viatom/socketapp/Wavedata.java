package com.viatom.socketapp;

public class Wavedata {

    byte[] content;
    int spo2;
    int pr;
    int len;
    byte[] waveByte;
    float[] fs;

    public Wavedata() {

    }

    public Wavedata(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return;
        }

        this.content = bytes;
        this.spo2 = bytes[0] & 0xFF;
        this.pr = (bytes[1] & 0xFF) + ( (bytes[2] & 0xFF) << 8 );
        this.len = (bytes[10] & 0xFF) + ( (bytes[11] & 0xFF) << 8 );
        this.waveByte = new byte[len];
        System.arraycopy(bytes, 12, waveByte, 0, len);
        fs = new float[len];
        for (int i = 0; i<len; i++) {
            fs[i] = (float) (waveByte[i] & 0xFF) / 255;
        }
    }

    public byte[] getContent() {
        return content;
    }

    public int getSpo2() {
        return spo2;
    }

    public int getPr() {
        return pr;
    }

    public int getLen() {
        return len;
    }

    public byte[] getWaveByte() {
        return waveByte;
    }

    public float[] getFs() {
        return fs;
    }
}
