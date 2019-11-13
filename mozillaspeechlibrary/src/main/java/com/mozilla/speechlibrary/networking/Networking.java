package com.mozilla.speechlibrary.networking;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.mozilla.speechlibrary.MozillaSpeechService;

import java.io.ByteArrayOutputStream;

public abstract class Networking {

    String STT_ENDPOINT = "http://100.81.194.147:8080/";

    MozillaSpeechService mSpeechService;
    boolean cancelled;
    protected Context mContext;
    Handler mMainThreadHandler;

    Networking(Context aContext, MozillaSpeechService aSpeechService) {
        mContext = aContext;
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mSpeechService = aSpeechService;
    }

    public abstract void doSTT(final ByteArrayOutputStream baos, NetworkSettings mNetworkSettings);

    public void cancel() {
        cancelled = true;
    }
}
