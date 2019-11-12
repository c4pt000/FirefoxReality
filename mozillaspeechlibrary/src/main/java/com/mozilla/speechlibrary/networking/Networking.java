package com.mozilla.speechlibrary.networking;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.mozilla.speechlibrary.MozillaSpeechService;

import java.io.ByteArrayOutputStream;

public abstract class Networking {

    String STT_ENDPOINT = "https://speaktome-2.services.mozilla.com/";

    protected MozillaSpeechService mSpeechService;
    protected boolean cancelled;
    protected Context mContext;
    protected Handler mMainThreadHandler;

    public Networking(Context aContext, MozillaSpeechService aSpeechService) {
        mContext = aContext;
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mSpeechService = aSpeechService;
    }

    public abstract void doSTT(final ByteArrayOutputStream baos, NetworkSettings mNetworkSettings);

    public void cancel() {
        cancelled = true;
    }
}
