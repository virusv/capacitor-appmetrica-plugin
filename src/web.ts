import { WebPlugin } from '@capacitor/core';
import {
  AppMetricaPlugin,
  YAMConfig,
  YAMReportEventOptions,
  YAMReportErrorOptions,
  YAMLocation,
  ECommerceScreen,
  YAMShowProductCardEventOptions,
  YAMShowProductDetailsEventOptions,
  ECommerceCartItem,
  ECommerceOrder,
  YAMUserProfileId,
  YAMUserProfile
} from './definitions';

export class AppMetricaWeb extends WebPlugin implements AppMetricaPlugin {
  private isMetricaLogs: boolean = false;

  constructor() {
    super({
      name: 'AppMetrica',
      platforms: ['web'],
    });
  }

  private log(...vars: any[]) {
    if (this.isMetricaLogs) {
      console.log(...vars);
    }
  }

  async activate(config: YAMConfig): Promise<void> {
    this.isMetricaLogs = config.logs === undefined ? false : config.logs;
    this.log('AppMetrica: Web not supported. [activate()]', config);
  }
  async reportEvent(options: YAMReportEventOptions): Promise<void> {
    this.log('AppMetrica: Web not supported. [reportEvent()]', options);
  }
  async reportError(options: YAMReportErrorOptions): Promise<void> {
    this.log('AppMetrica: Web not supported. [reportError()]', options);
  }
  async setLocation(location: YAMLocation): Promise<void> {
    this.log('AppMetrica: Web not supported. [setLocation()]', location);
  }
  async setLocationTracking(options: { enabled: boolean; }): Promise<void> {
    this.log('AppMetrica: Web not supported. [setLocationTracking()]', options);
  }
  async showScreenEvent(screen: ECommerceScreen): Promise<void> {
    this.log('AppMetrica: Web not supported. [showScreenEvent()]', screen);
  }
  async showProductCardEvent(options: YAMShowProductCardEventOptions): Promise<void> {
    this.log('AppMetrica: Web not supported. [showProductCardEvent()]', options);
  }
  async showProductDetailsEvent(options: YAMShowProductDetailsEventOptions): Promise<void> {
    this.log('AppMetrica: Web not supported. [showProductDetailsEvent()]', options);
  }
  async addCartItemEvent(cartItem: ECommerceCartItem): Promise<void> {
    this.log('AppMetrica: Web not supported. [addCartItemEvent()]', cartItem);
  }
  async removeCartItemEvent(cartItem: ECommerceCartItem): Promise<void> {
    this.log('AppMetrica: Web not supported. [removeCartItemEvent()]', cartItem);
  }
  async beginCheckoutEvent(order: ECommerceOrder): Promise<void> {
    this.log('AppMetrica: Web not supported. [beginCheckoutEvent()]', order);
  }
  async purchaseEvent(order: ECommerceOrder): Promise<void> {
    this.log('AppMetrica: Web not supported. [purchaseEvent()]', order);
  }
  async setUserProfileId(userProfileId: YAMUserProfileId): Promise<void> {
    this.log('AppMetrica: Web not supported. [setUserProfileId()]', userProfileId);
  }
  async reportUserProfile(userProfile: YAMUserProfile): Promise<void> {
    this.log('AppMetrica: Web not supported. [reportUserProfile()]', userProfile);
  }
}

const AppMetrica = new AppMetricaWeb();

export { AppMetrica };
