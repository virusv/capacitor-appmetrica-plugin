package ru.inaliv.appmetrica;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.yandex.metrica.ecommerce.ECommerceCartItem;
import com.yandex.metrica.ecommerce.ECommerceEvent;
import com.yandex.metrica.ecommerce.ECommerceOrder;
import com.yandex.metrica.ecommerce.ECommerceProduct;
import com.yandex.metrica.ecommerce.ECommerceReferrer;
import com.yandex.metrica.ecommerce.ECommerceScreen;

import org.json.JSONException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NativePlugin
public class AppMetrica extends Plugin {
    private final Object mLock = new Object();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private boolean mActivityPaused = true;
    private boolean mAppMetricaActivated = false;

    /**
     * Активация метрики
     *
     * @param call
     */
    @PluginMethod
    public void activate(PluginCall call) {
        final YandexMetricaConfig config;
        try {
            config = Converter.toConfig(call.getData());
        } catch (JSONException e) {
            call.error("Failed to activate metric: " + e.getMessage());
            return;
        }
        final Context context = getBridge().getActivity().getApplicationContext();

        YandexMetrica.activate(context, config);

        synchronized (mLock) {
            if (mAppMetricaActivated == false) {
                YandexMetrica.reportAppOpen(bridge.getActivity());

                if (mActivityPaused == false) {
                    YandexMetrica.resumeSession(bridge.getActivity());
                }
            }

            mAppMetricaActivated = true;

            call.success();
        }
    }

    /**
     * Отправляет событие в App метрику
     *
     * @param call
     */
    @PluginMethod
    public void reportEvent(PluginCall call) {
        final String evName = call.getString("name");

        if (call.hasOption("params")) {
            final JSObject evParams = call.getObject("params", new JSObject());
            YandexMetrica.reportEvent(evName, evParams.toString());
        }
        else {
            YandexMetrica.reportEvent(evName);
        }

        call.success();
    }

    /**
     * Отправляет ошибку в App метрику
     * @param call
     */
    @PluginMethod
    public void reportError(PluginCall call) {
        final String errorName = call.getString("name");

        Throwable errorThrowable = null;

        if (call.hasOption("error")) {
            errorThrowable = new Throwable(
                call.getString("error")
            );
        }

        YandexMetrica.reportError(errorName, errorThrowable);

        call.success();
    }

    /**
     * Задать объект Location для метрики
     * @param call
     * @throws JSONException
     */
    @PluginMethod
    public void setLocation(PluginCall call) {
        final JSObject locationObj = call.getData();

        try {
            final Location location = Converter.toLocation(locationObj);
            YandexMetrica.setLocation(location);
            call.success();
        } catch (JSONException e) {
            call.error(e.getMessage());
        }
    }

    /**
     * Отслеживание местоположения (вкл/выкл)
     * @param call
     */
    @PluginMethod
    public void setLocationTracking(PluginCall call) {
        final boolean enabled = call.getBoolean("enabled", true);
        YandexMetrica.setLocationTracking(enabled);
        call.success();
    }

    //-------------------- ECOMMERCE ------------------------------------------

    /**
     * eCommerce: Открытие страницы
     *
     * @param call
     */
    @PluginMethod
    public void showScreenEvent(PluginCall call) {
        try {
            ECommerceScreen screen = Converter.toECommerceScreen(call.getData());

            ECommerceEvent showScreenEvent = ECommerceEvent.showScreenEvent(screen);
            YandexMetrica.reportECommerce(showScreenEvent);

            call.success();
        } catch (JSONException e) {
            call.error(e.getMessage());
        }
    }

    /**
     * eCommerce: Просмотр карточки товара
     *
     * @param call
     */
    @PluginMethod
    public void showProductCardEvent(PluginCall call) {
        try {
            ECommerceProduct product = Converter.toECommerceProduct(call.getObject("product"));
            ECommerceScreen screen = Converter.toECommerceScreen(call.getObject("screen"));

            ECommerceEvent showProductCardEvent = ECommerceEvent.showProductCardEvent(product, screen);
            YandexMetrica.reportECommerce(showProductCardEvent);

            call.success();
        } catch (JSONException e) {
            call.error(e.getMessage());
        }
    }

    /**
     * eCommerce: Просмотр страницы товара
     *
     * @param call
     */
    @PluginMethod
    public void showProductDetailsEvent(PluginCall call) {
        try {
            ECommerceProduct product = Converter.toECommerceProduct(call.getObject("product"));
            ECommerceReferrer referrer = Converter.toECommerceReferrer(call.getObject("referrer"));

            ECommerceEvent showProductDetailsEvent = ECommerceEvent.showProductDetailsEvent(product, referrer);
            YandexMetrica.reportECommerce(showProductDetailsEvent);

            call.success();
        } catch (JSONException e) {
            call.error(e.getMessage());
        }
    }

    /**
     * eCommerce: Добавление товара в корзину
     *
     * @param call
     */
    @PluginMethod
    public void addCartItemEvent(PluginCall call) {
        try {
            ECommerceCartItem cartItem = Converter.toECommerceCartItem(call.getData());

            ECommerceEvent addCartItemEvent = ECommerceEvent.addCartItemEvent(cartItem);
            YandexMetrica.reportECommerce(addCartItemEvent);

            call.success();
        } catch (JSONException e) {
            call.error(e.getMessage());
        }
    }

    /**
     * eCommerce: Удаление товара из корзины
     *
     * @param call
     */
    @PluginMethod
    public void removeCartItemEvent(PluginCall call) {
        try {
            ECommerceCartItem cartItem = Converter.toECommerceCartItem(call.getData());

            ECommerceEvent removeCartItemEvent = ECommerceEvent.removeCartItemEvent(cartItem);
            YandexMetrica.reportECommerce(removeCartItemEvent);

            call.success();
        } catch (JSONException e) {
            call.error(e.getMessage());
        }
    }

    /**
     * eCommerce: Начало оформления заказа
     *
     * @param call
     */
    @PluginMethod
    public void beginCheckoutEvent(PluginCall call) {
        try {
            ECommerceOrder order = Converter.toECommerceOrder(call.getData());

            ECommerceEvent beginCheckoutEvent = ECommerceEvent.beginCheckoutEvent(order);
            YandexMetrica.reportECommerce(beginCheckoutEvent);

            call.success();
        } catch (JSONException e) {
            call.error(e.getMessage());
        }
    }

    /**
     * eCommerce: Завершение оформления заказа
     *
     * @param call
     */
    @PluginMethod
    public void purchaseEvent(PluginCall call) {
        try {
            ECommerceOrder order = Converter.toECommerceOrder(call.getData());

            ECommerceEvent purchaseEvent = ECommerceEvent.purchaseEvent(order);
            YandexMetrica.reportECommerce(purchaseEvent);

            call.success();
        } catch (JSONException e) {
            call.error(e.getMessage());
        }
    }

    //-------------------- SERVICES -------------------------------------------
    /**
     * Возобновить сессию
     */
    private void onResumeSession() {
        synchronized (mLock) {
            mActivityPaused = false;
            if (mAppMetricaActivated) {
                YandexMetrica.resumeSession(getBridge().getActivity());
            }
        }
    }

    /**
     * Поставить на паузу сессию
     */
    private void onPauseSession() {
        synchronized (mLock) {
            mActivityPaused = true;
            if (mAppMetricaActivated) {
                YandexMetrica.pauseSession(getBridge().getActivity());
            }
        }
    }

    /**
     * Handle onNewIntent
     * @param intent
     */
    protected void handleOnNewIntent(Intent intent) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mAppMetricaActivated) {
                    YandexMetrica.reportAppOpen(getBridge().getActivity());
                }
            }
        });
    }

    /**
     * Handle onResume
     */
    @Override
    protected void handleOnResume() {
        onResumeSession();
    }

    /**
     * Handle onPause
     */
    @Override
    protected void handleOnPause() {
        onPauseSession();
    }
}
