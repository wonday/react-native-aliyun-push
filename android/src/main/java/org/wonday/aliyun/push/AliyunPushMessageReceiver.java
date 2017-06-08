/**
 * Copyright (c) 2017-present, Wonday (@wonday.org)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package org.wonday.aliyun.push;

import android.content.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.annotation.Nullable;

import com.facebook.common.logging.FLog;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.MessageReceiver;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.notification.CPushMessage;

public class AliyunPushMessageReceiver extends MessageReceiver {
    public static ReactApplicationContext ctx;

    public AliyunPushMessageReceiver(ReactApplicationContext reactContext) {
        super();
        ctx = reactContext;
    }

    public AliyunPushMessageReceiver() {
        super();
    }

    @Override
    protected void onMessage(Context context, CPushMessage cPushMessage) {
        FLog.i(ReactConstants.TAG, "onMessage: id=" +  cPushMessage.getMessageId() + " title=" + cPushMessage.getTitle() + " content=" + cPushMessage.getContent());

        super.onMessage(context, cPushMessage);

        WritableMap params = Arguments.createMap();
        params.putString("messageId", cPushMessage.getMessageId());
        params.putString("content", cPushMessage.getContent());
        params.putString("title", cPushMessage.getTitle());
        sendEvent("onAliyunPushMessage", params);
    }

    @Override
    protected void onNotification(Context context, String title, String content, Map<String, String> extraMap) {
        FLog.i(ReactConstants.TAG, "onNotification: title=" +  title + " content=" + content);

        super.onNotification(context, title, content, extraMap);

        WritableMap params = Arguments.createMap();
        params.putString("content", content);
        params.putString("title", title);
        for (Map.Entry<String, String> entry: extraMap.entrySet()) {
            params.putString(entry.getKey(), entry.getValue());
        }
        sendEvent("onAliyunPushNotification", params);
    }

    @Override
    protected void onNotificationOpened(Context context, String title, String content, String extraMap) {
        FLog.i(ReactConstants.TAG, "onNotificationOpened: title=" +  title + " content=" + content + " extraMap=" + extraMap);

        super.onNotificationOpened(context, title, content, extraMap);

        WritableMap params = Arguments.createMap();
        params.putString("content", content);
        params.putString("title", title);
        params.putString("extraMap", extraMap);

        sendEvent("onAliyunPushNotificationOpened", params);
    }

    @Override
    protected void onNotificationRemoved(Context context, String messageId){
        FLog.i(ReactConstants.TAG, "onNotificationRemoved: messageId=" +  messageId);

        super.onNotificationRemoved(context, messageId);

        WritableMap params = Arguments.createMap();
        params.putString("messageId", messageId);
        sendEvent("onAliyunPushNotificationRemoved", params);
    }

    @Override
    protected void onNotificationReceivedInApp(Context context, String title, String content, Map<String, String> extraMap, int openType, String openActivity, String openUrl) {
            FLog.i(ReactConstants.TAG, "onNotificationReceivedInApp: title=" +  title + " content=" + content);

            super.onNotificationReceivedInApp(context, title, content, extraMap, openType, openActivity, openUrl);

            WritableMap params = Arguments.createMap();
            params.putString("content", content);
            params.putString("title", title);
            for (Map.Entry<String, String> entry: extraMap.entrySet()) {
                params.putString(entry.getKey(), entry.getValue());
            }
            params.putString("openType", String.valueOf(openType));
            params.putString("openActivity", openActivity);
            params.putString("openUrl", openUrl);

            sendEvent("onAliyunPushNotificationReceivedInApp", params);
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        if (ctx == null) {
            FLog.i(ReactConstants.TAG, "reactContext==null");
        }else{
            ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }
}