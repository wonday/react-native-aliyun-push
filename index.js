/**
 * Copyright (c) 2017-present, Wonday (@wonday.org)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

'use strict';
import React,{ Component, PropTypes } from 'react';
import {
    requireNativeComponent,
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

    static getDeviceId = (cb)=> {
        AliyunPushNative.getDeviceId(function(args) {
            cb(args);
        });
    }

    static addMessageListener = (cb) => {

        AliyunPush._addListener(cb,"onAliyunPushMessage");

    };

    static removeMessageListener = (cb) => {

        AliyunPush._removeListener(cb, "onAliyunPushMessage");

    };

    static addNotificationListener = (cb) => {

        AliyunPush._addListener(cb,"onAliyunPushNotification");

    };

    static removeNotificationListener = (cb) => {

        AliyunPush._removeListener(cb, "onAliyunPushNotification");

    };

    static addNotificationOpenedListener = (cb) => {

        AliyunPush._addListener(cb,"onAliyunPushNotificationOpened");

    };

    static removeNotificationOpenedListener = (cb) => {

        AliyunPush._removeListener(cb, "onAliyunPushNotificationOpened");

    };


    static addNotificationRemovedListener = (cb) => {

        AliyunPush._addListener(cb,"onAliyunPushNotificationRemoved");

    };

    static removeNotificationRemovedListener = (cb) => {

        AliyunPush._removeListener(cb, "onAliyunPushNotificationRemoved");

    };

    static addNotificationReceivedInAppListener = (cb) => {

        AliyunPush._addListener(cb,"onNotificationReceivedInApp");

    };

    static removeNotificationReceivedInAppListener = (cb) => {

        AliyunPush._removeListener(cb, "onNotificationReceivedInApp");

    };

    static _addListener = (cb,type) => {

        var key = getKey(cb,type);
        listeners[key] = LocalEventEmitter.addListener(type,
            (e) => {
                if (type==="onAliyunPushNotificationOpened"){
                    if (e.extraMap){
                        let extraMap = JSON.parse(e.extraMap);
                        for (var attrname in extraMap) { e[attrname] = extraMap[attrname]; }
                        delete e.extraMap;
                        cb(e);
                    }
                } else {
                    cb(e);
                }

            });

    };

    static _removeListener = (cb,type) => {

        var key = getKey(cb,type);
        if (!listeners[key]) {
            return;
        }
        listeners[key].remove();
        listeners[key] = null;

    };
}
