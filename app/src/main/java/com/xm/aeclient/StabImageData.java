package com.xm.aeclient;

public class StabImageData
{
    private final byte[] imageData;
    private final long seed;

    public StabImageData(byte[] inImageData, long inSeed) {
        imageData = inImageData;
        seed = inSeed;
    }
    public byte[] getImageData() {
        return imageData;
    }
    public long getSeed() {
        return seed;
    }
}
