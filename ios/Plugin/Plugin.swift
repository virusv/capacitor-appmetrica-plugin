//
//  Plugin.swift
//  CapacitorAppmetricaPlugin
//
//  Created by Nalivayko Ivan on 22.07.2022.
//

import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(AppMetrica)
public class AppMetrica: CAPPlugin {

    @objc func activate(_ call: CAPPluginCall) {
        let config = Converter.toConfig(config: call.options)
        
        
        
        call.success([])
    }
}
