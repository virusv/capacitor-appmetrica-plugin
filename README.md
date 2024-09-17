# Плагин Yandex App Metrica для Capacitor

- Поддержка Capacitor 2, 3 - 5, 6.
- Работает на платформах: iOS, Android.
- Поддерживает E-Commerce события
- Поддерживает отправку атрибутов профиля
- Deeplinks (не тестровались)
- Locations (не тестровались)

**Версии App Metrica SDK:**
- iOS: **4.5.0**
- Android: **5.2.0**

## Демо приложение

```bash
cd example

npm install
npm run build

npx cap sync
npx cap open android|ios
```

## Установка

Capacitor 3, 4, 5:
```bash
npm install capacitor-appmetrica-plugin

npx cap sync
```

Для Capacitor 2:
```bash
npm install capacitor-appmetrica-plugin@^2.0.0

npx cap sync
```

### Android

**Настройка геолокации (опционально)**

Подробнее: [ссылка](https://appmetrica.yandex.ru/docs/mobile-sdk-dg/android/about/android-initialize.html#step4)

Открыть файл: `android/app/src/main/AndroidManifest.xml`
```xml
<manifest>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <application>...</application>
</manifest>
```

---------------------------------------

## Использование

Создать новое приложение и получить ключ [здесь](https://appmetrica.yandex.ru/application/new).

### Конфигурация/Активация

```ts
import { AppMetrica } from 'capacitor-appmetrica-plugin';

AppMetrica.activate({
  apiKey: "<API key приложения>",
  logs: true
}).then(() => {
  // Успешная активация
}).catch(() => {
  // Что-то пошло не так
});

```

### Отправка событий

Простое событие:
```ts
AppMetrica.reportEvent({
  name: "Имя события",
  params: {
    "ключ": "значение"
  }
});
```

Событие ошибки:
```ts
AppMetrica.reportError({
  group: "идентификатор_группы",
  message: "Сообщение ошибки",
  parameters: { // В Android данные передаются в виде JSON строки внутри Throwable объекта
    key: "value",
  }
});
```

**WARNING:** Ранее данные события ошибки передавались через поля `name` и `error` - данные значения объявлены как deprecated, будут удалены в следующей версии.

### Отправка атрибутов профиля

Задать идентификатор профиля:
```ts
AppMetrica.setUserProfileId({ id: 'user_id_1' })
```

Отправка атрибутов:
```ts
const userProfile: YAMUserProfile = {
  name: 'Nalivayko Ivan',
  gender: 'male',
  notificationEnabled: true,
  birthDate: { // or { age: 20 }
    year: 2001,
    month: 1,
    day: 1
  }
};

AppMetrica.reportUserProfile(userProfile);
```

### E-Commerce события

<details>
  <summary>Открытие страницы</summary>
  
  ```ts
  const screen: ECommerceScreen = {
    "name": "ProductCardActivity",
    "searchQuery": "даниссимо кленовый сироп",
    "categoriesPath": ["Акции", "Красная цена"],
    "payload": {
        "full_screen": "true",
    }
  };

  AppMetrica.showScreenEvent(screen);
  ```
</details>

<details>
  <summary>Просмотр карточки товара</summary>
  
  ```ts
  const screen: ECommerceScreen = {
    "name": "ProductCardActivity",
    "searchQuery": "даниссимо кленовый сироп",
    "categoriesPath": ["Акции", "Красная цена"],
    "payload": {
        "full_screen": "true",
    }
  };

  const actualPrice: ECommercePrice = {
    "fiat": [4.53, "USD"],
    "internalComponents": [
      [30570000, "wood"],
      [26.89, "iron"],
      [5.1, "gold"]
    ]
  };

  const product: ECommerceProduct = {
    "sku": "779213",
    "name": "Продукт творожный «Даниссимо» 5.9%, 130 г.",
    "actualPrice": actualPrice,
    "originalPrice": {
      "fiat": [5.78, "USD"],
      "internalComponents": [
        [30590000, "wood"],
        [26.92, "iron"],
        [5.5, "gold"],
      ]
    },
    "categoriesPath": ["Продукты", "Молочные продукты", "Йогурты"],
    "promocodes": ["BT79IYX", "UT5412EP"],
    "payload": {
      "full_screen": "true",
    }
  };

  AppMetrica.showProductCardEvent({ product, screen });
  ```
</details>

<details>
  <summary>Просмотр страницы товара</summary>

  ```ts
  const screen: ECommerceScreen = {
    "name": "ProductCardActivity",
    "searchQuery": "даниссимо кленовый сироп",
    "categoriesPath": ["Акции", "Красная цена"],
    "payload": {
        "full_screen": "true",
    }
  };

  const actualPrice: ECommercePrice = {
    "fiat": [4.53, "USD"],
    "internalComponents": [
      [30570000, "wood"],
      [26.89, "iron"],
      [5.1, "gold"]
    ]
  };

  const product: ECommerceProduct = {
    "sku": "779213",
    "name": "Продукт творожный «Даниссимо» 5.9%, 130 г.",
    "actualPrice": actualPrice,
    "originalPrice": {
      "fiat": [5.78, "USD"],
      "internalComponents": [
        [30590000, "wood"],
        [26.92, "iron"],
        [5.5, "gold"],
      ]
    },
    "categoriesPath": ["Продукты", "Молочные продукты", "Йогурты"],
    "promocodes": ["BT79IYX", "UT5412EP"],
    "payload": {
      "full_screen": "true",
    }
  };

  const referrer: ECommerceReferrer = {
    "type": "button",
    "identifier": "76890",
    "screen": screen
  };

  AppMetrica.showProductDetailsEvent({ product, referrer });
  ```
</details>

<details>
  <summary>Добавление или удаление товара из корзины</summary>

  ```ts
  const screen: ECommerceScreen = {
    "name": "ProductCardActivity",
    "searchQuery": "даниссимо кленовый сироп",
    "categoriesPath": ["Акции", "Красная цена"],
    "payload": {
        "full_screen": "true",
    }
  };

  const actualPrice: ECommercePrice = {
    "fiat": [4.53, "USD"],
    "internalComponents": [
      [30570000, "wood"],
      [26.89, "iron"],
      [5.1, "gold"]
    ]
  };

  const product: ECommerceProduct = {
    "sku": "779213",
    "name": "Продукт творожный «Даниссимо» 5.9%, 130 г.",
    "actualPrice": actualPrice,
    "originalPrice": {
      "fiat": [5.78, "USD"],
      "internalComponents": [
        [30590000, "wood"],
        [26.92, "iron"],
        [5.5, "gold"],
      ]
    },
    "categoriesPath": ["Продукты", "Молочные продукты", "Йогурты"],
    "promocodes": ["BT79IYX", "UT5412EP"],
    "payload": {
      "full_screen": "true",
    }
  };

  const referrer: ECommerceReferrer = {
    "type": "button",
    "identifier": "76890",
    "screen": screen
  };

  const addedItem: ECommerceCartItem = {
    product,
    referrer,
    quantity: 1.0,
    revenue: actualPrice
  };

  // Добавление
  AppMetrica.addCartItemEvent(addedItem);

  // Удаление
  AppMetrica.removeCartItemEvent(addedItem);
  ```
</details>

<details>
  <summary>Начало оформления и завершение покупки</summary>

  ```ts
  const screen: ECommerceScreen = {
    "name": "ProductCardActivity",
    "searchQuery": "даниссимо кленовый сироп",
    "categoriesPath": ["Акции", "Красная цена"],
    "payload": {
        "full_screen": "true",
    }
  };

  const actualPrice: ECommercePrice = {
    "fiat": [4.53, "USD"],
    "internalComponents": [
      [30570000, "wood"],
      [26.89, "iron"],
      [5.1, "gold"]
    ]
  };

  const product: ECommerceProduct = {
    "sku": "779213",
    "name": "Продукт творожный «Даниссимо» 5.9%, 130 г.",
    "actualPrice": actualPrice,
    "originalPrice": {
      "fiat": [5.78, "USD"],
      "internalComponents": [
        [30590000, "wood"],
        [26.92, "iron"],
        [5.5, "gold"],
      ]
    },
    "categoriesPath": ["Продукты", "Молочные продукты", "Йогурты"],
    "promocodes": ["BT79IYX", "UT5412EP"],
    "payload": {
      "full_screen": "true",
    }
  };

  const referrer: ECommerceReferrer = {
    "type": "button",
    "identifier": "76890",
    "screen": screen
  };

  const addedItem: ECommerceCartItem = {
    product,
    referrer,
    quantity: 1.0,
    revenue: actualPrice
  };

  const order: ECommerceOrder = {
    "identifier": "88528768",
    "cartItems": [
      addedItem,
      // ...
    ],
    "payload": ["black_friday": "true"]
  };

  // Начало оформления
  AppMetrica.beginCheckoutEvent(order);

  // Завершение покупки
  AppMetrica.purchaseEvent(order);
  ```
</details>


### Местоположение

<details>
  <summary>Установить информацию о местоположении устройства</summary>

  ```ts
  const location: YAMLocation = {
    latitude: 51.660781
    longitude: 39.200296

    // altitude?: number;
    // accuracy?: number;
    // vAccuracy?: number;
    // hAccuracy?: number;
    // course?: number;
    // speed?: number;
    // timestamp?: number;
  };

  AppMetrica.setLocation(location);
  ```
</details>

<details>
  <summary>Включить/отключить отправку информации о местоположении устройства</summary>

  ```ts
  AppMetrica.setLocationTracking({ enabled: true });
  ```
</details>

---------------------------------------

## Документация

#### Android SDK
* [docs](https://appmetrica.yandex.ru/docs/mobile-sdk-dg/android/about/android-initialize.html)
* [ecommerce](https://appmetrica.yandex.ru/docs/data-collection/sending-ecommerce-android.html)

#### iOS SDK
* [docs](https://appmetrica.yandex.ru/docs/mobile-sdk-dg/ios/ios-quickstart.html)
* [ecommerce](https://appmetrica.yandex.ru/docs/data-collection/sending-ecommerce-ios.html)
* [deeplinks](https://appmetrica.yandex.ru/docs/data-collection/tracking-deeplink-ios.html)
