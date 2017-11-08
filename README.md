# react-native-aliyun-push
[![npm](https://img.shields.io/npm/v/react-native-aliyun-push.svg?style=flat-square)](https://www.npmjs.com/package/react-native-aliyun-push)

[阿里云移动推送](https://www.aliyun.com/product/cps?spm=5176.2020520107.0.0.fgXGFp)react-native封装组件


### 修改履历


v1.0.4

1. 删除对PropTypes的import #8

v1.0.3

1. 修正iOS上xcode9编译警告 #6 
2. iOS版本工程文件升级为xcode9格式

v1.0.2

1. 修正iOS上actionIdentifier错误,"open"->"opened" #5 

v1.0.1

1. 设置badgeNumber时，增加对badgeNumber判断，避免小米上显示0条消息未读

v1.0.0

1. 初始发布


## 前提
使用本组件前提是注册过阿里云移动推送服务，注册过app并取得了appKey及appSecret, 如果要使用ios版还要向苹果公司申请证书并配置好阿里云上的设置。
这里不详细描述，请参考[阿里云移动推送文档](https://help.aliyun.com/document_detail/30054.html)
## 安装
```
npm install react-native-aliyun-push --save
react-native link react-native-aliyun-push
```
## android配置
1. 在Project根目录下build.gradle文件中配置maven库URL:
```
allprojects {
    repositories {
        mavenLocal()
        jcenter()
        maven {
            // All of React Native (JS, Obj-C sources, Android binaries) is installed from npm
            url "$rootDir/../node_modules/react-native/android"
        }
        // 下面是添加的代码
        maven {
            url "http://maven.aliyun.com/nexus/content/repositories/releases/"
        }
        flatDir {
            dirs project(':react-native-aliyun-push').file('libs')
        }
        // 添加结束
    }
}
```
2. 确保settings.gradle中被添加如下代码：
```
include ':react-native-aliyun-push'
project(':react-native-aliyun-push').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-aliyun-push/android')
```
3. 确保app/build.gradle中被添加如下代码：
```
dependencies {
    //下面是被添加的代码
    compile project(':react-native-aliyun-push')
    //添加结束
}
```
4. 确保MainApplication.java中被添加如下代码
```
// 下面是被添加的代码
import org.wonday.aliyun.push.AliyunPushPackage;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.register.HuaWeiRegister;
import com.alibaba.sdk.android.push.register.MiPushRegister;
// 添加结束
...
    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
            //下面是被添加的代码
            new AliyunPushPackage()
            //添加结束
      );
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();
    SoLoader.init(this, /* native exopackage */ false);
    
    //下面是添加的代码
    this.initCloudChannel();
    //添加结束
  }

  // 下面是添加的代码
  /**
   * 初始化阿里云推送通道
   * @param applicationContext
   */
  private void initCloudChannel() {
    PushServiceFactory.init(this.getApplicationContext());
    CloudPushService pushService = PushServiceFactory.getCloudPushService();
    pushService.setNotificationSmallIcon(R.mipmap.ic_launcher_s);//设置通知栏小图标， 需要自行添加
    pushService.register(this.getApplicationContext(), "阿里云appKey", "阿里云appSecret", new CommonCallback() {
      @Override
      public void onSuccess(String responnse) {
        // success
      }
      @Override
      public void onFailed(String code, String message) {
        // failed
      }
    });

    // 注册方法会自动判断是否支持小米系统推送，如不支持会跳过注册。
    MiPushRegister.register(this.getApplicationContext(), "小米AppID", "小米AppKey");
    // 注册方法会自动判断是否支持华为系统推送，如不支持会跳过注册。
    HuaWeiRegister.register(this.getApplicationContext());
  }
  // 添加结束

  
```

## ios配置

1. 添加node_modules/react-native-aliyun-push/ios/RCTAliyunPush.xcodeproj到xcode项目工程

2. 添加阿里云移动推送SDK

拖拽node_modules/react-native-aliyun-push/ios/libs下列目录到xcode工程的```frameworks```目录下，将```copy items if needed```打勾。
注意：从阿里云下载的SDK中UTDID.framework有问题，编译会报错，请使用react-native-aliyun-push中内置的版本。

- AlicloudUtils.framework
- CloudPushSDK.framework
- UTDID.framework

3. 点击项目根节点，在targets app的属性BuildPhase的Link Binary With Libraries中添加公共包依赖

- libz.tbd
- libresolv.tbd
- libsqlite3.tbd
- CoreTelephony.framework
- SystemConfiguration.framework
- UserNotifications.framework

4. 修改AppDelegate.m添加如下代码
```
#import "AliyunPushManager.h"
```

```
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  
...

  // 下面是添加的代码
  [[AliyunPushManager sharedInstance] setParams:@"阿里云appKey"
                                      appSecret:@"阿里云appSecret"
                                   lauchOptions:launchOptions
              createNotificationCategoryHandler:^{
                //create customize notification category here
  }];
  // 添加结束
  
  return YES;
}

```

```
// 下面是添加的代码

// APNs注册成功回调，将返回的deviceToken上传到CloudPush服务器
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
  [[AliyunPushManager sharedInstance] application:application didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}


// APNs注册失败回调
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
  [[AliyunPushManager sharedInstance] application:application didFailToRegisterForRemoteNotificationsWithError:error];
}

// 打开／删除通知回调
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult result))completionHandler
{
  [[AliyunPushManager sharedInstance] application:application didReceiveRemoteNotification:userInfo fetchCompletionHandler:completionHandler];
}


// 请求注册设定后，回调
- (void)application:(UIApplication *)application didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings
{
  [[AliyunPushManager sharedInstance] application:application didRegisterUserNotificationSettings:notificationSettings];
}
// 添加结束
```

## 使用示例

引入模块
```
import AliyunPush from 'react-native-aliyun-push';
```

监听推送事件
```
componentDidMount() {
    //监听推送事件
    AliyunPush.addListener(this.handleAliyunPushMessage);
}

componentWillUnmount() {
    //
    AliyunPush.removeListener(this.handleAliyunPushMessage);
}

handleAliyunPushMessage = (e) => {
	console.log("Message Received. " + JSON.stringify(e));


    //e结构说明:
    //e.type: "notification":通知 或者 "message":消息
    //e.title: 推送通知/消息标题
    //e.body: 推送通知/消息具体内容
    //e.actionIdentifier: "opened":用户点击了通知, "removed"用户删除了通知, 其他非空值:用户点击了自定义action（仅限ios）
    //e.extras: 用户附加的{key:value}的对象

};

```

## 阿里云SDK接口封装
详细参数说明请参考阿里云移动推送SDK [[android版]](https://help.aliyun.com/document_detail/30066.html?spm=5176.doc30064.6.643.Mu5vP0)    [[ios版]](https://help.aliyun.com/document_detail/42668.html?spm=5176.doc30066.6.649.VmzJfM)

**获取deviceId**

示例:
```
AliyunPush.getDeviceId((deviceId)=>{
    console.log("AliyunPush DeviceId:" + deviceId);
});
```
**绑定账号**

参数：
- account 待绑定账号

示例:
```
AliyunPush.bindAccount(account)
    .then((data)=>{
        console.log("bindAccount success");
        console.log(JSON.stringify(data));
    })
    .catch((error)=>{
        console.log("bindAccount error");
        console.log(JSON.stringify(error));
    });
```
**解绑定账号**

示例:
```
AliyunPush.unbindAccount()
    .then((result)=>{
        console.log("unbindAccount success");
        console.log(JSON.stringify(result));
    })
    .catch((error)=>{
        console.log("bindAccount error");
        console.log(JSON.stringify(error));
    });
```
**绑定标签**

参数：
- target 目标类型，1：本设备；2：本设备绑定账号；3：别名
- tags 标签（数组输入）
- alias 别名（仅当target = 3时生效）

示例:
```
AliyunPush.bindTag(1,["testtag1","testtag2"],"")
    .then((result)=>{
        console.log("bindTag success");
        console.log(JSON.stringify(result));
    })
    .catch((error)=>{
        console.log("bindTag error");
        console.log(JSON.stringify(error));
    });
```
**解绑定标签**

参数:
- target 目标类型，1：本设备；2：本设备绑定账号；3：别名
- tags 标签（数组输入）
- alias 别名（仅当target = 3时生效）

示例:
```
AliyunPush.unbindTag(1,["testTag1"],"")
    .then((result)=>{
        console.log("unbindTag succcess");
        console.log(JSON.stringify(result));
    })
    .catch((error)=>{
        console.log("unbindTag error");
        console.log(JSON.stringify(error));
    });
```
**查询当前Tag列表**

参数:
- target 目标类型，1：本设备

示例:
```
AliyunPush.listTags(1)
    .then((result)=>{
        console.log("listTags success");
        console.log(JSON.stringify(result));
    })
    .catch((error)=>{
        console.log("listTags error");
        console.log(JSON.stringify(error));
    });
```
**添加别名**

参数:
- alias 要添加的别名

示例:
```
AliyunPush.addAlias("testAlias")
    .then((result)=>{
        console.log("addAlias success");
        console.log(JSON.stringify(result));
    })
    .catch((error)=>{
        console.log("addAlias error");
        console.log(JSON.stringify(error));
    });
```
**删除别名**

参数:
- alias 要移除的别名

示例:
```
AliyunPush.removeAlias("testAlias")
    .then((result)=>{
        console.log("removeAlias success");
        console.log(JSON.stringify(result));
    })
    .catch((error)=>{
        console.log("removeAlias error");
        console.log(JSON.stringify(error));
    });
```
**查询别名列表**

示例:
```
AliyunPush.listAliases()
    .then((result)=>{
        console.log("listAliases success");
        console.log(JSON.stringify(result));
    })
    .catch((error)=>{
        console.log("listAliases error");
        console.log(JSON.stringify(error));
    });
```
**设置桌面图标角标数字** (ios支持，android支持绝大部分手机)

参数:
- num角标数字，如果要清除请设置0

示例:
```
AliyunPush.setApplicationIconBadgeNumber(5);
```
**获取桌面图标角标数字** (ios支持，android支持绝大部分手机)

示例:
```
AliyunPush.getApplicationIconBadgeNumber((num)=>{
    console.log("ApplicationIconBadgeNumber:" + num);
});
```