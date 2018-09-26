/**
 * @flow
 */


'use strict';
import React, { Component } from 'react';
import {
    View,
    Linking,
    Text
} from 'react-native';
import AliyunPush from 'react-native-aliyun-push';

export default class App extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {

        
        AliyunPush.getDeviceId()
            .then((aliyunDeviceId) => {
                //do something
            });



        AliyunPush.addListener(this.handleAliyunPushMessage);
        Linking.addEventListener('url', this.handleOpenURL);
        Linking.getInitialURL().then((url) => {
            if (url) {
                UrlNavigator.push(
                    {
                        url: url,
                    }
                );
            }
        }).catch((err) => {
            __DEV__ && console.error('start from url error', err)
        });

    }
    componentDidMount() {

    }

    componentWillUnmount() {

        AliyunPush.removeListener(this.handleAliyunPushMessage);
        Linking.removeEventListener('url', this.handleOpenURL);

    }

    handleAliyunPushMessage = (msg) => {


        __DEV__ && console.log("Message Received. " + JSON.stringify(msg));

        if (msg.type && msg.type === "message" ) {
            try{
                msg = JSON.parse(msg.body);
            }catch (e) {

            }

        }

        if (msg.extras
            &&msg.extras.action_ok) {

                Alert.alert(
                    msg.title,
                    msg.body,
                    [
                        {text: btnTextCancel, onPress: () => {}},
                        {text: btnTextView, onPress: () => {
                        /*
                            UrlNavigator.push(
                                {
                                    url: msg.extras.action_ok,
                                }
                            );
                            */
                        }},
                    ]
                );

        } else {
            if (!!msg.actionIdentifier===false || msg.actionIdentifier!=='removed') {
                Alert.alert(
                    msg.title,
                    msg.body
                );
            }

        }

    };

    handleOpenURL = (e) => {
        if (e.url) {
            UrlNavigator.push(
                {
                    url:e.url,
                }
            );
        }
    };


    render() {


        return (
            <View style={{flex:1,justifyContent:'center',alignItems:'center'}}>
            <Text>Hello World</Text>
            </View>
        );

    }
}

module.exports = App;
