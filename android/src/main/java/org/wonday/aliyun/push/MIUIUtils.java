/**
 * Copyright (c) 2017-present, Wonday (@wonday.org)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package org.wonday.aliyun.push;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.os.Environment;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.app.ActivityManager.RunningTaskInfo;

import com.facebook.react.common.ReactConstants;
import com.facebook.common.logging.FLog;

public class MIUIUtils {

    // 检测MIUI
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static boolean hasChecked = false;
    private static boolean isMIUI = false;

    // 通知ID
    private static int notificationId = (int)(System.currentTimeMillis()/1000);


    public static boolean isMIUI(Context context) {
        if(hasChecked) {
            return isMIUI;
        }
        
        try {
            SystemProperty sp = new SystemProperty(context);
            String ret1 = "";
            String ret2 = "";
            String ret3 = "";
            ret1 = sp.getOrThrow(KEY_MIUI_VERSION_CODE);
            ret2 = sp.getOrThrow(KEY_MIUI_VERSION_NAME);
            ret3 = sp.getOrThrow(KEY_MIUI_INTERNAL_STORAGE);
            if ((ret1 != null && ret1.compareToIgnoreCase("") != 0) ||
                (ret2 != null && ret2.compareToIgnoreCase("") != 0) ||
                (ret3 != null && ret3.compareToIgnoreCase("") != 0)) {
                hasChecked = true;
                isMIUI = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return isMIUI;
    }


    //小米角标特殊处理
    public static void setBadgeNumber(Context context, Class<?> cls, int badgeNumber) {

            //避免显示0条消息
            if (badgeNumber<=0) return;

            try {

                Intent intent = new Intent(context, cls);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Builder builder = new Notification.Builder(context)
                                    .setContentTitle(context.getString(R.string.app_name))
                                    .setContentText("您有"+Integer.toString(badgeNumber)+"条未读消息")
                                    .setWhen(System.currentTimeMillis())
                                    .setContentIntent(contentIntent)
                                    .setSmallIcon(android.R.drawable.stat_notify_chat);

                final Notification notification = builder.build();

                Field field = notification.getClass().getDeclaredField("extraNotification");
                Object extraNotification = field.get(notification);
                Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
                method.invoke(extraNotification, badgeNumber);

                NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.cancelAll();
                notificationManager.notify(notificationId++, notification);

            } catch (Exception e) {

            }
    }
}