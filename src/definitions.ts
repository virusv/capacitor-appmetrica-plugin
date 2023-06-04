export interface AppMetricaPlugin {
  /**
   * Активация метрики
   * @param config 
   */
  activate(config: YAMConfig): Promise<void>;

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

  /**
   * User Profile: Отправка идентификатора профиля
   * 
   * @param userProfileId 
   */
  setUserProfileId(userProfileId: YAMUserProfileId): Promise<void>;

  /**
   * User Profile: Отправка атрибутов профиля
   * 
   * @param userProfile 
   */
  reportUserProfile(userProfile: YAMUserProfile): Promise<void>;
}

//#region Share App Merika
export interface YAMConfig {
  /**
   * API key приложения
   */
  apiKey: string;

  /**
   * Версия приложения
   */
  appVersion?: string;

  /**
   * Определяет первый запуск приложения как обновление
   */
  handleFirstActivationAsUpdate?: boolean;

  /**
   * Включает/отключает отправку информации о местоположении устройства
   */
  locationTracking?: boolean;

  /**
   * Задает длительность тайм-аута сессии в секундах
   */
  sessionTimeout?: number;

  /**
   * Сбор и отправка информации об аварийных остановках приложения
   */
  crashReporting?: boolean;

  /**
   * Сбор и отправка информации об нативных аварийных остановках приложения (по умолчанию активен)
   * Только Android!
   */
  nativeCrashReporting?: boolean;

  /**
   * Включает/отключает логирование работы библиотеки
   */
  logs?: boolean;

  /**
   * Устанавливает собственную информацию о местоположении устройства
   */
  location?: YAMLocation;

  //--- Не реализованы ---
  /**
   * Определяет тип приложения как «детский», чтобы соответствовать правилам проверки детских приложений.
   * Если опция включена, AppMetrica SDK не отправляет рекламные идентификаторы и информацию о местоположении.
   */
  // appForKids?: boolean;

  /**
   * Признак автоматического сбора и отправки информации о запуске приложения через deeplink
   */
  // appOpenTrackingEnabled?: boolean;

  /**
   * Определяет инициализацию AppMetrica как начало пользовательской сессии
   */
  // handleActivationAsSessionStart?: boolean;

  /**
   * Максимальное число отчетов об ошибках, которое хранится во внутренней БД
   */
  // maxReportsInDatabaseCount?: number; // UInt

  /**
   * Устанавливает объект класса YamPreloadInfo для отслеживания предустановленных приложений.
   */
  // preloadInfo?: unknown; // YamPreloadInfo (не реализованная структура)

  /**
   * Включает/выключает автоматический сбор информации об In-App покупках
   */
  // revenueAutoTrackingEnabled?: boolean;

  /**
   * Включает/отключает автоматическое отслеживание жизненного цикла приложений
   */
  // sessionsAutoTracking?: boolean;

  /**
   * Включает/отключает отправку данных на сервер AppMetrica
   */
  // statisticsSending?: boolean;

  /**
   * Задает идентификатор пользовательского профиля при активации.
   */
  // userProfileId?: string;
}

export interface YAMLocation {
  latitude?: number;
  longitude?: number;
  altitude?: number;
  accuracy?: number;
  vAccuracy?: number;
  hAccuracy?: number;
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
  /** Идентификатор группы */
  group: string;

  /** Сообщение ошибки */
  message?: string;

  /** Дополнительные параметры */
  parameters?: {
    [ptop: string]: string;
  };
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
export type ECommerceAmount = [number, string]; // value: number, unit: string

export interface ECommercePrice {
  /**
   * Стоимость в фиатных деньгах
   */
  fiat: ECommerceAmount;

  /**
   * Стоимость внутренних компонентов — суммы во внутренней валюте.
   * Допустимый размер: до 30 элементов
   */
  internalComponents?: ECommerceAmount[];
}

export interface ECommerceReferrer {
  /**
   * Тип источника перехода — тип объекта, с которого выполняется переход.
   * Например: «button», «banner», «href».
   * Допустимый размер: до 100 символов.
   */
  type?: string;

  /**
   * Идентификатор источника перехода. Допустимый размер: до 2048 символов
   */
  identifier?: string;

  /**
   * Экран источника перехода — экран, с которого выполняется переход
   */
  screen?: ECommerceScreen;
}

export interface ECommerceScreen {
  /**
   * Название экрана. Допустимые размеры: до 100 символов
   */
  name?: string;

  /**
   * Поисковый запрос. Допустимый размер: до 1000 символов
   */
  searchQuery?: string;

  /**
   * Путь к экрану по категориям.
   * 
   * Допустимые размеры:
   * - до 10 элементов;
   * - размер одного элемента до 100 символов.
   */
  сategoriesPath?: string[];

  /**
   * Дополнительная информация об экране.
   * 
   * Допустимые размеры:
   * - общий размер payload: до 20 КБ;
   * - размер key: до 100 символов;
   * - размер value: до 1000 символов.
   */
  payload?: ECommercePayload;
}

export interface ECommerceProduct {
  /**
   * Артикул товара. Допустимый размер: до 100 символов.
   */
  sku: string;

  /**
   * Название товара. Допустимый размер: до 1000 символов.
   */
  name?: string;

  /**
   * Фактическая цена товара — цена после применения всех скидок и промокодов.
   */
  actualPrice?: ECommercePrice;

  /**
   * Первоначальная цена товара
   */
  originalPrice?: ECommercePrice;

  /**
   * Путь к товару по категориям.
   * Допустимые размеры:
   * - до 10 элементов;
   * - размер одного элемента до 100 символов.
   */
  categoriesPath?: string[];

  /**
   * Список промокодов, которые применяются к товару.
   * Допустимые размеры:
   * - до 20 элементов;
   * - длина промокода до 100 символов.
   */
  promocodes?: string[];

  /**
   * Дополнительная информация о товаре.
   * 
   * Допустимые размеры:
   * - общий размер payload: до 20 КБ;
   * - размер key: до 100 символов;
   * - размер value: до 1000 символов.
   */
  payload?: ECommercePayload;
}

/**
 * Объект с информацией о товаре в корзине.
 */
export interface ECommerceCartItem {
  /**
   * Товар
   */
  product: ECommerceProduct;

  /**
   * Общая цена товара в корзине. Она учитывает количество, применяемые скидки и т.д.
   */
  revenue: ECommercePrice;

  /**
   * Количество
   */
  quantity: number;

  /**
   * Источника перехода в корзину
   */
  referrer?: ECommerceReferrer;
}

export interface ECommerceOrder {
  /**
   * Идентификатор заказа. Допустимый размер: до 100 символов
   */
  identifier: string;

  /**
   * Список товаров в корзине
   */
  cartItems: ECommerceCartItem[];

  /**
   * Дополнительная информация о заказе.
   * 
   * Допустимые размеры:
   * - общий размер payload: до 20 КБ;
   * - размер key: до 100 символов;
   * - размер value: до 1000 символов.
   */
  payload?: ECommercePayload;
}
//#endregion

//#region User Profile
export type YAMGenderType = 'male'|'female'|'other';
export interface YAMUserProfileBirthDate {
  year: number;
  month?: number;
  day?: number;
}

export interface YAMUserProfileAge {
  age: number;
}

export interface YAMUserProfileId {
  id: string;
}

export interface YAMUserProfile {
  name?: string;
  gender?: YAMGenderType;
  notificationEnabled?: boolean;
  birthDate?: YAMUserProfileBirthDate|YAMUserProfileAge;
}
//#endregion