import { WebPlugin } from '@capacitor/core';
import { AppMetricaPlugin } from './definitions';

export class AppMetricaWeb extends WebPlugin implements AppMetricaPlugin {
  constructor() {
    super({
      name: 'AppMetrica',
      platforms: ['web'],
    });
  }
  activate(/*config: YAMConfig*/): Promise<void> {
    return Promise.reject('Web not supported. [activate()]');
  }
  reportEvent(/*options: YAMReportEventOptions*/): Promise<void> {
    return Promise.reject('Web not supported. [reportEvent()]');
  }
  reportError(/*options: YAMReportErrorOptions*/): Promise<void> {
    return Promise.reject('Web not supported. [reportError()]');
  }
  setLocation(/*location: YAMLocation*/): Promise<void> {
    return Promise.reject('Web not supported. [setLocation()]');
  }
  setLocationTracking(/*options: { enabled: boolean; }*/): Promise<void> {
    return Promise.reject('Web not supported. [setLocationTracking()]');
  }
  showScreenEvent(/*screen: ECommerceScreen*/): Promise<void> {
    return Promise.reject('Web not supported. [showScreenEvent()]');
  }
  showProductCardEvent(/*options: YAMShowProductCardEventOptions*/): Promise<void> {
    return Promise.reject('Web not supported. [showProductCardEvent()]');
  }
  showProductDetailsEvent(/*options: YAMShowProductDetailsEventOptions*/): Promise<void> {
    return Promise.reject('Web not supported. [showProductDetailsEvent()]');
  }
  addCartItemEvent(/*cartItem: ECommerceCartItem*/): Promise<void> {
    return Promise.reject('Web not supported. [addCartItemEvent()]');
  }
  removeCartItemEvent(/*cartItem: ECommerceCartItem*/): Promise<void> {
    return Promise.reject('Web not supported. [removeCartItemEvent()]');
  }
  beginCheckoutEvent(/*order: ECommerceOrder*/): Promise<void> {
    return Promise.reject('Web not supported. [beginCheckoutEvent()]');
  }
  purchaseEvent(/*order: ECommerceOrder*/): Promise<void> {
    return Promise.reject('Web not supported. [purchaseEvent()]');
  }
}

const AppMetrica = new AppMetricaWeb();

export { AppMetrica };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(AppMetrica);
