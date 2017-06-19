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

    public static boolean isMIUI() {
        if(hasChecked)
        {
            return isMIUI;
        }

        Properties prop= new Properties();

        try {
            prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        isMIUI= prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
        || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
        || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;

        hasChecked = true;

        return isMIUI;
    }

    /*
     * 由于Notification.when不起作用，故需要延时操作时采用postAtTime(Runnable r, Object token, long uptimeMillis)
     * 其中token用来标记该runnable，以便后面取消用
     * 注意：如果到了触发通知时，应用程序不在后台运行，则不会弹出通知，即app必须处于运行状态才有效
     */
    private static boolean initialized = false;
    private static Handler mHandler;
    private static int notificationId = (int)(System.currentTimeMillis()/1000);
    private static int requestCount = 0;

    public static void setBadgeNumber(Context context, Class<?> cls, int badgeNumber) throws Exception{
        getHandler().removeCallbacksAndMessages(Integer.toString(notificationId));
        registerLocalNotification(context, cls, badgeNumber);
    }

    private static Handler getHandler() {
        if (!initialized) {
            initialized = true;
            HandlerThread thread = new HandlerThread("Notifier");
            thread.start();
            mHandler = new Handler(thread.getLooper());
        }
        return mHandler;
    }

    private static void registerLocalNotification(final Context context, Class<?> cls, int badgeNumber) throws Exception{

        FLog.e(ReactConstants.TAG, "MIUIUtils:registerLocalNotification");

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

        requestCount++;

        getHandler().post(new Runnable() {
            @Override
            public void run() {
                FLog.e(ReactConstants.TAG, "MIUIUtils:registerLocalNotification run...");
                requestCount--;

                if (isAppOnBackground(context)) {
                    FLog.e(ReactConstants.TAG, "MIUIUtils:registerLocalNotification notified.");

                    //getNotificationManager(context).cancel(notificationId);
                    getNotificationManager(context).cancelAll();
                    getNotificationManager(context).notify(notificationId++, notification);
                    return;
                }

                // 非后台时会到此
                // requestCount>0 表明已经有新的请求， 本次废弃， 否则再次请求执行
                if (requestCount==0){
                    requestCount++;
                    getHandler().postDelayed(this, 1000);
                }
            }
        });
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

/*
    private static boolean isAppOnBackground(Context context) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    FLog.i(ReactConstants.TAG, "MIUIUtils:isAppOnBackground() true");
                    return true;
                } else {
                    FLog.i(ReactConstants.TAG, "MIUIUtils:isAppOnBackground() false");
                    return false;
                }
            }
        }
        return false;
    }
*/
     /**
      *判断当前应用程序处于前台还是后台
      *
      * @param context
      * @return

      */

     public static boolean isAppOnBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }

        return false;
     }
}