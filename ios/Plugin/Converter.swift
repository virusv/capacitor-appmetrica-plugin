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
            coordinate: coordinate,
            altitude: altitude,
            horizontalAccuracy: hAccuracy,
            verticalAccuracy: vAccuracy,
            course: course,
            speed: speed,
            timestamp: locationDate
        )
        
        return yamLocation
    }
}
