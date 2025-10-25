//
//  Plugin.swift
//  CapacitorAppmetricaPlugin
//
//  Created by Nalivayko Ivan on 22.07.2022.
//


import Foundation
import Capacitor
import AppMetricaCore
import AppMetricaCrashes

/**
 * docs: https://appmetrica.yandex.ru/docs/ru/sdk/ios/
 */
@objc(AppMetrica)
public class AppMetricaPlugin: CAPPlugin {
    
    public override func load() {
        NotificationCenter.default.addObserver(self, selector: #selector(self.handleUrlOpened(notification:)), name: Notification.Name(Notification.Name.capacitorOpenURL.rawValue), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(self.handleUniversalLink(notification:)), name: Notification.Name(Notification.Name.capacitorOpenUniversalLink.rawValue), object: nil)
    }
    
    /*
     * Открытие DeepLink
     */
    @objc func handleUrlOpened(notification: NSNotification) {
        guard let object = notification.object as? [String:Any?] else {
            return
        }
        
        guard let url = (object["url"] as? URL) else {
            return
        }
                
        AppMetrica.trackOpeningURL(url)
    }
    
    /*
     * Открытие UniversalLink
     */
    @objc func handleUniversalLink(notification: NSNotification) {
        guard let object = notification.object as? [String:Any?] else {
            return
        }
        
        guard let url = (object["url"] as? URL) else {
            return
        }
      
        AppMetrica.trackOpeningURL(url)
    }
    
    /**
     * Инициализация плагина
     */
    @objc func activate(_ call: CAPPluginCall) {
        do {
            let config = try Converter.toConfig(config: call.options)
            AppMetrica.activate(with: config)
            
            if let crashReporting = call.options["crashReporting"] as? Bool {
                let configuration = AppMetricaCrashesConfiguration()
                configuration.autoCrashTracking = crashReporting
                AppMetricaCrashes.crashes().setConfiguration(configuration)
            }
            
            call.resolve()
        } catch {
            call.reject("Не удалось инициализировать метрику")
        }
    }
    
    /**
     * Отправляет событие в App метрику
     */
    @objc func reportEvent(_ call: CAPPluginCall) {
        guard let evName = call.getString("name") else {
            call.reject("Undefined or empty event name")
            return
        }
        
        if call.options.index(forKey: "params") != nil {
            let evParams = call.getObject("params")
            AppMetrica.reportEvent(name: evName, parameters: evParams)
        } else {
            AppMetrica.reportEvent(name: evName)
        }
        
        call.resolve()
    }

    /**
     * Отправляет ошибку в App метрику
     */
    @objc func reportError(_ call: CAPPluginCall) {
        let group = call.getString("group") ?? call.getString("name") ?? "undefined"
        let message = call.getString("message") ?? call.getString("error") ?? nil
        let parameters = call.getObject("parameters", [:])
        
        let yandexError = AppMetricaError(
            identifier: group,
            message: message,
            parameters: parameters,
            backtrace: Thread.callStackReturnAddresses,
            underlyingError: nil
        )
        
        AppMetricaCrashes.crashes().report(error: yandexError)

        call.resolve()
    }
    
    /*
     * Задать объект Location для метрики
     */
    @objc func setLocation(_ call: CAPPluginCall) {
        let location = Converter.toLocation(location: call.options)
        
        AppMetrica.customLocation = location
        
        call.resolve()
    }
    
    /*
     * Отслеживание местоположения (вкл/выкл)
     */
    @objc func setLocationTracking(_ call: CAPPluginCall) {
        let enabled = call.getBool("enabled") ?? true
        
        AppMetrica.isLocationTrackingEnabled = enabled
        
        call.resolve()
    }
    
    /**
     * eCommerce: Открытие страницы
     */
    @objc func showScreenEvent(_ call: CAPPluginCall) {
        let screen = Converter.toECommerceScreen(screen: call.options)
        AppMetrica.reportECommerce(.showScreenEvent(screen: screen), onFailure: nil)

        call.resolve()
    }
    
    /**
     * eCommerce: Просмотр карточки товара
     */
    @objc func showProductCardEvent(_ call: CAPPluginCall) {
        do {
            let screen = Converter.toECommerceScreen(screen: call.options["screen"] as? [AnyHashable: Any] ?? [:])
            let product = try Converter.toECommerceProduct(product: call.options["product"] as? [AnyHashable: Any] ?? [:])
            
            AppMetrica.reportECommerce(.showProductCardEvent(product: product, screen: screen), onFailure: nil)
            
            call.resolve()
        }
        catch let e as Converter.ValidationError {
            call.reject(e.errorDescription ?? "Undefined error")
        }
        catch {
            call.reject("Undefined error")
        }
    }
    
    /**
     * eCommerce: Просмотр страницы товара
     */
    @objc func showProductDetailsEvent(_ call: CAPPluginCall) {
        do {
            let referrer = Converter.toECommerceReferrer(referrer: call.options["referrer"] as? [AnyHashable: Any] ?? [:])
            let product = try Converter.toECommerceProduct(product: call.options["product"] as? [AnyHashable: Any] ?? [:])
            
            AppMetrica.reportECommerce(.showProductDetailsEvent(product: product, referrer: referrer), onFailure: nil)
            
            call.resolve()
        }
        catch let e as Converter.ValidationError {
            call.reject(e.errorDescription ?? "Undefined error")
        }
        catch {
            call.reject("Undefined error")
        }
    }
    
    
    /**
     * eCommerce: Добавление товара в корзину
     */
    @objc func addCartItemEvent(_ call: CAPPluginCall) {
        do {
            let cartItem = try Converter.toECommerceCartItem(item: call.options)
            AppMetrica.reportECommerce(.addCartItemEvent(cartItem: cartItem), onFailure: nil)
            
            call.resolve()
        }
        catch let e as Converter.ValidationError {
            call.reject(e.errorDescription ?? "Undefined error")
        }
        catch {
            call.reject("Undefined error")
        }
    }
    
    /**
     * eCommerce: Удаление товара из корзины
     */
    @objc func removeCartItemEvent(_ call: CAPPluginCall) {
        do {
            let cartItem = try Converter.toECommerceCartItem(item: call.options)
            AppMetrica.reportECommerce(.removeCartItemEvent(cartItem: cartItem), onFailure: nil)
            
            call.resolve()
        }
        catch let e as Converter.ValidationError {
            call.reject(e.errorDescription ?? "Undefined error")
        }
        catch {
            call.reject("Undefined error")
        }
    }
    
    /**
     * eCommerce: Начало оформления заказа
     */
    @objc func beginCheckoutEvent(_ call: CAPPluginCall) {
        do {
            let order = try Converter.toECommerceOrder(order: call.options)
            
            AppMetrica.reportECommerce(.beginCheckoutEvent(order: order), onFailure: nil)
            
            call.resolve()
        }
        catch let e as Converter.ValidationError {
            call.reject(e.errorDescription ?? "Undefined error")
        }
        catch {
            call.reject("Undefined error")
        }
    }
    
    /**
     * eCommerce: Завершение оформления заказа
     */
    @objc func purchaseEvent(_ call: CAPPluginCall) {
        do {
            let order = try Converter.toECommerceOrder(order: call.options)
            
            AppMetrica.reportECommerce(.purchaseEvent(order: order), onFailure: nil)
            
            call.resolve()
        }
        catch let e as Converter.ValidationError {
            call.reject(e.errorDescription ?? "Undefined error")
        }
        catch {
            call.reject("Undefined error")
        }
    }
    
    /**
     * User Profile:  Отправка идентификатора  профиля
     */
    @objc func setUserProfileId(_ call: CAPPluginCall) {
        if let userId = call.options["id"] as? String {
            AppMetrica.userProfileID = userId
            call.resolve()
        } else {
            call.reject("Не передан обязательный идентификатор профиля")
        }
    }
    
    /**
     * User Profile:  Отправка атрибутов профиля
     */
    @objc func reportUserProfile(_ call: CAPPluginCall) {
        do {
            if let userId = call.options["id"] as? String {
                AppMetrica.userProfileID = userId
            }
            
            let profile = try Converter.toUserProfile(user: call.options)
            AppMetrica.reportUserProfile(profile)
            
            call.resolve()
        } catch {
            call.reject("Undefined Error")
        }
    }
}
