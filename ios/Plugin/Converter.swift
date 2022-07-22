//
//  Converter.swift
//  CapacitorAppmetricaPlugin
//
//  Created by Nalivayko Ivan on 22.07.2022.
//

import Foundation
import YandexMobileMetrica

class Converter {
    static func toConfig(config: [AnyHashable: Any]) throws -> YMMYandexMetricaConfiguration {
        let yamConfig = YMMYandexMetricaConfiguration.init(apiKey: config.getString("apiKey"))
        
        
        
        return yamConfig
    }
}
