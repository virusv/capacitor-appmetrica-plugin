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
        case incorrectSkuProduct
        case unknown
    }
    
    /**
     * Конвертирует JSObject конфигурацию в объект конфигурации для AppMetrika
     */
    static func toConfig(config: NSDictionary) throws -> YMMYandexMetricaConfiguration {
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
        
        if let location = config["location"] as? NSDictionary {
            yamConfig.location = self.toLocation(location: location)
        }
        
        return yamConfig
    }
    
    /**
     * Конвертирует JSObject в объект местоположения
     */
    static func toLocation(location: NSDictionary) -> CLLocation {
        let latitude = location["latitude"] as? Double ?? 0.0
        let longitude = location["longitude"] as? Double ?? 0.0
        let altitude = location["altitude"] as? Double ?? 0.0
        let hAccuracy = location["hAccuracy"] as? Double ?? location["accuracy"] as? Double ?? 0.0
        let vAccuracy = location["vAccuracy"] as? Double ?? 0.0
        let timestampNumber = location["timestamp"] as? NSNumber ?? 0.0
        let course = location["course"] as? Double ?? 0.0
        let speed = location["speed"] as? Double ?? 0.0
        
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
     */
    static func toECommerceScreen(screen: [AnyHashable: Any]) -> YMMECommerceScreen {
        let yamScreen = YMMECommerceScreen(
            name:               screen["name"] as? String,
            categoryComponents: screen["сategoriesPath"] as? [String],
            searchQuery:        screen["searchQuery"] as? String,
            payload:            screen["payload"] as? [String:String]
        )
        
        return yamScreen
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
     */
    static func toECommerceProduct(product: [AnyHashable: Any]) throws -> YMMECommerceProduct {
        guard let sku = product["sku"] as? String else {
            throw ValidationError.incorrectSkuProduct
        }
        
        var actualPrice: YMMECommercePrice? = nil
        var originalPrice: YMMECommercePrice? = nil
        
        if product.index(forKey: "actualPrice") != nil {
            actualPrice = try toECommercePrice(price: product["actualPrice"] as? [AnyHashable: Any] ?? [:])
        }
        
        if product.index(forKey: "originalPrice") != nil {
            originalPrice = try toECommercePrice(price: product["originalPrice"] as? [AnyHashable: Any] ?? [:])
        }
        
        let yamProduct = YMMECommerceProduct(
            sku:                sku,
            name:               product["name"] as? String,
            categoryComponents: product["сategoriesPath"] as? [String],
            payload:            product["payload"] as? [String:String],
            actualPrice:        actualPrice,
            originalPrice:      originalPrice,
            promoCodes:         product["promoCodes"] as? [String]
        )
        
        return yamProduct
    }
    
    /**
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
    
    /**
     * From:
     * [10.5, "USD"]
     */
    static func toECommerceAmount(amount: [Any]) throws -> YMMECommerceAmount {
        guard
            let value = amount[0] as? Double,
            let unit  = amount[1] as? String
        else {
            throw ValidationError.incorrectAmount
        }
        
        let yamAmount = YMMECommerceAmount(
            unit:  unit,
            value: NSDecimalNumber(value: value)
        )
        
        return yamAmount
    }
}

extension Converter.ValidationError: LocalizedError {
    public var errorDescription: String? {
        switch self {
        case .apiKeyNotDefined:
            return NSLocalizedString("Api key not defined", comment: "Api key not defined")
        case .incorrectAmount:
            return NSLocalizedString("Incorrect amount", comment: "Incorrect amount value")
        case .incorrectSkuProduct:
            return NSLocalizedString("Incorrect SKU product", comment: "Incorrect SKU product")
        case .unknown:
            return NSLocalizedString("Unknown", comment: "Unknown")
        }
    }
}
