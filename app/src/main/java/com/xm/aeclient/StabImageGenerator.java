package com.xm.aeclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StabImageGenerator extends Handler {
    private final OkHttpClient cli = new OkHttpClient();
    private final ArrayList<StabImageData> imgList = new ArrayList<>();
    private final Object imgListInUse = new Object();
    private AECallback mAECallback;

    public StabImageGenerator(Looper looper, AECallback inAECallback) {
        super(looper);
        mAECallback = inAECallback;
    }

    public void clearCallback() {
        mAECallback = null;
    }

    private JSONObject getImageBlocking(int mCfgScale, String mClipGuidance,
                                   String mSampler, String mStylePreset, int mSamples, int mSteps,
                                   long mSeed, ArrayList<String> mPrompt,
                                   ArrayList<Integer> mWeight, String mApiKey) throws JSONException {
        String inUrl = "https://api.stability.ai/v1/generation/stable-diffusion-v1-6/text-to-image";
        JSONObject j = new JSONObject();
        j.put("cfg_scale", mCfgScale);
        j.put("clip_guidance_preset", mClipGuidance);
        if (!mSampler.equals("AUTO")) {
            j.put("sampler", mSampler);
        }
        if (!mStylePreset.equalsIgnoreCase("auto")) {
            j.put("style_preset", mStylePreset);
        }
        j.put("samples", mSamples);
        j.put("steps", mSteps);
        j.put("seed", mSeed);
        JSONArray a = new JSONArray();
        for (int i = 0; i < mPrompt.size(); i++) {
            a.put(new JSONObject().put("text", mPrompt.get(i)).put("weight",
                    mWeight.get(i)));
        }
        j.put("text_prompts", a);
        Request req = new Request.Builder().url(inUrl).post(RequestBody.create(j.toString(),
            MediaType.parse("application/json"))).
                addHeader("Authorization", mApiKey).
                addHeader("Accept", "application/json").build();
        JSONObject resp = new JSONObject();
        try (Response res = cli.newCall(req).execute()) {
            if (res.body() != null) {
                if (res.isSuccessful()) {
                    try {
                        resp.put("data", new JSONObject(res.body().string()));
                        resp.put("result", 0);
                    } catch (JSONException jex) {
                        resp.put("error_msg", "JSONException: " + jex.getMessage());
                        resp.put("result", -2);
                    }
                } else {
                    String errMsg = "";
                    try {
                        JSONObject errObj = new JSONObject(res.body().string());
                        errMsg = errObj.optString("message",
                                "Error response from server is malformed!");
                    } catch (JSONException nj) {
                        errMsg = "Error response from server is malformed!";
                    }
                    resp.put("result", -1);
                    resp.put("error_msg", "Response code(" + String.valueOf(res.code()) + ")\n" +
                            errMsg);
                }
            } else {
                resp.put("result", -1);
                resp.put("error_msg", "NULL response body received!");
            }
        } catch (IOException e) {
            resp.put("result", -2);
            resp.put("error_msg", "IOException: " + e.getMessage());
        }
        return resp;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (msg.what == 0) {
                if (msg.peekData() != null) {
                    processGetImage(msg.peekData().getInt("mCfgScale"),
                            msg.peekData().getString("mClipGuidance"),
                            msg.peekData().getString("mSampler"),
                            msg.peekData().getString("mStylePreset"),
                            msg.peekData().getInt("mSamples"),
                            msg.peekData().getInt("mSteps"),
                            msg.peekData().getLong("mSeed"),
                            msg.peekData().getStringArrayList("mPrompt"),
                            msg.peekData().getIntegerArrayList("mWeight"),
                            msg.peekData().getString("mApiKey"));
                }
        }
    }

    public boolean createGetImageRequest(String mCfgScale, String mClipGuidance,
                                      String mSampler, String mStylePreset, String mSamples,
                                      String mSteps, String mSeed, ArrayList<String> mPrompt,
                                      ArrayList<Integer> mWeight, String mApiKey) {
        if (mPrompt.size() != mWeight.size()) {
            return false;
        }
        if (mApiKey.isEmpty()) {
            return false;
        }
        Bundle b = new Bundle();
        b.putStringArrayList("mPrompt", mPrompt);
        b.putIntegerArrayList("mWeight", mWeight);
        b.putInt("mCfgScale", parseIntOrDefault(mCfgScale, 7));
        b.putString("mClipGuidance", mClipGuidance);
        b.putString("mSampler", mSampler);
        b.putString("mApiKey", mApiKey);
        b.putString("mStylePreset", mStylePreset);
        b.putInt("mSamples", parseIntOrDefault(mSamples, 1));
        b.putInt("mSteps", parseIntOrDefault(mSteps, 30));
        b.putLong("mSeed", parseLongOrDefault(mSeed, 0));
        Message m = this.obtainMessage(0);
        m.setData(b);
        m.sendToTarget();
        return true;
    }

    private void processGetImage(int mCfgScale, String mClipGuidance,
                                         String mSampler, String mStylePreset, int mSamples, int mSteps,
                                         long mSeed, ArrayList<String> mPrompt,
                                 ArrayList<Integer> mWeight, String mApiKey) {
        boolean imgChanged = false;
        mAECallback.onStateChanged(new AEJobStatus(imgList.size(), true, 0, "Requesting", imgChanged));
        try {
            JSONObject j = getImageBlocking(mCfgScale, mClipGuidance, mSampler, mStylePreset,
                    mSamples, mSteps, mSeed, mPrompt, mWeight, mApiKey);
            int errno = j.getInt("result");
            if (errno == 0) {
                JSONArray ja = j.getJSONObject("data").getJSONArray("artifacts");
                synchronized(imgListInUse) {
                    imgList.clear();
                    imgChanged = true;
                    mAECallback.onStateChanged(new AEJobStatus(imgList.size(), true, 0, "Parsing", imgChanged));
                    for (int i = 0; i < ja.length(); i++) {
                        imgList.add(new StabImageData(base64ToByteArray(ja.getJSONObject(i).getString("base64")),
                                ja.getJSONObject(i).getLong("seed")));
                    }
                }
                mAECallback.onStateChanged(new AEJobStatus(imgList.size(), false, 0, "", imgChanged));
            } else {
                mAECallback.onStateChanged(new AEJobStatus(imgList.size(), false, errno,
                        j.optString("error_msg", "Unknown Error"), imgChanged));
            }
        } catch (JSONException jex) {
            mAECallback.onStateChanged(new AEJobStatus(imgList.size(), false, -2, "JSONException", imgChanged));
        }
    }

    public StabImageData getImage(int idx) {
        synchronized(imgListInUse) {
            return imgList.get(idx);
        }
    }

    public long[] getAllSeeds() {
        synchronized(imgListInUse) {
            if (imgList.size() == 0) {
                return null;
            }
            long[] mSeeds = new long[imgList.size()];
            for (int i = 0; i < imgList.size(); i++) {
                mSeeds[i] = imgList.get(i).getSeed();
            }
            return mSeeds;
        }
    }

    public void clearImages() {
        synchronized(imgListInUse) {
            imgList.clear();
            mAECallback.onStateChanged(new AEJobStatus(imgList.size(), false, 0, "", true));
        }
    }

    public static int parseIntOrDefault(String valToConv, int dVal) {
        try {
            return Integer.parseInt(valToConv);
        } catch (NumberFormatException n) {
            return dVal;
        }
    }

    public static long parseLongOrDefault(String valToConv, long dVal) {
        try {
            return Long.parseLong(valToConv);
        } catch (NumberFormatException n) {
            return dVal;
        }
    }

    public void ackResult() {
        mAECallback.onStateChanged(new AEJobStatus(imgList.size(), false, 0, "", false));
    }

    public static byte[] base64ToByteArray(String b64input) throws IllegalArgumentException {
        return Base64.getDecoder().decode(b64input);
    }

    public boolean saveImage(String inPath, int idx) {
        synchronized(imgListInUse) {
            File f = new File(inPath);
            try (FileOutputStream fos = new FileOutputStream(f); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                fos.write(imgList.get(idx).getImageData());
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

}
