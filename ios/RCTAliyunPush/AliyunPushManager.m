/**
 * Copyright (c) 2017-present, Wonday (@wonday.org)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import <React/RCTBridge.h>
#import <React/RCTConvert.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTUtils.h>

#import "AliyunPushManager.h"
#import <CloudPushSDK/CloudPushSDK.h>

// iOS 10 notification
#import <UserNotifications/UserNotifications.h>


#ifndef __OPTIMIZE__
// only output log when debug
#define DLog( s, ... ) NSLog( @"<%p %@:(%d)> %@", self, [[NSString stringWithUTF8String:__FILE__] lastPathComponent], __LINE__, [NSString stringWithFormat:(s), ##__VA_ARGS__] )
#else
#define DLog( s, ... )
#endif

// output log both debug and release
#define ALog( s, ... ) NSLog( @"<%p %@:(%d)> %@", self, [[NSString stringWithUTF8String:__FILE__] lastPathComponent], __LINE__, [NSString stringWithFormat:(s), ##__VA_ARGS__] )


NSString *const ALIYUN_PUSH_TYPE_MESSAGE = @"message";
NSString *const ALIYUN_PUSH_TYPE_NOTIFICATION = @"notification";



@interface AliyunPushManager () <UNUserNotificationCenterDelegate>
@end

@implementation AliyunPushManager
{
    bool hasListeners;
}


static AliyunPushManager * sharedInstance = nil;

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

#pragma mark singleton instance method
+ (id)allocWithZone:(NSZone *)zone {
    static AliyunPushManager *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super allocWithZone:zone];
    });
    return sharedInstance;
}

//copy返回单例本身
- (id)copyWithZone:(NSZone *)zone
{
    return self;
}

- (instancetype)init
{
    
    if (!(self = [super init])) {
        DLog(@"init AliyunPushManager error");
    }
    sharedInstance = self;
    return self;
    
}

//获取单例
+ (AliyunPushManager *)sharedInstance
{
    @synchronized(self) {
        if (sharedInstance == nil){
            sharedInstance = [[self alloc] init];
        }
    }
    return sharedInstance;
}


RCT_EXPORT_MODULE(AliyunPush);

#pragma mark React Native Method
/**
 * Update the application icon badge number on the home screen
 */
RCT_EXPORT_METHOD(setApplicationIconBadgeNumber:(NSInteger)number)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        RCTSharedApplication().applicationIconBadgeNumber = number;
    });
}

/**
 * Get the current application icon badge number on the home screen
 */
RCT_EXPORT_METHOD(getApplicationIconBadgeNumber:(RCTResponseSenderBlock)callback)
{
    callback(@[@(RCTSharedApplication().applicationIconBadgeNumber)]);
}

/**
 * sync badge number to the server
 */
RCT_EXPORT_METHOD(syncBadgeNum:(NSInteger) num
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [CloudPushSDK syncBadgeNum:num withCallback:^(CloudPushCallbackResult *res){
        if (res.success) {
            resolve(@"");
        } else {
            reject([NSString stringWithFormat:@"%ld",(long)res.error.code], res.error.localizedDescription,res.error);
        }
    }];
}

/**
 * Get the aliyun push device id
 */
RCT_EXPORT_METHOD(getDeviceId:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *deviceId = [CloudPushSDK getDeviceId];
    if (deviceId!=Nil) {
        resolve(deviceId);
    } else {
        // 或许还没有初始化完成，等3秒钟再次尝试
        [NSThread sleepForTimeInterval:3.0f];

        deviceId = [CloudPushSDK getDeviceId];
        if (deviceId!=Nil) {
            resolve(deviceId);
        } else {
            reject([NSString stringWithFormat:@"getDeviceId() failed."], nil, RCTErrorWithMessage(@"getDeviceId() failed."));
        }

    }
}

/**
 * bind account to cloud sdk
 */
RCT_EXPORT_METHOD(bindAccount:(NSString *)account
                    resolver:(RCTPromiseResolveBlock)resolve
                    rejecter:(RCTPromiseRejectBlock)reject)
{
    [CloudPushSDK bindAccount:account withCallback:^(CloudPushCallbackResult *res) {
        if (res.success) {
            resolve(@"");
        } else {
            reject([NSString stringWithFormat:@"%ld",(long)res.error.code], res.error.localizedDescription,res.error);
        }
    }];
}

/**
 * unbind account from cloud sdk
 */

RCT_EXPORT_METHOD(unbindAccount:(RCTPromiseResolveBlock)resolve
                       rejecter:(RCTPromiseRejectBlock)reject)
{
    [CloudPushSDK unbindAccount:^(CloudPushCallbackResult *res) {
        if (res.success) {
            resolve(@"");
        } else {
            reject([NSString stringWithFormat:@"%ld",(long)res.error.code], res.error.localizedDescription,res.error);
        }
    }];
}

/**
 * bind tags to cloud sdk */
RCT_EXPORT_METHOD(bindTag:(int)target
                  withTags:(NSArray *)tags
                  withAlias:(NSString *)alias
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [CloudPushSDK bindTag:target
                 withTags:tags
                withAlias:alias
             withCallback:^(CloudPushCallbackResult *res) {
                 if (res.success) {
                     resolve(@"");
                 } else {
                     reject([NSString stringWithFormat:@"%ld",(long)res.error.code], res.error.localizedDescription,res.error);
                 }
             }];
}

/**
 * list tags from cloud sdk
 */

RCT_EXPORT_METHOD(unbindTag:(int)target
                  withTags:(NSArray *)tags
                  withAlias:(NSString *)alias
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [CloudPushSDK unbindTag:target
                   withTags:tags
                  withAlias:alias
               withCallback:^(CloudPushCallbackResult *res) {
                   if (res.success) {
                       resolve(@"");
                   } else {
                       reject([NSString stringWithFormat:@"%ld",(long)res.error.code], res.error.localizedDescription,res.error);
                   }
               }];
}

/**
 * list tags of target
 */

RCT_EXPORT_METHOD(listTags:(int)target
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [CloudPushSDK listTags:target
              withCallback:^(CloudPushCallbackResult *res) {
                  if (res.success) {
                      resolve(res.data);
                  } else {
                      reject([NSString stringWithFormat:@"%ld",(long)res.error.code], res.error.localizedDescription,res.error);
                  }
             }];
}

/**
 * add alias to cloud sdk
 */
RCT_EXPORT_METHOD(addAlias:(NSString *)alias
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [CloudPushSDK addAlias:alias withCallback:^(CloudPushCallbackResult *res) {
        if (res.success) {
            resolve(@"");
        } else {
            reject([NSString stringWithFormat:@"%ld",(long)res.error.code], res.error.localizedDescription,res.error);
        }
    }];
}

/**
 * remove alias from cloud sdk
 */

RCT_EXPORT_METHOD(removeAlias:(NSString *)alias
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [CloudPushSDK removeAlias:alias withCallback:^(CloudPushCallbackResult *res) {
        if (res.success) {
            resolve(@"");
        } else {
            reject([NSString stringWithFormat:@"%ld",(long)res.error.code], res.error.localizedDescription,res.error);
        }
    }];
}

/**
 * list aliases of target
 */

RCT_EXPORT_METHOD(listAliases:(RCTPromiseResolveBlock)resolve
                     rejecter:(RCTPromiseRejectBlock)reject)
{
    [CloudPushSDK listAliases:^(CloudPushCallbackResult *res) {
        if (res.success) {
            resolve(res.data);
        } else {
            reject([NSString stringWithFormat:@"%ld",(long)res.error.code], res.error.localizedDescription,res.error);
        }
    }];
}

/**
 * 主动获取设备通知是否授权
 */
RCT_EXPORT_METHOD(getAuthorizationStatus:(RCTResponseSenderBlock)callback)
{
    float systemVersionNum = [[[UIDevice currentDevice] systemVersion] floatValue];
    if (systemVersionNum >= 10.0) {
        UNUserNotificationCenter *_notificationCenter = [UNUserNotificationCenter currentNotificationCenter];
        [_notificationCenter getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {
            if (settings.authorizationStatus == UNAuthorizationStatusAuthorized) {
                callback(@[@(TRUE)]);
            } else {
                callback(@[@(FALSE)]);
            }
        }];
    } else {
        callback(@[@(TRUE)]);
    }
}

#pragma mark
- (NSArray<NSString *> *)supportedEvents
{
    return @[@"aliyunPushReceived"];
}

#pragma mark

/**
 set parames for aliyun sdk
 
 @param appKey aliyun push appKey
 @param appSecret aliyun push appSecret
 @param launchOptions app launch Options
 @param createNotificationCategoryHandler callback for create user's customized notification category
 */
- (void)setParams:(NSString *)appKey appSecret:(NSString *)appSecret lauchOptions:(NSDictionary *)launchOptions createNotificationCategoryHandler:(void (^)(void))createNotificationCategoryHandler
{
    
    // APNs注册，获取deviceToken并上报
    [self registerAPNs:createNotificationCategoryHandler];
    
    // 初始化SDK
    [self initCloudPush:appKey appSecret:appSecret];
    
    // 监听推送通道打开动作
    [self listenerOnChannelOpened];
    
    // 监听推送消息到达
    [self registerMessageReceive];
    
    // 点击通知将App从关闭状态启动时，将通知打开回执上报
    [CloudPushSDK sendNotificationAck:launchOptions];
    
}

/*
 *  APNs注册成功回调，将返回的deviceToken上传到CloudPush服务器
 */
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    DLog(@"Upload deviceToken to CloudPush server.");
    [CloudPushSDK registerDevice:deviceToken withCallback:^(CloudPushCallbackResult *res) {
        if (res.success) {
            DLog(@"Register deviceToken success, deviceToken: %@", [CloudPushSDK getApnsDeviceToken]);
        } else {
            DLog(@"Register deviceToken failed, error: %@", res.error);
        }
    }];
}

/*
 *  APNs注册失败回调
 */
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    DLog(@"didFailToRegisterForRemoteNotificationsWithError %@", error);
}

- (void)application:(UIApplication *)application didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings
{
    
}

#pragma mark APNs Register
/**
 *	向APNs注册，获取deviceToken用于推送
 *
 */
- (void)registerAPNs:(void (^)(void))createNotificationCategoryHandler
{
    float systemVersionNum = [[[UIDevice currentDevice] systemVersion] floatValue];
    if (systemVersionNum >= 10.0) {
        
        // iOS 10 notifications
        UNUserNotificationCenter *_notificationCenter = [UNUserNotificationCenter currentNotificationCenter];
        // 创建category，并注册到通知中心
        if (createNotificationCategoryHandler) {
            createNotificationCategoryHandler();
        }

        
        _notificationCenter.delegate = sharedInstance;
        // 请求推送权限
        [_notificationCenter requestAuthorizationWithOptions:UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (granted) {
                // granted
                DLog(@"User authored notification.");
                dispatch_async(dispatch_get_main_queue(), ^{
                    // 向APNs注册，获取deviceToken
                    [[UIApplication sharedApplication] registerForRemoteNotifications];
                });
                
            } else {
                // not granted
                DLog(@"User denied notification.");
            }
        }];
    } else if (systemVersionNum >= 8.0) {
        // iOS 8 Notifications
#pragma clang diagnostic push
#pragma clang diagnostic ignored"-Wdeprecated-declarations"
        [[UIApplication sharedApplication] registerUserNotificationSettings:
         [UIUserNotificationSettings settingsForTypes:
          (UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge)
                                           categories:nil]];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
#pragma clang diagnostic pop
    } else {
        // iOS < 8 Notifications
#pragma clang diagnostic push
#pragma clang diagnostic ignored"-Wdeprecated-declarations"
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:
         (UIRemoteNotificationTypeAlert | UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound)];
#pragma clang diagnostic pop
    }
}

#pragma mark SDK Init
- (void)initCloudPush:(NSString *)appKey appSecret:(NSString *)appSecret
{
    
    // 正式上线建议关闭
    //[CloudPushSDK turnOnDebug];
    
    // SDK初始化
    [CloudPushSDK asyncInit:appKey appSecret:appSecret callback:^(CloudPushCallbackResult *res) {
        if (res.success) {
            DLog(@"Push SDK init success, deviceId: %@.", [CloudPushSDK getDeviceId]);
        } else {
            DLog(@"Push SDK init failed, error: %@", res.error);
        }
    }];
    
}

/**
 *  App处于前台时收到通知(iOS 10+)
 */
- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler
{
    DLog(@"Receive a notification in foregound.");
    
    
    UNNotificationRequest *request = notification.request;
    UNNotificationContent *content = request.content;
    NSDictionary *userInfo = content.userInfo;
    
    NSMutableDictionary *notificationDict = [NSMutableDictionary dictionaryWithCapacity:5];
    
    // 通知时间
    notificationDict[@"date"] = notification.date;
    
    // 标题
    notificationDict[@"title"] = content.title;
    
    // 副标题
    notificationDict[@"subtitle"] = content.subtitle;
    
    // 内容
    notificationDict[@"body"] = content.body;
    
    // 保存角标并设置
    notificationDict[@"badge"] = content.badge;
    [UIApplication sharedApplication].applicationIconBadgeNumber = [content.badge intValue];
    
    // 取得通知自定义字段内容
    notificationDict[@"extras"] =userInfo;
    
    // 类型 “notification” or "message"
    notificationDict[@"type"] = ALIYUN_PUSH_TYPE_NOTIFICATION;
    
    // sent to Js
    [self sendEventToJs:notificationDict];
    
    // 通知打开回执上报
    [CloudPushSDK sendNotificationAck:userInfo];    
    
    if ([content.body isEqualToString:@""]){
        
        // 通知不弹出
        completionHandler(UNNotificationPresentationOptionNone);
        
    } else {
    
        // 通知弹出，且带有声音、内容和角标
        completionHandler(UNNotificationPresentationOptionSound | UNNotificationPresentationOptionAlert | UNNotificationPresentationOptionBadge);
        
    }
}

/**
 *  触发通知动作时回调，比如点击、删除通知和点击自定义action(iOS 10+)
 */
- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler
{
    DLog(@"Open/delete a notification.");
    
    UNNotificationRequest *request = response.notification.request;
    UNNotificationContent *content = request.content;
    NSDictionary *userInfo = content.userInfo;
    
    NSMutableDictionary *notificationDict = [NSMutableDictionary dictionary];
    
    // 通知时间
    notificationDict[@"date"] = response.notification.date;
    
    // 标题
    notificationDict[@"title"] = content.title;
    
    // 副标题
    notificationDict[@"subtitle"] = content.subtitle;
    
    // 内容
    notificationDict[@"body"] = content.body;
    
    // 角标
    notificationDict[@"badge"] = content.badge;
    [UIApplication sharedApplication].applicationIconBadgeNumber = [content.badge intValue];
    
    // 取得通知自定义字段内容
    notificationDict[@"extras"] = userInfo;
    
    // 类型 “notification” or "message"
    notificationDict[@"type"] = ALIYUN_PUSH_TYPE_NOTIFICATION;
    
    if ([response.actionIdentifier isEqualToString:UNNotificationDefaultActionIdentifier]) {
        
        // 用户动作
        notificationDict[@"actionIdentifier"] = @"opened";
        
    } else if([response.actionIdentifier isEqualToString:UNNotificationDismissActionIdentifier]) {
        
        // 用户动作
        notificationDict[@"actionIdentifier"] = @"removed";
        
        
    } else {

        // 用户自定义action
        notificationDict[@"actionIdentifier"] =response.actionIdentifier;
        
    }
    
    // sent to Js
    [self sendEventToJs:notificationDict];
    
    // 通知打开回执上报
    [CloudPushSDK sendNotificationAck:userInfo];
    
    // 通知不弹出
    completionHandler();
}

/**
 *  收到静默通知
 */
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult result))completionHandler
{
    DLog(@"Open/delete a notification(didReceiveRemoteNotification).");
    
    // 取得APNS通知内容
    NSDictionary *aps = [userInfo valueForKey:@"aps"];
    
    NSMutableDictionary *notificationDict = [NSMutableDictionary dictionary];
    
    // 通知时间，修复：iOS低版本下点击推送闪退的问题（未分配内存就初始化对象）
    notificationDict[@"date"] = [[NSDate alloc] init];
    
    // 标题
    notificationDict[@"title"] = @"";
    
    // 副标题
    notificationDict[@"subtitle"] = @"";
    
    // 内容
    notificationDict[@"body"] = [aps valueForKey:@"alert"];
    
    // 保存角标并设置
    notificationDict[@"badge"] = [aps valueForKey:@"badge"];
    [UIApplication sharedApplication].applicationIconBadgeNumber = [[aps valueForKey:@"badge"] intValue];
    
    // 取得通知自定义字段内容，例：获取key为"Extras"的内容
    notificationDict[@"extras"] =userInfo;
    
    // 类型 “notification” or "message"
    notificationDict[@"type"] = ALIYUN_PUSH_TYPE_NOTIFICATION;
    
    // sent to Js
    [self sendEventToJs:notificationDict];
    
    // 通知打开回执上报
    [CloudPushSDK sendNotificationAck:userInfo];
    

    completionHandler(UIBackgroundFetchResultNewData);
}


#pragma mark Channel Opened
/**
 *	注册推送通道打开监听
 */
- (void)listenerOnChannelOpened
{
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(onChannelOpened:)
                                                 name:@"CCPDidChannelConnectedSuccess"
                                               object:nil];
}

/**
 *	推送通道打开回调
 *
 *	@param 	notification
 */
- (void)onChannelOpened:(NSNotification *)notification
{
    DLog(@"ChannelOpened.");
}

#pragma mark Receive Message
/**
 *	@brief	注册推送消息到来监听
 */
- (void)registerMessageReceive
{
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(onMessageReceived:)
                                                 name:@"CCPDidReceiveMessageNotification"
                                               object:nil];
}

/**
 *	处理到来推送消息
 *
 *	@param 	notification
 */
- (void)onMessageReceived:(NSNotification *)notification
{
    DLog(@"onMessageReceived.");
    
    NSMutableDictionary *notificationDict = [NSMutableDictionary dictionary];
    
    CCPSysMessage *message = [notification object];
    
    notificationDict[@"title"] = [[NSString alloc] initWithData:message.title encoding:NSUTF8StringEncoding];
    notificationDict[@"body"] = [[NSString alloc] initWithData:message.body encoding:NSUTF8StringEncoding];
    // 取得通知自定义字段内容
    if (notification.userInfo) {
        notificationDict[@"extras"] = notification.userInfo;
    } else {
        notificationDict[@"extras"] = @{};
    }
    
    
    // 类型 “notification” or "message"
    notificationDict[@"type"] = ALIYUN_PUSH_TYPE_MESSAGE;
    
    [self sendEventToJs:notificationDict];
    
}

- (void)sendEventToJs:(NSMutableDictionary*)notification
{
    DLog(@"sendEventToJs:");
    
    for (NSString *key in notification) {
        DLog(@"key: %@ value: %@", key, notification[key]);
    }
    
    notification[@"appState"] = [self getAppState];

    //修正app退出后，点击通知会闪退bug
    AliyunPushManager* __weak weakSelf = self;
    if(!hasListeners){
        initialNotification = notification;
        return;
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        //修正app退出后，点击通知会闪退bug
        if([UIApplication sharedApplication].applicationState == UIApplicationStateActive
           ||[UIApplication sharedApplication].applicationState == UIApplicationStateInactive) {
            [weakSelf sendEventWithName:@"aliyunPushReceived" body:notification];
        }
    });
}

-(void)startObserving {
    hasListeners = YES;
    // Set up any upstream listeners or background tasks as necessary
}
-(void)stopObserving {
    hasListeners = NO;
    // Remove upstream listeners, stop unnecessary background tasks
}
//存储初始化的消息
static NSMutableDictionary * initialNotification = nil;

RCT_EXPORT_METHOD(getInitialMessage:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve(initialNotification);
}
-(NSString *)getAppState
{
    UIApplicationState state = [UIApplication sharedApplication].applicationState;
    if(state == UIApplicationStateActive){
        return @"active";
    }else if(state == UIApplicationStateInactive){
        return @"inactive";
    }else if(state == UIApplicationStateBackground){
        return @"background";
    }else{
        return nil;
    }
}
@end
