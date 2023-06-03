//
//  Converter.swift
//  CapacitorAppmetricaPlugin
//
//  Created by Nalivayko Ivan on 22.07.2022.
//

import Foundation
import YandexMobileMetrica
import CoreLocation

class Converter {
    enum ValidationError: Error {
        case apiKeyNotDefined
        case incorrectAmount
        case incorrectProductSku
        case incorrectProductItemQty
        case incorrectOrderId
        case incorrectOrderCartItems
        case unknown
    }
    
    /*
     * Конвертирует JSObject конфигурацию в объект конфигурации для AppMetrika
     */
    static func toConfig(config: [AnyHashable: Any]) throws -> YMMYandexMetricaConfiguration {
        guard let apiKey = config["apiKey"] as? String else {
            throw ValidationError.apiKeyNotDefined
        }
        
        let yamConfig = YMMYandexMetricaConfiguration.init(apiKey: apiKey)!
        
        if let handleFirstActivationAsUpdate = config["handleFirstActivationAsUpdate"] as? Bool {
            yamConfig.handleFirstActivationAsUpdate = handleFirstActivationAsUpdate
        }
        
        if let locationTracking = config["locationTracking"] as? Bool {
            yamConfig.locationTracking = locationTracking
        }
        
        if let sessionTimeout = config["sessionTimeout"] as? UInt {
            yamConfig.sessionTimeout = sessionTimeout
        }
        
        if let crashReporting = config["crashReporting"] as? Bool {
            yamConfig.crashReporting = crashReporting
        }
        
        if let appVersion = config["appVersion"] as? String {
            yamConfig.appVersion = appVersion
        }
        
        if let logs = config["logs"] as? Bool {
            yamConfig.logs = logs
        }
        
        if let location = config["location"] as? [AnyHashable: Any] {
            yamConfig.location = self.toLocation(location: location)
        }
        
        return yamConfig
    }
    
    /*
     * Конвертирует JSObject в объект местоположения
     */
    static func toLocation(location: [AnyHashable: Any]) -> CLLocation {
        let latitude        = location["latitude"]  as? Double   ?? 0.0
        let longitude       = location["longitude"] as? Double   ?? 0.0
        let altitude        = location["altitude"]  as? Double   ?? 0.0
        let hAccuracy       = location["hAccuracy"] as? Double   ?? location["accuracy"] as? Double ?? 0.0
        let vAccuracy       = location["vAccuracy"] as? Double   ?? -1
        let timestampNumber = location["timestamp"] as? NSNumber ?? -1
        let course          = location["course"]    as? Double   ?? 0.0
        let speed           = location["speed"]     as? Double   ?? 0.0
        
        let locationDate = Date(timeIntervalSince1970: timestampNumber.doubleValue)
        let coordinate = CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
        
        let yamLocation = CLLocation(
            coordinate:         coordinate,
            altitude:           altitude,
            horizontalAccuracy: hAccuracy,
            verticalAccuracy:   vAccuracy,
            course:             course,
            speed:              speed,
            timestamp:          locationDate
        )
        
        return yamLocation
    }
    
    /*
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
     * NOTE: В SDK для iOS "сategoriesPath" называется "categoryComponents"
     */
    static func toECommerceScreen(screen: [AnyHashable: Any]) -> YMMECommerceScreen {
        let yamScreen = YMMECommerceScreen(
            name:               screen["name"] as? String,
            categoryComponents: self.toStringArray(screen["categoriesPath"]),
            searchQuery:        screen["searchQuery"] as? String,
            payload:            screen["payload"] as? [String:String]
        )
        
        return yamScreen
    }
    
    /*
     * From:
     * {
     *     "sku": "779213",              // [!] Обязательный
     *     "name": "Продукт творожный «Даниссимо» 5.9%, 130 г.",
     *     "actualPrice": { ... },       // Смотри структуру toECommercePrice()
     *     "originalPrice": { ... },     // Смотри структуру toECommercePrice()
     *     "categoriesPath": ["Продукты", "Молочные продукты", "Йогурты"],
     *     "promocodes": ["BT79IYX", "UT5412EP"],
     *     "payload": {
     *         "ключ": "текстовое значение",
     *         ...
     *     }
     * }
     */
    static func toECommerceProduct(product: [AnyHashable: Any]) throws -> YMMECommerceProduct {
        guard let sku = product["sku"] as? String else {
            throw ValidationError.incorrectProductSku
        }
        
        var actualPrice: YMMECommercePrice? = nil
        var originalPrice: YMMECommercePrice? = nil
        
        if product.index(forKey: "actualPrice") != nil {
            actualPrice = try self.toECommercePrice(price: product["actualPrice"] as? [AnyHashable: Any] ?? [:])
        }
        
        if product.index(forKey: "originalPrice") != nil {
            originalPrice = try self.toECommercePrice(price: product["originalPrice"] as? [AnyHashable: Any] ?? [:])
        }
        
        let yamProduct = YMMECommerceProduct(
            sku:                sku,
            name:               product["name"] as? String,
            categoryComponents: self.toStringArray(product["categoriesPath"]),
            payload:            product["payload"] as? [String:String],
            actualPrice:        actualPrice,
            originalPrice:      originalPrice,
            promoCodes:         self.toStringArray(product["promocodes"])
        )
        
        return yamProduct
    }
    
    /*
     * From:
     * {
     *     "product": { ... },  // [!] Обязательный. Смотри структуру toECommerceProduct()
     *     "revenue": { ... },  // [!] Обязательный. Получаемый доход. Смотри структуру toECommercePrice()
     *     "quantity": 1.0,     // [!] Обязательный.
     *     "referrer": { ... }  // Смотри структуру toECommerceReferrer()
     * }
     */
    static func toECommerceCartItem(item: [AnyHashable: Any]) throws -> YMMECommerceCartItem {
        let yamProduct = try self.toECommerceProduct(product: item["product"] as? [AnyHashable: Any] ?? [:])
        let yamRevenue = try self.toECommercePrice(price: item["revenue"] as? [AnyHashable: Any] ?? [:])
        
        guard let quantity = item["quantity"] as? NSNumber else {
            throw ValidationError.incorrectProductItemQty
        }
        
        var yamReferrer: YMMECommerceReferrer? = nil
        if item.index(forKey: "referrer") != nil {
            yamReferrer = self.toECommerceReferrer(referrer: item["referrer"] as? [AnyHashable: Any] ?? [:])
        }
        
        let yamCartItem = YMMECommerceCartItem(
            product:    yamProduct,
            quantity:   NSDecimalNumber(value: quantity.doubleValue),
            revenue:    yamRevenue,
            referrer:   yamReferrer
        )
        
        return yamCartItem
    }
    
    /*
     * From:
     * {
     *     "identifier": "88528768",     [!] Обязательный.
     *     "cartItems": [                [!] Обязательный.
     *          { ... },                 Смотри структуру toECommerceCartItem()
     *          ...
     *     ],
     *     "payload": {
     *         "ключ": "текстовое значение",
     *         ...
     *     }
     * }
     */
    static func toECommerceOrder(order: [AnyHashable: Any]) throws -> YMMECommerceOrder {
        guard let identifier = order["identifier"] as? String else {
            throw ValidationError.incorrectOrderId
        }
        
        guard let items = order["cartItems"] as? [ [AnyHashable: Any] ] else {
            throw ValidationError.incorrectOrderCartItems
        }
        
        let yamCartItems = try items.map { item in
            return try self.toECommerceCartItem(item: item)
        }
        
        let yamOrder = YMMECommerceOrder(
            identifier: identifier,
            cartItems:  yamCartItems,
            payload:    order["payload"] as? [String: String]
        )
        
        return yamOrder
    }
    
    /*
     * From:
     * {
     *     "type": "button",
     *     "identifier": "76890",
     *     "screen": { ... }      // Смотри структуру toECommerceScreen
     * }
     */
    static func toECommerceReferrer(referrer: [AnyHashable: Any]) -> YMMECommerceReferrer {
        var yamScreen: YMMECommerceScreen? = nil
        
        if referrer.index(forKey: "screen") != nil {
            yamScreen = toECommerceScreen(screen: referrer["screen"] as? [AnyHashable: Any] ?? [:])
        }
        
        let yamReferrer = YMMECommerceReferrer(
            type:       referrer["type"] as? String,
            identifier: referrer["identifier"] as? String,
            screen:     yamScreen
        )
        
        return yamReferrer
    }
    
    /*
     * From:
     * {
     *     "fiat": [4.53, "USD"],      // [!] Обязательный
     *     "internalComponents": [
     *          [30_570_000, "wood"],
     *          [26.89, "iron"],
     *          [5.1, "gold"]
     *     ]
     * }
     */
    static func toECommercePrice(price: [AnyHashable: Any]) throws -> YMMECommercePrice {
        let internalComponents: [YMMECommerceAmount]? = try (price["internalComponents"] as? [Any])?.map { amount in
            return try self.toECommerceAmount(amount: amount as? [Any] ?? [])
        }
        
        let actualPrice = YMMECommercePrice(
            fiat: try self.toECommerceAmount(amount: price["fiat"]! as? [Any] ?? []),
            internalComponents: internalComponents
        )
        
        return actualPrice
    }
    
    /*
     * From:
     * [10.5, "USD"]
     */
    static func toECommerceAmount(amount: [Any]) throws -> YMMECommerceAmount {
        guard
            let value = amount[0] as? NSNumber,
            let unit  = amount[1] as? String
        else {
            throw ValidationError.incorrectAmount
        }
        
        let yamAmount = YMMECommerceAmount(
            unit:  unit,
            value: NSDecimalNumber(value: value.doubleValue)
        )
        
        return yamAmount
    }
    
    static func toStringArray(_ rawItems: Any?) -> [String]? {
        guard let items = rawItems as? NSArray else {
            return nil
        }
        
        var preparedItems = [String]()

        for rawItem in items {
            // Skip no String() items
            if let strItem = rawItem as? String {
                preparedItems.append(strItem)
            }
        }

        return preparedItems
    }
    
    /*
     * From:
     * {
     *   "name": "Ivan Nalivayko",
     *   "gender": "male",
     *   "notificationEnabled": false,
     *   "birthDate": { // OR: { "age": 20 }
     *     "year": 2001,
     *     "month": 1,
     *     "day": 1
     *   }
     * }
     */
    static func toUserProfile(user: [AnyHashable: Any]) throws -> YMMMutableUserProfile {
        let yamProfile = YMMMutableUserProfile()
        
        if let name = user["name"] as? String {
            yamProfile.apply(YMMProfileAttribute.name().withValue(name))
        }
        
        if let gender = user["gender"] as? String {
            yamProfile.apply(YMMProfileAttribute.gender().withValue(toGenderType(gender)))
        }
        
        if let birthDateParts = user["birthDate"] as? [AnyHashable: Any] {
            var birthDate: YMMUserProfileUpdate? = nil;
            
            if let year = birthDateParts["year"] as? NSNumber as? UInt {
                if let month = birthDateParts["month"] as? NSNumber as? UInt {
                    if let day = birthDateParts["day"] as? NSNumber as? UInt {
                        birthDate = YMMProfileAttribute
                            .birthDate()
                            .withDate(year: year, month: month, day: day)
                    } else {
                        birthDate = YMMProfileAttribute
                            .birthDate()
                            .withDate(year: year, month: month)
                    }
                } else {
                    birthDate = YMMProfileAttribute
                        .birthDate()
                        .withDate(year: year)
                }
            } else if let age = birthDateParts["age"] as? NSNumber as? UInt {
                birthDate = YMMProfileAttribute
                    .birthDate()
                    .withAge(age)
            }
            
            if birthDate != nil {
                yamProfile.apply(birthDate!)
            }
        }
        
        if let notificationEnabled = user["notificationEnabled"] as? Bool {
            yamProfile.apply(YMMProfileAttribute.notificationsEnabled().withValue(notificationEnabled))
        }
        
        return yamProfile
    }
    
    static func toGenderType(_ gender: String) -> YMMGenderType {
        if gender == "female" {
            return YMMGenderType.female
        }
        
        if gender == "male" {
            return YMMGenderType.male
        }
        
        return YMMGenderType.other
    }
}

extension Converter.ValidationError: LocalizedError {
    public var errorDescription: String? {
        switch self {
        case .apiKeyNotDefined:
            return NSLocalizedString("Api key not defined", comment: "Api key not defined")
        case .incorrectAmount:
            return NSLocalizedString("Incorrect amount", comment: "Incorrect amount value")
        case .incorrectProductSku:
            return NSLocalizedString("Incorrect SKU product", comment: "Incorrect SKU product")
        case .incorrectProductItemQty:
            return NSLocalizedString("Incorrect product qantity", comment: "Incorrect cart product item quantity")
        case .incorrectOrderId:
            return NSLocalizedString("Incorrect order id", comment: "Incorrect order id")
        case .incorrectOrderCartItems:
            return NSLocalizedString("Incorrect order cart items", comment: "Incorrect order cart items")
        case .unknown:
            return NSLocalizedString("Unknown", comment: "Unknown")
        }
    }
}
