//
//  Plugin.swift
//  CapacitorAppmetricaPlugin
//
//  Created by Nalivayko Ivan on 22.07.2022.
//


import Foundation
import Capacitor
import YandexMobileMetrica

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(AppMetrica)
public class AppMetrica: CAPPlugin {

    @objc func activate(_ call: CAPPluginCall) {
        
        do {
            let config = try Converter.toConfig(config: call.options as NSDictionary)
            YMMYandexMetrica.activate(with: config)
            
            call.success()
        } catch {
            call.error("Не удалось инициализировать метрику")
        }
    }
}
