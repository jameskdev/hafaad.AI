package com.xm.aeclient;

import android.os.HandlerThread;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class AEViewModel extends ViewModel implements AECallback {
    private final MutableLiveData<AEJobStatus> aeStatus = new MutableLiveData<>(new AEJobStatus(0, false, 0, "", false));
    private final HandlerThread mGenThr;
    private final StabImageGenerator mGen;
    private int currentImgPos = -1;
    private String currentPrompt = "";
    public LiveData<AEJobStatus> getAEStatus() {
        return aeStatus;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mGen.clearCallback();
    }

    public AEViewModel() {
        mGenThr = new HandlerThread("mStabImageGeneratorThread");
        mGenThr.start();
        mGen = new StabImageGenerator(mGenThr.getLooper(), this);
    }

    @Override
    public void onStateChanged(AEJobStatus a) {
        aeStatus.postValue(a);
    }

    public boolean createGetRequest(String mCfgScale, String mClipGuidance,
                                 String mSampler, String mStylePreset, String mSamples,
                                 String mSteps, String mSeed, ArrayList<String> mPrompt,
                                 ArrayList<Integer> mWeight, String mApiKey) {
        return mGen.createGetImageRequest(mCfgScale, mClipGuidance, mSampler, mStylePreset,
                mSamples, mSteps, mSeed, mPrompt, mWeight, mApiKey);
    }

    public StabImageData getImage(int idx) {
        return mGen.getImage(idx);
    }

    public void clearImages() {
        mGen.clearImages();
    }

    public void ackResult() {
        mGen.ackResult();
    }

    public boolean saveImage(String fn, int idx) {
        return mGen.saveImage(fn, idx);
    }

    public int getCurrentImgPos() {
        return currentImgPos;
    }

    public void setCurrentImgPos(int inCurrentImgPos) {
        currentImgPos = inCurrentImgPos;
    }

    public void setCurrentPrompt(String inCurrentPrompt) {
        currentPrompt = inCurrentPrompt;
    }

    public String getCurrentPrompt() {
        return currentPrompt;
    }
}
