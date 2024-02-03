package com.xm.aeclient;

public class AEJobStatus {
    private final int numOfImages;
    private final boolean currentlyAtWork;
    private final int isError;
    private final String mMessage;
    private final boolean imgChanged;

    public AEJobStatus(int numOfImages, boolean currentlyAtWork, int isError,
                       String inMessage, boolean inImgChanged) {
        this.numOfImages = numOfImages;

        this.currentlyAtWork = currentlyAtWork;
        this.isError = isError;
        mMessage = inMessage;
        imgChanged = inImgChanged;
    }

    public boolean isCurrentlyAtWork() {
        return currentlyAtWork;
    }

    public int getNumOfImages() {
        return numOfImages;
    }

    public int getIsError() {
        return isError;
    }

    public boolean isImgChanged() {
        return imgChanged;
    }

    public String getResMessage() {
        return mMessage;
    }
}
