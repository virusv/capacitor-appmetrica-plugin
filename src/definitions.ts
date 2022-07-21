declare module '@capacitor/core' {
  interface PluginRegistry {
    AppMetrica: AppMetricaPlugin;
  }
}

export interface AppMetricaPlugin {
  /**
   * Активация метрики
   * @param config 
   */
  activate(config: YAMConfig): Promise<{ activated: boolean; }>;

  /**
   * Отправка простого события
   * 
   * @param options 
   */
  reportEvent(options: YAMReportEventOptions): Promise<void>;

  /**
   * Отправит событие ошибки
   * 
   * @param options 
   */
  reportError(options: YAMReportErrorOptions): Promise<void>;

  /**
   * Сообщить геопозицию
   * 
   * @param location 
   */
  setLocation(location: YAMLocation): Promise<void>;

  /**
   * Вкл/выкл отслеживание местоположения
   * 
   * @param options 
   */
  setLocationTracking(options: { enabled: boolean; }): Promise<void>;

  /**
   * eCommerce: Открытие страницы
   * 
   * @param screen 
   */
  showScreenEvent(screen: ECommerceScreen): Promise<void>;

  /**
   * eCommerce: Просмотр карточки товара
   * 
   * @param options 
   */
  showProductCardEvent(options: YAMShowProductCardEventOptions): Promise<void>;

  /**
   * eCommerce: Просмотр страницы товара
   * 
   * @param options 
   */
  showProductDetailsEvent(options: YAMShowProductDetailsEventOptions): Promise<void>;

  /**
   * eCommerce: Добавление товара в корзину
   * 
   * @param cartItem 
   */
  addCartItemEvent(cartItem: ECommerceCartItem): Promise<void>;

  /**
   * eCommerce: Удаление товара из корзины
   * 
   * @param cartItem 
   */
  removeCartItemEvent(cartItem: ECommerceCartItem): Promise<void>;

  /**
   * eCommerce: Начало оформления заказа
   * 
   * @param order 
   */
  beginCheckoutEvent(order: ECommerceOrder): Promise<void>;

  /**
   * eCommerce: Завершение оформления заказа
   * 
   * @param order 
   */
   purchaseEvent(order: ECommerceOrder): Promise<void>;
}

//#region Share App Merika
export interface YAMConfig {
  apiKey: string;
  appVersion?: string;
  handleFirstActivationAsUpdate?: boolean;
  locationTracking?: boolean;
  sessionTimeout?: number;
  crashReporting?: boolean;
  logs?: boolean;
  location?: YAMLocation;
}

export interface YAMLocation {
  latitude?: number;
  longitude?: number;
  altitude?: number;
  accuracy?: number;
  course?: number;
  speed?: number;
  timestamp?: number;
}

export interface YAMParams {
  [paramName: string]: any;
}

export interface YAMReportEventOptions {
  name: string;
  params?: YAMParams;
}

export interface YAMReportErrorOptions {
  name: string;
  error?: string;
}

export interface YAMShowProductCardEventOptions {
  product: ECommerceProduct;
  screen: ECommerceScreen;
}

export interface YAMShowProductDetailsEventOptions {
  product: ECommerceProduct;
  referrer: ECommerceReferrer;
}
//#region 

//#region ECommerce definitions
export type ECommercePayload = { [key: string]: string; };
export type ECommerceAmount = [value: number, unit: string];

export interface ECommercePrice {
  fiat: ECommerceAmount;
  internalComponents?: ECommerceAmount[];
}

export interface ECommerceReferrer {
  type?: string;
  identifier?: string;
  screen?: ECommerceScreen;
}

export interface ECommerceScreen {
  name?: string;
  searchQuery?: string;
  сategoriesPath?: string[];
  payload?: ECommercePayload;
}

export interface ECommerceProduct {
  sku: string;
  name?: string;
  actualPrice?: ECommercePrice;
  originalPrice?: ECommercePrice;
  categoriesPath?: string[];
  promocodes?: string[];
  payload?: ECommercePayload;
}

export interface ECommerceCartItem {
  product: ECommerceProduct;
  revenue: ECommercePrice;
  quantity: number;
  referrer?: ECommerceReferrer;
}

export interface ECommerceOrder {
  identifier: string;
  cartItems: ECommerceCartItem[];
  payload?: ECommercePayload;
}
//#endregion