package com.airmap.airmapsdk.networking.callbacks;

import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class GenericOkHttpCallback extends GenericBaseOkHttpCallback {

    private static final String TAG = "GenericOkHttpCallback";

    public GenericOkHttpCallback(AirMapCallback listener, Class<? extends AirMapBaseModel> classToInstantiate) {
        super(listener, classToInstantiate);
    }

    @Override
    public void onResponse(Call call, Response response) {
        if (listener == null) {
            return; //Don't need to do anything if no listener was provided
        }

        String jsonString;
        try {
            jsonString = response.body().string();
        } catch (IOException e) {
            failed(e);
            return;
        } finally {
            response.body().close();
        }

        JSONObject result = null;
        try {
            result = new JSONObject(jsonString);
        } catch (JSONException e) {
            AirMapLog.e("AirMapCallback", jsonString);
        }

        if (!response.isSuccessful() || !Utils.statusSuccessful(result)) {
            failed(response.code(), result);
            return;
        }

        JSONObject jsonObject = result.optJSONObject("data");
        if (jsonObject == null && !result.isNull("data")) {
            failed(response.code(), result);
        } else if (jsonObject == null || result.isNull("data") || jsonObject.length() == 0) {
            success(null);
        } else {
            try {
                AirMapBaseModel model = classToInstantiate.newInstance().constructFromJson(jsonObject);
                success(model);
            } catch (InstantiationException e) {
                failed(e);
            } catch (IllegalAccessException e) {
                failed(e);
            }
        }
    }
}