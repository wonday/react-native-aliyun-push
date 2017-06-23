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

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.common.logging.FLog;
import com.facebook.react.common.ReactConstants;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.MessageReceiver;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.notification.CPushMessage;

public class AliyunPushMessageReceiver extends MessageReceiver {
    public static ReactApplicationContext context;
    public static AliyunPushMessageReceiver instance;

    private final String ALIYUN_PUSH_TYPE_MESSAGE = "message";
    private final String ALIYUN_PUSH_TYPE_NOTIFICATION = "notification";

    public AliyunPushMessageReceiver() {
        super();
        instance = this;
    }

    @Override
    protected void onMessage(Context context, CPushMessage cPushMessage) {

        super.onMessage(context, cPushMessage);

        WritableMap params = Arguments.createMap();
        params.putString("messageId", cPushMessage.getMessageId());
        params.putString("body", cPushMessage.getContent());
        params.putString("title", cPushMessage.getTitle());
        params.putString("type", ALIYUN_PUSH_TYPE_MESSAGE);

        sendEvent("aliyunPushReceived", params);
    }

    @Override
    protected void onNotification(Context context, String title, String content, Map<String, String> extraMap) {
        FLog.d(ReactConstants.TAG, "onNotification.");

        super.onNotification(context, title, content, extraMap);

        WritableMap params = Arguments.createMap();
        params.putString("body", content);
        params.putString("title", title);

        WritableMap extraWritableMap = Arguments.createMap();
        for (Map.Entry<String, String> entry : extraMap.entrySet()) {
            extraWritableMap.putString(entry.getKey(),entry.getValue());
        }
        params.putMap("extras", extraWritableMap);

        params.putString("type", ALIYUN_PUSH_TYPE_NOTIFICATION);

        sendEvent("aliyunPushReceived", params);
    }

    @Override
    protected void onNotificationOpened(Context context, String title, String content, String extraMap) {
        FLog.d(ReactConstants.TAG, "onNotificationOpened.");

        super.onNotificationOpened(context, title, content, extraMap);

        WritableMap params = Arguments.createMap();
        params.putString("body", content);
        params.putString("title", title);
        params.putString("extraStr", extraMap);

        params.putString("type", ALIYUN_PUSH_TYPE_NOTIFICATION);
        params.putString("actionIdentifier", "opened");

        sendEvent("aliyunPushReceived", params);
    }

    @Override
    protected void onNotificationClickedWithNoAction(Context context, String title, String content, String extraMap) {
        FLog.d(ReactConstants.TAG, "onNotificationClickedWithNoAction.");

        super.onNotificationOpened(context, title, content, extraMap);

        WritableMap params = Arguments.createMap();
        params.putString("body", content);
        params.putString("title", title);
        params.putString("extraStr", extraMap);

        params.putString("type", ALIYUN_PUSH_TYPE_NOTIFICATION);
        params.putString("actionIdentifier", "opened");

        sendEvent("aliyunPushReceived", params);
    }

    @Override
    protected void onNotificationRemoved(Context context, String messageId){
        FLog.d(ReactConstants.TAG, "onNotificationRemoved: messageId=" +  messageId);

        super.onNotificationRemoved(context, messageId);

        WritableMap params = Arguments.createMap();
        params.putString("messageId", messageId);

        params.putString("type", ALIYUN_PUSH_TYPE_NOTIFICATION);
        params.putString("actionIdentifier", "removed");

        sendEvent("aliyunPushReceived", params);
    }

    @Override
    public void onNotificationReceivedInApp(Context context, String title, String content, Map<String, String> extraMap, int openType, String openActivity, String openUrl) {
        FLog.d(ReactConstants.TAG, "onNotificationReceivedInApp");

        super.onNotificationReceivedInApp(context, title, content, extraMap, openType, openActivity, openUrl);

        WritableMap params = Arguments.createMap();
        params.putString("content", content);
        params.putString("title", title);
        params.putString("openType", String.valueOf(openType));
        params.putString("openActivity", openActivity);
        params.putString("openUrl", openUrl);

        WritableMap extraWritableMap = Arguments.createMap();
        for (Map.Entry<String, String> entry : extraMap.entrySet()) {
            extraWritableMap.putString(entry.getKey(),entry.getValue());
        }
        params.putMap("extras", extraWritableMap);

        params.putString("type", ALIYUN_PUSH_TYPE_NOTIFICATION);

        sendEvent("aliyunPushReceived", params);
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        if (context == null) {
            FLog.d(ReactConstants.TAG, "reactContext==null");
        }else{
            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
        }
    }
}