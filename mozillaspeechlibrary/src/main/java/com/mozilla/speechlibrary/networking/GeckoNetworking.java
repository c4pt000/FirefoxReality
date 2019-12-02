package com.mozilla.speechlibrary.networking;

import android.content.Context;

import com.mozilla.speechlibrary.MozillaSpeechService;
import com.mozilla.speechlibrary.STTResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.gecko.mozglue.DirectBufferAllocator;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoWebExecutor;
import org.mozilla.geckoview.WebRequest;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class GeckoNetworking extends Networking {

    private GeckoWebExecutor mExecutor;

    public GeckoNetworking(Context aContext, MozillaSpeechService aSpeechService, GeckoRuntime runtime) {
        super(aContext, aSpeechService);

        mExecutor = new GeckoWebExecutor(runtime);
    }

    @Override
    public void doSTT(final ByteArrayOutputStream baos, NetworkSettings mNetworkSettings) {

        if (cancelled) {
            mMainThreadHandler.post(() -> {
                mSpeechService.notifyListeners(MozillaSpeechService.SpeechState.CANCELED, null);
            });
        }

        mMainThreadHandler.post(() -> {
            byte[] bytes = baos.toByteArray();
            ByteBuffer buffer = DirectBufferAllocator.allocate(bytes.length);
            WebRequest request = new WebRequest.Builder(STT_ENDPOINT)
                    .method("POST")
                    .uri(STT_ENDPOINT)
                    .cacheMode(WebRequest.CACHE_MODE_NO_CACHE)
                    .header("Accept-Language-STT", mNetworkSettings.mLanguage)
                    .header("Store-Transcription", mNetworkSettings.mStoreTranscriptions ? "1": "0" )
                    .header("Store-Sample", mNetworkSettings.mStoreSamples ? "1": "0")
                    .header("Product-Tag", mNetworkSettings.mProductTag)
                    .header("Content-Type", "audio/3gpp")
                    .body(buffer.put(bytes))
                    .build();

            mSpeechService.notifyListeners(MozillaSpeechService.SpeechState.DECODING, null);
            mExecutor.fetch(request).then(webResponse -> {
                String body;
                if (webResponse != null) {
                    if (webResponse.body != null) {
                        int size = webResponse.body.available();
                        byte[] responseBuffer = new byte[size];
                        webResponse.body.read(responseBuffer);
                        webResponse.body.close();
                        body = new String(responseBuffer, StandardCharsets.UTF_8);

                        if (webResponse.statusCode == 200) {
                            JSONObject reader = new JSONObject(body);
                            JSONArray results = reader.getJSONArray("data");
                            final String transcription = results.getJSONObject(0).getString("text");
                            final String confidence = results.getJSONObject(0).getString("confidence");
                            STTResult sttResult = new STTResult(transcription, Float.parseFloat(confidence));
                            mSpeechService.notifyListeners(MozillaSpeechService.SpeechState.STT_RESULT, sttResult);

                        } else {
                            // called when response HTTP status is not "200"
                            String error = String.format("Request Error %s: %s", webResponse.statusCode, body);
                            mSpeechService.notifyListeners(MozillaSpeechService.SpeechState.ERROR, error);
                        }

                    } else {
                        // WebResponse body is null
                        mSpeechService.notifyListeners(MozillaSpeechService.SpeechState.ERROR, "Response error null");
                    }

                } else {
                    // WebResponse is null
                    mSpeechService.notifyListeners(MozillaSpeechService.SpeechState.ERROR, "Unknown network Error");
                }

                return null;

            }).exceptionally(throwable -> {
                // Exception happened
                throwable.printStackTrace();
                mSpeechService.notifyListeners(MozillaSpeechService.SpeechState.ERROR, String.format("An exception happened during the request: %s", throwable.getMessage()));
                return null;
            });
        });
    }
}