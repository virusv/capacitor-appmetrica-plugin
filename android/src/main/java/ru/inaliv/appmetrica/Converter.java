package ru.inaliv.appmetrica;

import android.location.Location;
import android.os.Build;

import com.yandex.metrica.YandexMetricaConfig;
import com.yandex.metrica.ecommerce.ECommerceAmount;
import com.yandex.metrica.ecommerce.ECommerceCartItem;
import com.yandex.metrica.ecommerce.ECommerceOrder;
import com.yandex.metrica.ecommerce.ECommercePrice;
import com.yandex.metrica.ecommerce.ECommerceProduct;
import com.yandex.metrica.ecommerce.ECommerceReferrer;
import com.yandex.metrica.ecommerce.ECommerceScreen;
import com.yandex.metrica.profile.Attribute;
import com.yandex.metrica.profile.GenderAttribute;
import com.yandex.metrica.profile.UserProfile;
import com.yandex.metrica.profile.UserProfileUpdate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Converter {
    /**
     * Сконвертирует конфигурацию JSONObject в объект для App Метрики
     *
     * @param config
     * @return
     */
    public static YandexMetricaConfig toConfig(final JSONObject config) throws JSONException {
        final String apiKey = config.getString("apiKey");
        final YandexMetricaConfig.Builder builder = YandexMetricaConfig.newConfigBuilder(apiKey);

        if (config.has("handleFirstActivationAsUpdate")) {
            builder.handleFirstActivationAsUpdate(config.getBoolean("handleFirstActivationAsUpdate"));
        }
        if (config.has("locationTracking")) {
            builder.withLocationTracking(config.getBoolean("locationTracking"));
        }
        if (config.has("sessionTimeout")) {
            builder.withSessionTimeout(config.getInt("sessionTimeout"));
        }
        if (config.has("crashReporting")) {
            builder.withCrashReporting(config.getBoolean("crashReporting"));
        }
        if (config.has("nativeCrashReporting")) {
            builder.withNativeCrashReporting(config.getBoolean("nativeCrashReporting"));
        }
        if (config.has("appVersion")) {
            builder.withAppVersion(config.getString("appVersion"));
        }
        if (config.optBoolean("logs", false)) {
            builder.withLogs();
        }
        if (config.has("location")) {
            final Location location = Converter.toLocation(config.getJSONObject("location"));
            builder.withLocation(location);
        }

        return builder.build();
    }

    /**
     * Конвертирует конфигурацию JSONObject в объект Location для App Метрики
     *
     * @param location
     * @return
     * @throws JSONException
     */
    public static Location toLocation(final JSONObject location) throws JSONException {
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

        if (location.has("vAccuracy")) {
            yamLocation.setAccuracy((float) location.getDouble("vAccuracy"));
        }
        else if (location.has("accuracy")) {
            yamLocation.setAccuracy((float) location.getDouble("accuracy"));
        }

        if (location.has("hAccuracy") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            yamLocation.setVerticalAccuracyMeters((float) location.getDouble("hAccuracy"));
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

//        yamLocation.setVerticalAccuracyMeters(); // TODO:


        return yamLocation;
    }

    /**
     * From:
     * {
     *     "sku": "779213",              // [!] Обязательный
     *     "name": "Продукт творожный «Даниссимо» 5.9%, 130 г.",
     *     "actualPrice": { ... },      // Смотри структуру toECommercePrice()
     *     "originalPrice": { ... },    // Смотри структуру toECommercePrice()
     *     "categoriesPath": ["Продукты", "Молочные продукты", "Йогурты"],
     *     "promocodes": ["BT79IYX", "UT5412EP"],
     *     "payload": {
     *         "ключ": "текстовое значение",
     *         ...
     *     }
     * }
     *
     * @param product
     * @return
     * @throws JSONException
     */
    public static ECommerceProduct toECommerceProduct(final JSONObject product) throws JSONException {
        ECommerceProduct yamProduct = new ECommerceProduct(product.getString("sku"));

        if (product.has("name")) {
            yamProduct.setName(product.getString("name"));
        }

        if (product.has("actualPrice")) {
            yamProduct.setActualPrice(
                toECommercePrice(
                    product.getJSONObject("actualPrice")
                )
            );
        }

        if (product.has("originalPrice")) {
            yamProduct.setOriginalPrice(
                toECommercePrice(
                    product.getJSONObject("originalPrice")
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
                    product.getJSONObject("payload")
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
     *     "categoriesPath": ["Акции", "Красная цена"],
     *     "payload": {
     *         "ключ": "текстовое значение",
     *         ...
     *     }
     * }
     *
     * NOTE: В SDK для iOS "categoriesPath" называется "categoryComponents"
     *
     * @param screen
     * @return
     * @throws JSONException
     */
    public static ECommerceScreen toECommerceScreen(final JSONObject screen) throws JSONException {
        ECommerceScreen yamScreen = new ECommerceScreen();

        if (screen.has("name")) {
            yamScreen.setName(screen.getString("name"));
        }

        if (screen.has("searchQuery")) {
            yamScreen.setSearchQuery(screen.getString("searchQuery"));
        }

        if (screen.has("categoriesPath")) {
            yamScreen.setCategoriesPath(toStringList(screen.getJSONArray("categoriesPath")));
        }

        if (screen.has("payload")) {
            yamScreen.setPayload(toHashMapPayload(screen.getJSONObject("payload")));
        }

        return yamScreen;
    }

    /**
     * From:
     * {
     *     "product": { ... }, // [!] Обязательный. Смотри структуру toECommerceProduct()
     *     "revenue": { ... }, // [!] Обязательный. Получаемый доход. Смотри структуру toECommercePrice()
     *     "quantity": 1.0,    // [!] Обязательный.
     *     "referrer": { ... } // Смотри структуру toECommerceReferrer()
     * }
     *
     * @param item
     * @return
     * @throws JSONException
     */
    public static ECommerceCartItem toECommerceCartItem(final JSONObject item) throws JSONException {
        ECommerceProduct yamProduct = toECommerceProduct(item.getJSONObject("product"));
        ECommercePrice yamRevenue = toECommercePrice(item.getJSONObject("revenue"));
        double quantity = (double)item.getDouble("quantity");

        ECommerceCartItem yamCartItem = new ECommerceCartItem(yamProduct, yamRevenue, quantity);

        if (item.has("referrer")) {
            yamCartItem.setReferrer(
                toECommerceReferrer(
                    item.getJSONObject("referrer")
                )
            );
        }

        return yamCartItem;
    }

    /**
     * From:
     * {
     *     "identifier": "88528768", // [!] Обязательный.
     *     "cartItems": [            // [!] Обязательный.
     *          { ... },             // Смотри структуру toECommerceCartItem()
     *          ...
     *     ],
     *     "payload": {
     *         "ключ": "текстовое значение",
     *         ...
     *     }
     * }
     *
     * @param order
     * @return
     * @throws JSONException
     */
    public static ECommerceOrder toECommerceOrder(final JSONObject order) throws JSONException {
        String identifier = order.getString("identifier");
        List<ECommerceCartItem> yamCartItems = new ArrayList<>();

        JSONArray items = order.getJSONArray("cartItems");
        for (int i = 0; i < items.length(); ++i) {
            ECommerceCartItem yamItem = toECommerceCartItem(
                    items.getJSONObject(i)
            );

            yamCartItems.add(yamItem);
        }

        ECommerceOrder yamOrder = new ECommerceOrder(identifier, yamCartItems);

        if (order.has("payload")) {
            yamOrder.setPayload(
                toHashMapPayload(
                    order.getJSONObject("payload")
                )
            );
        }

        return yamOrder;
    }

    /**
     * From:
     * {
     *     "type": "button",
     *     "identifier": "76890",
     *     "screen": { ... }      // Смотри структуру toECommerceScreen
     * }
     *
     * @param referrer
     * @return
     * @throws JSONException
     */
    public static ECommerceReferrer toECommerceReferrer(final JSONObject referrer) throws JSONException {
        ECommerceReferrer yamReferrer = new ECommerceReferrer();

        if (referrer.has("type")) {
            yamReferrer.setType(referrer.getString("type"));
        }

        if (referrer.has("identifier")) {
            yamReferrer.setIdentifier(referrer.getString("identifier"));
        }

        if (referrer.has("screen")) {
            yamReferrer.setScreen(
                toECommerceScreen(
                    referrer.getJSONObject("screen")
                )
            );
        }

        return yamReferrer;
    }

    /**
     * From:
     * {
     *     "fiat": [4.53, "USD"],      // [!] Обязательный
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
    public static ECommercePrice toECommercePrice(final JSONObject price) throws JSONException {
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
     * - toECommerceScreen
     * - toECommerceProduct
     *
     * @param payload
     * @return
     */
    public static Map<String, String> toHashMapPayload(final JSONObject payload) throws JSONException {
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
     * - toECommerceProduct, для категорий и промокодов
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

    /**
     * From:
     * {
     *   "name": "Ivan Nalivayko",
     *   "gender": "male",
     *   "notificationEnabled": false,
     *   "birthDate": { // OR: { "age": 20 }
     *     "year": 2001,
     *     "month": 1,
     *      "day": 1
     *   }
     * }
     *
     * @param user
     * @return
     * @throws JSONException
     */
    public static UserProfile toUserProfile(final JSONObject user) throws JSONException {
        UserProfile.Builder yamProfileBuilder = UserProfile.newBuilder();

        if (user.has("name")) {
            yamProfileBuilder.apply(Attribute.name().withValue(
                user.getString("name")
            ));
        }

        if (user.has("gender")) {
            yamProfileBuilder.apply(Attribute.gender().withValue(
                toGenderType(user.getString("gender"))
            ));
        }

        if (user.has("notificationEnabled")) {
            yamProfileBuilder.apply(Attribute.notificationsEnabled().withValue(
                user.getBoolean("notificationEnabled")
            ));
        }

        if (user.has("birthDate")) {
            JSONObject birthDate = user.getJSONObject("birthDate");
            UserProfileUpdate yamBirthDate = null;

            if (birthDate.has("year")) {
                int year = birthDate.getInt("year");

                if (birthDate.has("month")) {
                    int month = birthDate.getInt("month");

                    if (birthDate.has("day")) {
                        int day = birthDate.getInt("day");

                        yamBirthDate = Attribute.birthDate().withBirthDate(year, month, day);
                    } else {
                        yamBirthDate = Attribute.birthDate().withBirthDate(year, month);
                    }
                } else {
                    yamBirthDate = Attribute.birthDate().withBirthDate(year);
                }
            } else if (birthDate.has("age")) {
                int age = birthDate.getInt("age");

                yamBirthDate = Attribute.birthDate().withAge(age);
            }

            if (yamBirthDate != null) {
                yamProfileBuilder.apply(yamBirthDate);
            }
        }

        return yamProfileBuilder.build();
    }

    private static GenderAttribute.Gender toGenderType(final String gender) {
        if (gender == "female") {
            return GenderAttribute.Gender.FEMALE;
        }

        if (gender == "male") {
            return GenderAttribute.Gender.MALE;
        }

        return GenderAttribute.Gender.OTHER;
    }
}
