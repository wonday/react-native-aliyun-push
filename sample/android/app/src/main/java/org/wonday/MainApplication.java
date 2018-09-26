package org.wonday;

import android.app.Application;
import android.support.multidex.MultiDexApplication;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import com.facebook.react.ReactApplication;
import org.wonday.aliyun.push.AliyunPushPackage;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.facebook.react.common.ReactConstants;
import com.facebook.common.logging.FLog;

import java.util.Arrays;
import java.util.List;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.register.HuaWeiRegister;
import com.alibaba.sdk.android.push.register.MiPushRegister;

public class MainApplication extends MultiDexApplication implements ReactApplication {

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
            new AliyunPushPackage()
      );
    }

    @Override
    protected String getJSMainModuleName() {
    return "index";
    }

  };


  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    this.initCloudChannel(this);
  }

  /**
   * 初始化阿里云推送通道
   * @param applicationContext
   */
  private void initCloudChannel(final Context applicationContext) {
    // 创建notificaiton channel
    this.createNotificationChannel();
    PushServiceFactory.init(applicationContext);
    CloudPushService pushService = PushServiceFactory.getCloudPushService();
    pushService.setNotificationSmallIcon(R.mipmap.ic_launcher_s);
    pushService.register(applicationContext, "11111111", "11111111", new CommonCallback() {
      @Override
      public void onSuccess(String response) {
        FLog.d(ReactConstants.TAG, "init aliyun push cloudchannel success");
      }
      @Override
      public void onFailed(String code, String message) {
        FLog.d(ReactConstants.TAG, "init aliyun push cloudchannel failed. errorCode:" + code + ". errorMsg:" + message);
      }
    });

    // 注册方法会自动判断是否支持小米系统推送，如不支持会跳过注册。
    MiPushRegister.register(applicationContext, "11111111", "11111111");
    // 注册方法会自动判断是否支持华为系统推送，如不支持会跳过注册。
    HuaWeiRegister.register(applicationContext);
  }

  private void createNotificationChannel() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
          // 通知渠道的id
          String id = "1";
          // 用户可以看到的通知渠道的名字.
          CharSequence name = "sample";
          // 用户可以看到的通知渠道的描述
          String description = "xxx通知";
          int importance = NotificationManager.IMPORTANCE_HIGH;
          NotificationChannel mChannel = new NotificationChannel(id, name, importance);
          // 配置通知渠道的属性
          mChannel.setDescription(description);
          // 设置通知出现时的闪灯（如果 android 设备支持的话）
          mChannel.enableLights(true);
          mChannel.setLightColor(Color.RED);
          // 设置通知出现时的震动（如果 android 设备支持的话）
          mChannel.enableVibration(true);
          mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
          //最后在notificationmanager中创建该通知渠道
          mNotificationManager.createNotificationChannel(mChannel);
      }
  }
}
