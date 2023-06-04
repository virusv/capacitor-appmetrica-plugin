import {
  AppMetrica,
  ECommerceCartItem,
  ECommerceOrder,
  ECommercePrice,
  ECommerceProduct,
  ECommerceReferrer,
  ECommerceScreen,
  YAMConfig,
  YAMUserProfile
} from 'capacitor-appmetrica-plugin';

type HTMLElementIonCustom = HTMLElement & Record<string, any>;

const app = createDemoApp();

app.initMetrika().then(() => {
  app.initActions();
  console.log("App Metrica -- INIT | Plugin v3.0");
}).catch((e) => {
  // app.initActions(); // DEV
  alert(e);
});

function createDemoApp() {
  // Helpers
  const eClick = function (id, handler) {
    return document.getElementById(id)?.addEventListener('click', handler);
  };

  const toast = function (message, color = 'dark', duration = 2000) {
    const toast = document.createElement('ion-toast') as HTMLElementIonCustom;
    toast.message = message;
    toast.duration = duration;
    toast.color = color;
  
    document.body.appendChild(toast);
    return toast.present();
  };

  const config: YAMConfig = {
    // TODO: Заменить на свой ключ
    apiKey: 'b294b640-42a1-485e-b45f-82cf1dd34e91',

    logs: true,
  };

  function initMetrika() {
    return AppMetrica.activate(config);
  }

  function initActions() {
    const inputEventName = document.getElementById('input_report_event_name') as HTMLElementIonCustom;

    const inputErrorGroup = document.getElementById('input_error_event_group') as HTMLElementIonCustom;
    const inputErrorMessage = document.getElementById('input_error_event_message') as HTMLElementIonCustom;

    eClick('report_event', () => {
      const name = inputEventName.value || 'empty';

      // Простое событие
      AppMetrica.reportEvent({
        name: name,
        params: { paramOne: 'example' }
      })
        .then(() => { toast('Событие отправлено', 'success'); })
        .catch(e => { toast(e, 'danger'); })
      ;
    });

    eClick('report_error', () => {
      const group = inputErrorGroup.value || 'empty';
      const message = inputErrorMessage.value || undefined;

      // Событие ошибки
      AppMetrica.reportError({
        group: group,
        message: message,
        parameters: {
          key: "value",
        }
      })
        .then(() => { toast('Ошибка отправлена', 'success'); })
        .catch(e => { toast(e, 'danger'); })
      ;
    });

    eClick('user_profile_report', () => {
      // Установка идентификатора профиля пользователя
      AppMetrica.setUserProfileId({ id: 'user_id_1' }); // async

      const userProfile: YAMUserProfile = {
        name: 'Nalivayko Ivan',
        gender: 'male',
        notificationEnabled: true,
        birthDate: {
          year: 2001,
          month: 1,
          day: 1
        }
      };

      // Отправка атрибутов профиля
      AppMetrica.reportUserProfile(userProfile)
        .then(() => { toast('Данные профиля отправлены', 'success'); })
        .catch(e => { toast(e, 'danger'); })
      ;
    });

    const inputLocationLatitude = document.getElementById('input_location_latitude') as HTMLElementIonCustom;
    const inputLocationLongitude = document.getElementById('input_location_longitude') as HTMLElementIonCustom;

    eClick('set_location', function () {
      const latitude  = Number.parseFloat(inputLocationLatitude.value)  || 51.661660;
      const longitude = Number.parseFloat(inputLocationLongitude.value) || 39.200050;
      
      AppMetrica.setLocation({
        latitude: latitude,
        longitude: longitude,
      })
        .then(() => { toast('Координаты установлены', 'success'); })
        .catch(e => { toast(e, 'danger'); })
      ;
    });

    // 51.661660, 39.200050

    // -------------

    eClick('view_screen', () => {
      const screen = getDemoScreen('home');

      // eCommerce: Открытие страницы
      AppMetrica.showScreenEvent(screen)
        .then(() => { toast('Событие отправлено', 'success'); })
        .catch(e => { toast(e, 'danger'); })
      ;
    });

    eClick('show_product_card', () => {
      const screen = getDemoScreen('screen-101');
      const product = getDemoProduct('101');

      // eCommerce: Просмотр карточки товара
      AppMetrica.showProductCardEvent({
        product: product,
        screen: screen,
      })
        .then(() => { toast('Событие отправлено', 'success'); })
        .catch(e => { toast(e, 'danger'); })
      ;
    });

    eClick('show_product_details', () => {
      const screen = getDemoScreen('screen-102');
      const product = getDemoProduct('102', 1000, 600, true);
      const referrer = getDemoReferrer('button-102', screen);

      // eCommerce: Просмотр страницы товара
      AppMetrica.showProductDetailsEvent({
        product: product,
        referrer: referrer,
      })
        .then(() => { toast('Событие отправлено', 'success'); })
        .catch(e => { toast(e, 'danger'); })
      ;
    });

    eClick('add_cart_item', () => {
      const screen = getDemoScreen('screen-103');
      const referrer = getDemoReferrer('button-103', screen);
      const cartItem = getDemoCartItem('102', 1, 800, 400, referrer);

      // eCommerce: Добавление товара в корзину
      AppMetrica.addCartItemEvent(cartItem)
        .then(() => { toast('Событие отправлено [+1]', 'success'); })
        .catch(e => { toast(e, 'danger'); })
      ;
    });

    eClick('remove_cart_item', () => {
      const screen = getDemoScreen('screen-104');
      const referrer = getDemoReferrer('button-104', screen);
      const cartItem = getDemoCartItem('102', 1, 800, 400, referrer);

      // eCommerce: Добавление товара в корзину
      AppMetrica.removeCartItemEvent(cartItem)
        .then(() => { toast('Событие отправлено [-1]', 'success'); })
        .catch(e => { toast(e, 'danger'); })
      ;
    });

    let gOrder: ECommerceOrder|null = null;
    let gOrderIdPrefix = Date.now() + '_';
    let gOrderCounter = 0;
    const gOrderViewData = document.getElementById('order_data') as HTMLDivElement;

    eClick('begin_checkout', () => {
      gOrder = getDemoOrder(gOrderIdPrefix + String(++gOrderCounter));
      gOrderViewData.innerHTML = JSON.stringify(gOrder, null, '  ');

      // eCommerce: Начало оформления заказа
      AppMetrica.beginCheckoutEvent(gOrder)
        .then(() => { toast('Событие "beginCheckoutEvent" отправлено', 'success'); })
        .catch(e => { toast(e, 'danger'); })
      ;
    });

    eClick('purchase', () => {
      const purchaseOrder = gOrder || getDemoOrder(gOrderIdPrefix + String(++gOrderCounter));
      gOrderViewData.innerHTML = JSON.stringify(purchaseOrder, null, '  ');
      gOrder = null;

      // eCommerce: Завершение оформления заказа
      AppMetrica.purchaseEvent(purchaseOrder)
        .then(() => { toast('Событие "purchaseEvent" отправлено', 'success'); })
        .catch(e => { toast(e, 'danger'); })
      ;
    });
  }

  function getDemoPrice(fiat: number, isComponents = false): ECommercePrice {
    let price: ECommercePrice = {
      fiat: [fiat, 'RUB'],
    };
  
    if (isComponents) {
      price.internalComponents = [
        [fiat + 1, 'RUB-1'],
        [fiat - 1, 'RUB+1'],
      ];
    }
  
    return price;
  }
  
  function getDemoProduct(id: string, actualPrice = 1000, originalPrice = 600, isComponents = false): ECommerceProduct {
    return {
      sku: String(id),
      name: "Тестовый товар #" + id,
      actualPrice: getDemoPrice(actualPrice, isComponents),
      originalPrice: getDemoPrice(originalPrice, isComponents),
      categoriesPath: [
        'Корневая категория',
        'Подкатегория'
      ],
      promocodes: ['PROMOCODE_1', 'PROMO_2'],
      payload: {
        key1: 'value1',
        key2: 'value2'
      },
    };
  }
  
  function getDemoScreen(name: string): ECommerceScreen {
    return {
      name: name,
      searchQuery: name + ' - купить дешего',
      сategoriesPath: ['Корневая категория'],
      payload: { demo: 'true' },
    };
  }
  
  function getDemoReferrer(id: string, screen: ECommerceScreen): ECommerceReferrer {
    return {
      type: 'button',
      identifier: id,
      screen: screen,
    };
  }
  
  function getDemoCartItem(
    id: string,
    quantity = 1,
    actualPrice = 1000,
    originalPrice = 600,
    referrer?: ECommerceReferrer,
  ): ECommerceCartItem {
    return {
      product: getDemoProduct(id, actualPrice, originalPrice),
      revenue: getDemoPrice(actualPrice - originalPrice),
      quantity: quantity,
      referrer: referrer
    };
  }
  
  function getDemoOrder(id: string, itemsCount = 10): ECommerceOrder {
    let cartItems: ECommerceCartItem[] = [];
  
    let screen = getDemoScreen('Придуманная страница товара 1');
    let referrer = getDemoReferrer('order-screen', screen);
  
    for (let i = 0; i < itemsCount; ++i) {
      cartItems.push(
        getDemoCartItem(
          String((i + 1) * 10),
          Math.round(Math.random()),
          1000 + 10 * i,
          Math.round((1000 + 10 * i) * 0.6),
          referrer
        )
      );
    }
  
    return {
      identifier: 'demo_order_' + id,
      cartItems: cartItems,
      payload: {
        'test': 'order'
      }
    };
  }

  return {
    initMetrika,
    initActions,
  };
}

