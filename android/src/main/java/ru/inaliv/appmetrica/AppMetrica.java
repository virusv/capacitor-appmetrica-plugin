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
     * Сконвертирует конфигурацию JSObject в объект для App Метрики
     *
     * @param config
     * @return
     */
    public static YandexMetricaConfig toConfig(final JSObject config) throws JSONException {
        final String apiKey = config.getString("apiKey");
        final YandexMetricaConfig.Builder builder = YandexMetricaConfig.newConfigBuilder(apiKey);

        if (config.has("handleFirstActivationAsUpdate")) {
            builder.handleFirstActivationAsUpdate(config.getBool("handleFirstActivationAsUpdate"));
        }
        if (config.has("locationTracking")) {
            builder.withLocationTracking(config.getBool("locationTracking"));
        }
        if (config.has("sessionTimeout")) {
            builder.withSessionTimeout(config.getInteger("sessionTimeout"));
        }
        if (config.has("crashReporting")) {
            builder.withCrashReporting(config.getBool("crashReporting"));
        }
        if (config.has("appVersion")) {
            builder.withAppVersion(config.getString("appVersion"));
        }
        if (config.optBoolean("logs", false)) {
            builder.withLogs();
        }
        if (config.has("location")) {
            final Location location = toLocation(config.getJSObject("location"));
            builder.withLocation(location);
        }

        return builder.build();
    }

    /**
     * Конвертирует конфигурацию JSObject в объект Location для App Метрики
     *
     * @param location
     * @return
     * @throws JSONException
     */
    public static Location toLocation(final JSObject location) throws JSONException {
        final Location yandexLocation = new Location("Custom");

        if (location.has("latitude")) {
            yandexLocation.setLatitude(location.getDouble("latitude"));
        }
        if (location.has("longitude")) {
            yandexLocation.setLongitude(location.getDouble("longitude"));
        }
        if (location.has("altitude")) {
            yandexLocation.setAltitude(location.getDouble("altitude"));
        }
        if (location.has("accuracy")) {
            yandexLocation.setAccuracy((float) location.getDouble("accuracy"));
        }
        if (location.has("course")) {
            yandexLocation.setBearing((float) location.getDouble("course"));
        }
        if (location.has("speed")) {
            yandexLocation.setSpeed((float) location.getDouble("speed"));
        }
        if (location.has("timestamp")) {
            yandexLocation.setTime(location.getLong("timestamp"));
        }

        return yandexLocation;
    }

    /**
     * Активация метрики
     *
     * @param call
     */
    @PluginMethod
    private void activate(PluginCall call) {
        final YandexMetricaConfig config;
        try {
            config = toConfig(call.getData());
        } catch (JSONException e) {
            call.error("Не удалось активировать метрику: " + e.getMessage());
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
        }

        JSObject res = new JSObject();
        res.put("activated", mAppMetricaActivated);
        call.success(res);
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
    private void reportError(PluginCall call) {
        final String errorName = call.getString("name");
        final String errorReason = call.getString("error", "Undefined Error");

        Throwable errorThrowable = new Throwable(errorReason);
        YandexMetrica.reportError(errorName, errorThrowable);

        call.success();
    }

    /**
     * Задать объект Location для метрики
     * @param call
     * @throws JSONException
     */
    @PluginMethod
    private void setLocation(PluginCall call) {
        final JSObject locationObj = call.getData();

        try {
            final Location location = toLocation(locationObj);
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
    private void setLocationTracking(PluginCall call) {
        final boolean enabled = call.getBoolean("enabled");
        YandexMetrica.setLocationTracking(enabled);
        call.success();
    }

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
