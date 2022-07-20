package ru.inaliv.appmetrica;

import android.location.Location;
import com.getcapacitor.JSObject;
import com.yandex.metrica.YandexMetricaConfig;
import com.yandex.metrica.ecommerce.ECommerceAmount;
import com.yandex.metrica.ecommerce.ECommercePrice;
import com.yandex.metrica.ecommerce.ECommerceProduct;
import com.yandex.metrica.ecommerce.ECommerceScreen;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Converter {
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
            final Location location = Converter.toLocation(config.getJSObject("location"));
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
        final Location yamLocation = new Location("Custom");

        if (location.has("latitude")) {
            yamLocation.setLatitude(location.getDouble("latitude"));
        }
        if (location.has("longitude")) {
            yamLocation.setLongitude(location.getDouble("longitude"));
        }
        if (location.has("altitude")) {
            yamLocation.setAltitude(location.getDouble("altitude"));
        }
        if (location.has("accuracy")) {
            yamLocation.setAccuracy((float) location.getDouble("accuracy"));
        }
        if (location.has("course")) {
            yamLocation.setBearing((float) location.getDouble("course"));
        }
        if (location.has("speed")) {
            yamLocation.setSpeed((float) location.getDouble("speed"));
        }
        if (location.has("timestamp")) {
            yamLocation.setTime(location.getLong("timestamp"));
        }

        return yamLocation;
    }

    /**
     * From:
     * {
     *     "id": "779213",              // [!] Обязательный
     *     "name": "Продукт творожный «Даниссимо» 5.9%, 130 г.",
     *     "actualPrice": { ... },      // Смотри структуру toECommercePrice
     *     "originalPrice": { ... },    // Смотри структуру toECommercePrice
     *     "categoriesPath": ["Продукты", "Молочные продукты", "Йогурты"],
     *     "promocodes": ["BT79IYX", "UT5412EP"],
     *     "payload": {
     *         "ключ": "текстовое значение",
     *         ...
     *     }
     * }
     *
     * NOTE: В iOS SDK:
     * - "id" товара обозначается "sku"
     *
     * @param product
     * @return
     * @throws JSONException
     */
    public static ECommerceProduct toECommerceProduct(final JSObject product) throws JSONException {
        ECommerceProduct yamProduct = new ECommerceProduct(product.getString("id"));

        if (product.has("name")) {
            yamProduct.setName(product.getString("name"));
        }

        if (product.has("actualPrice")) {
            yamProduct.setActualPrice(
                toECommercePrice(
                    product.getJSObject("actualPrice")
                )
            );
        }

        if (product.has("originalPrice")) {
            yamProduct.setOriginalPrice(
                toECommercePrice(
                    product.getJSObject("originalPrice")
                )
            );
        }

        if (product.has("categoriesPath")) {
            yamProduct.setCategoriesPath(
                toStringList(
                    product.getJSONArray("categoriesPath")
                )
            );
        }

        if (product.has("promocodes")) {
            yamProduct.setPromocodes(
                toStringList(
                    product.getJSONArray("promocodes")
                )
            );
        }

        if (product.has("payload")) {
            yamProduct.setPayload(
                toHashMapPayload(
                    product.getJSObject("payload")
                )
            );
        }

        return yamProduct;
    }

    /**
     * From:
     * {
     *     "name": "ProductCardActivity",
     *     "searchQuery": "даниссимо кленовый сироп",
     *     "сategoriesPath": ["Акции", "Красная цена"],
     *     "payload": {
     *         "ключ": "текстовое значение",
     *         ...
     *     }
     * }
     *
     * NOTE: В SDK для iOS "сategoriesPath" называется "categoryComponents"
     *
     * @param screen
     * @return
     * @throws JSONException
     */
    public static ECommerceScreen toECommerceScreen(final JSObject screen) throws JSONException {
        ECommerceScreen yamScreen = new ECommerceScreen();

        if (screen.has("name")) {
            yamScreen.setName(screen.getString("name"));
        }

        if (screen.has("searchQuery")) {
            yamScreen.setSearchQuery(screen.getString("searchQuery"));
        }

        if (screen.has("сategoriesPath")) {
            yamScreen.setCategoriesPath(toStringList(screen.getJSONArray("сategoriesPath")));
        }

        if (screen.has("payload")) {
            yamScreen.setPayload(toHashMapPayload(screen.getJSObject("payload")));
        }

        return yamScreen;
    }

    /**
     * From:
     * {
     *     "fiat": [4.53, "USD"],
     *     "internalComponents": [
     *          [30_570_000, "wood"],
     *          [26.89, "iron"],
     *          [5.1, "gold"]
     *     ]
     * }
     *
     * @param price
     * @return
     * @throws JSONException
     */
    public static ECommercePrice toECommercePrice(final JSObject price) throws JSONException {
        ECommercePrice yamPrice = new ECommercePrice(
            toECommerceAmount(
                price.getJSONArray("fiat")
            )
        );

        if (price.has("internalComponents")) {
            JSONArray internalComponents = price.getJSONArray("internalComponents");
            List<ECommerceAmount> yamInternalComponents = new ArrayList<>();

            for (int i = 0; i < internalComponents.length(); ++i) {
                yamInternalComponents.add(
                    toECommerceAmount(
                        internalComponents.getJSONArray(i)
                    )
                );
            }

            yamPrice.setInternalComponents(yamInternalComponents);
        }

        return yamPrice;
    }

    /**
     * From:
     * [10.5, "USD"]
     *
     * @param jsonArray
     * @return
     * @throws JSONException
     */
    public static ECommerceAmount toECommerceAmount(JSONArray jsonArray) throws JSONException {
        return new ECommerceAmount(jsonArray.getDouble(0), jsonArray.getString(1));
    }

    /**
     * Используется для дополнительных значений в:
     * - ECommerceScreen
     *
     * @param payload
     * @return
     */
    public static Map<String, String> toHashMapPayload(final JSObject payload) {
        Map<String, String> yamPayload = new HashMap<>();
        Iterator<String> payloadIter = payload.keys();

        while (payloadIter.hasNext()) {
            String key = payloadIter.next();
            yamPayload.put(key, payload.getString(key));
        }

        return yamPayload;
    }

    /**
     * Преобразует JSONArray в строковый список, используется в
     * - toECommerceScreen, для категорий
     *
     * @param jsonArray
     * @return
     * @throws JSONException
     */
    private static List<String> toStringList(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); ++i) {
            list.add(jsonArray.get(i).toString());
        }

        return list;
    }
}
