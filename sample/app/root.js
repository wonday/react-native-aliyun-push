/**
 *app启动
 *
 */
import React, { Component } from 'react';
import App from './app';


export default class Root extends Component {
    state: State;

    constructor() {
      super();
      this.state = {
      };

    }

    render() {

      return (
          <App />
      );
    }
}