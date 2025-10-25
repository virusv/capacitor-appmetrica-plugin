package ru.inaliv.appmetrica;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.app.Activity;

import android.location.Location;
import com.getcapacitor.*;
import com.getcapacitor.annotation.CapacitorPlugin;

import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ecommerce.*;
import io.appmetrica.analytics.profile.UserProfile;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

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
        final AppMetricaConfig config;
        try {
            config = Converter.toConfig(call.getData());
        } catch (JSONException e) {
            call.reject("Failed to activate metric: " + e.getMessage());
            return;
        }

        io.appmetrica.analytics.AppMetrica.activate(getContext(), config);

        synchronized (mLock) {
            if (!mAppMetricaActivated) {
                io.appmetrica.analytics.AppMetrica.reportAppOpen(getActivity());

                if (!mActivityPaused) {
                    io.appmetrica.analytics.AppMetrica.resumeSession(getActivity());
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
            io.appmetrica.analytics.AppMetrica.reportEvent(evName, evParams.toString());
        }
        else {
            io.appmetrica.analytics.AppMetrica.reportEvent(evName);
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
            io.appmetrica.analytics.AppMetrica.reportError(group, message, errorThrowable);
        } else {
            if (message == null) {
                message = "undefined";
            }

            io.appmetrica.analytics.AppMetrica.reportError(message, errorThrowable);
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
            io.appmetrica.analytics.AppMetrica.setLocation(location);

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
        io.appmetrica.analytics.AppMetrica.setLocationTracking(enabled);

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
            io.appmetrica.analytics.AppMetrica.reportECommerce(showScreenEvent);

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
            io.appmetrica.analytics.AppMetrica.reportECommerce(showProductCardEvent);

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
            io.appmetrica.analytics.AppMetrica.reportECommerce(showProductDetailsEvent);

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
            io.appmetrica.analytics.AppMetrica.reportECommerce(addCartItemEvent);

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
            io.appmetrica.analytics.AppMetrica.reportECommerce(removeCartItemEvent);

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
            io.appmetrica.analytics.AppMetrica.reportECommerce(beginCheckoutEvent);

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
            io.appmetrica.analytics.AppMetrica.reportECommerce(purchaseEvent);

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
            io.appmetrica.analytics.AppMetrica.setUserProfileID(call.getString("id"));

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
            io.appmetrica.analytics.AppMetrica.reportUserProfile(userProfile);

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
                io.appmetrica.analytics.AppMetrica.resumeSession(getActivity());
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
                io.appmetrica.analytics.AppMetrica.pauseSession(getActivity());
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
                    io.appmetrica.analytics.AppMetrica.reportAppOpen(getActivity());
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
