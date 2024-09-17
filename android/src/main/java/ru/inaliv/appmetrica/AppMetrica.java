package ru.inaliv.appmetrica;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.app.Activity;

import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.yandex.metrica.ecommerce.ECommerceCartItem;
import com.yandex.metrica.ecommerce.ECommerceEvent;
import com.yandex.metrica.ecommerce.ECommerceOrder;
import com.yandex.metrica.ecommerce.ECommerceProduct;
import com.yandex.metrica.ecommerce.ECommerceReferrer;
import com.yandex.metrica.ecommerce.ECommerceScreen;
import com.yandex.metrica.profile.UserProfile;

import org.json.JSONException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CapacitorPlugin(name = "AppMetrica")
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
    public void activate(final PluginCall call) {
        final YandexMetricaConfig config;
        try {
            config = Converter.toConfig(call.getData());
        } catch (JSONException e) {
            call.reject("Failed to activate metric: " + e.getMessage());
            return;
        }

        YandexMetrica.activate(getContext(), config);

        synchronized (mLock) {
            if (mAppMetricaActivated == false) {
                YandexMetrica.reportAppOpen(getActivity());

                if (mActivityPaused == false) {
                    YandexMetrica.resumeSession(getActivity());
                }
            }

            mAppMetricaActivated = true;

            call.resolve();
        }
    }

    /**
     * Отправляет событие в App метрику
     *
     * @param call
     */
    @PluginMethod
    public void reportEvent(final PluginCall call) {
        final String evName = call.getString("name");

        if (evName == null || evName.isBlank()) {
            call.reject("Undefined or empty event name");
            return;
        }

        if (call.hasOption("params")) {
            final JSObject evParams = call.getObject("params", new JSObject());
            YandexMetrica.reportEvent(evName, evParams.toString());
        }
        else {
            YandexMetrica.reportEvent(evName);
        }

        call.resolve();
    }

    /**
     * Отправляет ошибку в App метрику
     * @param call
     */
    @PluginMethod
    public void reportError(final PluginCall call) {
        final String group = call.hasOption("group")
                ? call.getString("group")
                : call.getString("name"); // Legacy

        String message = call.hasOption("message")
                ? call.getString("message")
                : call.getString("error"); // Legacy

        Throwable errorThrowable = null;

        if (call.hasOption("parameters")) {
            errorThrowable = new Throwable(
                call.getObject("parameters").toString()
            );
        }

        if (group != null) {
            YandexMetrica.reportError(group, message, errorThrowable);
        } else {
            if (message == null) {
                message = "undefined";
            }

            YandexMetrica.reportError(message, errorThrowable);
        }

        call.resolve();
    }

    /**
     * Задать объект Location для метрики
     * @param call
     * @throws JSONException
     */
    @PluginMethod
    public void setLocation(final PluginCall call) {
        final JSObject locationObj = call.getData();

        try {
            final Location location = Converter.toLocation(locationObj);
            YandexMetrica.setLocation(location);

            call.resolve();
        } catch (JSONException e) {
            call.reject(e.getMessage());
        }
    }

    /**
     * Отслеживание местоположения (вкл/выкл)
     * @param call
     */
    @PluginMethod
    public void setLocationTracking(final PluginCall call) {
        final boolean enabled = Boolean.TRUE.equals(call.getBoolean("enabled", true));
        YandexMetrica.setLocationTracking(enabled);

        call.resolve();
    }

    //-------------------- ECOMMERCE ------------------------------------------

    /**
     * eCommerce: Открытие страницы
     *
     * @param call
     */
    @PluginMethod
    public void showScreenEvent(final PluginCall call) {
        try {
            ECommerceScreen screen = Converter.toECommerceScreen(call.getData());

            ECommerceEvent showScreenEvent = ECommerceEvent.showScreenEvent(screen);
            YandexMetrica.reportECommerce(showScreenEvent);

            call.resolve();
        } catch (JSONException e) {
            call.reject(e.getMessage());
        }
    }

    /**
     * eCommerce: Просмотр карточки товара
     *
     * @param call
     */
    @PluginMethod
    public void showProductCardEvent(final PluginCall call) {
        try {
            ECommerceProduct product = Converter.toECommerceProduct(call.getObject("product"));
            ECommerceScreen screen = Converter.toECommerceScreen(call.getObject("screen"));

            ECommerceEvent showProductCardEvent = ECommerceEvent.showProductCardEvent(product, screen);
            YandexMetrica.reportECommerce(showProductCardEvent);

            call.resolve();
        } catch (JSONException e) {
            call.reject(e.getMessage());
        }
    }

    /**
     * eCommerce: Просмотр страницы товара
     *
     * @param call
     */
    @PluginMethod
    public void showProductDetailsEvent(final PluginCall call) {
        try {
            ECommerceProduct product = Converter.toECommerceProduct(call.getObject("product"));
            ECommerceReferrer referrer = Converter.toECommerceReferrer(call.getObject("referrer"));

            ECommerceEvent showProductDetailsEvent = ECommerceEvent.showProductDetailsEvent(product, referrer);
            YandexMetrica.reportECommerce(showProductDetailsEvent);

            call.resolve();
        } catch (JSONException e) {
            call.reject(e.getMessage());
        }
    }

    /**
     * eCommerce: Добавление товара в корзину
     *
     * @param call
     */
    @PluginMethod
    public void addCartItemEvent(final PluginCall call) {
        try {
            ECommerceCartItem cartItem = Converter.toECommerceCartItem(call.getData());

            ECommerceEvent addCartItemEvent = ECommerceEvent.addCartItemEvent(cartItem);
            YandexMetrica.reportECommerce(addCartItemEvent);

            call.resolve();
        } catch (JSONException e) {
            call.reject(e.getMessage());
        }
    }

    /**
     * eCommerce: Удаление товара из корзины
     *
     * @param call
     */
    @PluginMethod
    public void removeCartItemEvent(final PluginCall call) {
        try {
            ECommerceCartItem cartItem = Converter.toECommerceCartItem(call.getData());

            ECommerceEvent removeCartItemEvent = ECommerceEvent.removeCartItemEvent(cartItem);
            YandexMetrica.reportECommerce(removeCartItemEvent);

            call.resolve();
        } catch (JSONException e) {
            call.reject(e.getMessage());
        }
    }

    /**
     * eCommerce: Начало оформления заказа
     *
     * @param call
     */
    @PluginMethod
    public void beginCheckoutEvent(final PluginCall call) {
        try {
            ECommerceOrder order = Converter.toECommerceOrder(call.getData());

            ECommerceEvent beginCheckoutEvent = ECommerceEvent.beginCheckoutEvent(order);
            YandexMetrica.reportECommerce(beginCheckoutEvent);

            call.resolve();
        } catch (JSONException e) {
            call.reject(e.getMessage());
        }
    }

    /**
     * eCommerce: Завершение оформления заказа
     *
     * @param call
     */
    @PluginMethod
    public void purchaseEvent(final PluginCall call) {
        try {
            ECommerceOrder order = Converter.toECommerceOrder(call.getData());

            ECommerceEvent purchaseEvent = ECommerceEvent.purchaseEvent(order);
            YandexMetrica.reportECommerce(purchaseEvent);

            call.resolve();
        } catch (JSONException e) {
            call.reject(e.getMessage());
        }
    }

    //-------------------- USER PROFILE ---------------------------------------

    /**
     * User Profile: Отправка идентификатора профиля
     *
     * @param call
     */
    @PluginMethod
    public void setUserProfileId(final PluginCall call) {
        if (call.hasOption("id")) {
            YandexMetrica.setUserProfileID(call.getString("id"));

            call.resolve();
        } else {
            call.reject("Не передан обязательный идентификатор профиля");
        }
    }

    /**
     * User Profile: Отправка атрибутов профиля
     *
     * @param call
     */
    @PluginMethod
    public void reportUserProfile(final PluginCall call) {
        try {
            UserProfile userProfile = Converter.toUserProfile(call.getData());
            YandexMetrica.reportUserProfile(userProfile);

            call.resolve();
        } catch (JSONException e) {
            call.reject(e.getMessage());
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
                YandexMetrica.resumeSession(getActivity());
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
                YandexMetrica.pauseSession(getActivity());
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
                    YandexMetrica.reportAppOpen(getActivity());
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
