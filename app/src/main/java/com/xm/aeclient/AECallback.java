package com.xm.aeclient;

public interface AECallback {
    /* Called when HTTP request succeeded */
    void onStateChanged(AEJobStatus a);
}
