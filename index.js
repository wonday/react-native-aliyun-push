/**
 * Copyright (c) 2017-present, Wonday (@wonday.org)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

'use strict';
import React,{ Component } from 'react';
import {
    requireNativeComponent,
    Platform,
} from 'react-native';

const AliyunPushNative = require('react-native').NativeModules.AliyunPush;
const { NativeEventEmitter } = require('react-native');
const LocalEventEmitter =  new NativeEventEmitter(AliyunPushNative);

var listeners = {};

var id = 0;
var META = '__listener_id';

function getKey(listener,type){

    if (!listener.hasOwnProperty(META)){
        if (!Object.isExtensible(listener)) {
            return 'F';
        }
        Object.defineProperty(listener, META, {
            value: 'L' + type + ++id,
        });
    }
    return listener[META];

};


export default class AliyunPush {

    static getDeviceId = () => {
        return AliyunPushNative.getDeviceId();
    }

    static getInitialMessage = () => {
        return AliyunPushNative.getInitialMessage().then(e => {
            if(e && e.extraStr) {
                let extras = JSON.parse(e.extraStr);
                if (extras) {
                    if (extras.badge) {
                        let badgeNumber = parseInt(extras.badge);
                        if (!isNaN(badgeNumber)) {
                            AliyunPush.setApplicationIconBadgeNumber(badgeNumber);
                        }
                    }
                    e.extras = extras;
                }
                delete e.extraStr;
            }
            return e;
        });
    }

    static getApplicationIconBadgeNumber = (callback) => {
        AliyunPushNative.getApplicationIconBadgeNumber(function(args) {
            callback(args);
        });
    }

    static setApplicationIconBadgeNumber = (num) => {
        AliyunPushNative.setApplicationIconBadgeNumber(num);
    }

    static syncBadgeNum = (num) => {
        if(Platform.OS === 'android') {
            return;
        }
        AliyunPushNative.syncBadgeNum(num);
    }

    static bindAccount = (account) => {
        return AliyunPushNative.bindAccount(account);
    }

    static unbindAccount = () => {
        return AliyunPushNative.unbindAccount();
    }

    static bindTag = (target, tags, alias) => {
        return AliyunPushNative.bindTag(target, tags, alias);
    }

    static unbindTag = (target, tags, alias) => {
        return AliyunPushNative.unbindTag(target, tags, alias);
    }

    static listTags = (target) => {
        return AliyunPushNative.listTags(target);
    }

    static addAlias = (alias) => {
        return AliyunPushNative.addAlias(alias);
    }

    static removeAlias = (alias) => {
        return AliyunPushNative.removeAlias(alias);
    }

    static listAliases = () => {
        return AliyunPushNative.listAliases();
    }

    static getAuthorizationStatus = (callback) => {
        if(Platform.OS === 'android') {
            // android always return true
            callback(true);
        } else {
            AliyunPushNative.getAuthorizationStatus(function(args) {
                callback(args);
            });
        }
    }


    static addListener = (callback) => {

        AliyunPush._addListener(callback,"aliyunPushReceived");

    };

    static removeListener = (callback) => {

        AliyunPush._removeListener(callback, "aliyunPushReceived");

    };

    static removeAllListeners = () => {
        for(var key in listeners){
            listeners[key].remove();
            listeners[key] = null;
        }
    };

    static _addListener = (callback,type) => {

        var key = getKey(callback,type);
        listeners[key] = LocalEventEmitter.addListener(type,
            (e) => {
                // convert json string to obj
                if (e.extraStr) {
                    let extras = JSON.parse(e.extraStr);
                    if (extras) {
                        if (extras.badge) {
                            let badgeNumber = parseInt(extras.badge);
                            if (!isNaN(badgeNumber)) {
                                AliyunPush.setApplicationIconBadgeNumber(badgeNumber);
                            }
                        }
                        e.extras = extras;
                    }
                    delete e.extraStr;
                    callback(e);
                } else {
                    callback(e);
                }

            });

    };

    static _removeListener = (callback,type) => {

        var key = getKey(callback,type);
        if (!listeners[key]) {
            return;
        }
        listeners[key].remove();
        listeners[key] = null;

    };
}
