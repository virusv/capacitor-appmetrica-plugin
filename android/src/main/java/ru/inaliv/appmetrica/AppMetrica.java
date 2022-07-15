package ru.inaliv.appmetrica;

import android.content.Context;
import android.location.Location;

import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

@NativePlugin
public class AppMetrica extends Plugin {
    private final Object mLock = new Object();
    private boolean mActivityPaused = true;
    private boolean mAppMetricaActivated = false;

    /**
     * Сконвертирует конфигурацию JSObject в объект для App Метрики
     *
     * @param config
     * @return
     */
    public static YandexMetricaConfig toConfig(JSObject config) {
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
//            final Location location = toLocation(configObj.getJSONObject("location"));
//            builder.withLocation(location);
        }

        return builder.build();
    }


    @PluginMethod
    public void init(PluginCall call) {
        final String apiKey = call.getString("key");
        final Context context = this.bridge.getActivity().getApplicationContext();

        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(apiKey).withLogs().build();
        YandexMetrica.activate(context, config);

        call.success();
    }

    /**
     * Активация метрики
     *
     * @param call
     */
    @PluginMethod
    private void activate(PluginCall call) {
        final YandexMetricaConfig config = toConfig(call.getData());
        final Context context = bridge.getActivity().getApplicationContext();

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


}
