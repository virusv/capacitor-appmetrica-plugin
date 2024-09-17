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
    
    public override func load() {
        NotificationCenter.default.addObserver(self, selector: #selector(self.handleUrlOpened(notification:)), name: Notification.Name(CAPNotifications.URLOpen.name()), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(self.handleUniversalLink(notification:)), name: Notification.Name(CAPNotifications.UniversalLinkOpen.name()), object: nil)
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
                
        YMMYandexMetrica.handleOpen(url)
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
      
        YMMYandexMetrica.handleOpen(url)
    }
    
    /**
     * Инициализация плагина
     */
    @objc func activate(_ call: CAPPluginCall) {
        do {
            let config = try Converter.toConfig(config: call.options)
            YMMYandexMetrica.activate(with: config)
            
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
        
        let evParams = call.getObject("params")

        YMMYandexMetrica.reportEvent(evName, parameters: evParams)
        
        call.resolve()
    }

    /**
     * Отправляет ошибку в App метрику
     */
    @objc func reportError(_ call: CAPPluginCall) {
        let group = call.getString("group") ?? call.getString("name") ?? "undefined"
        let message = call.getString("message") ?? call.getString("error") ?? nil
        let parameters = call.getObject("parameters", [:])
        
        let yandexError = YMMError(
            identifier: group,
            message: message,
            parameters: parameters,
            backtrace: Thread.callStackReturnAddresses,
            underlyingError: nil
        )
        
        YMMYandexMetrica.report(error: yandexError, onFailure: nil)

        call.resolve()
    }
    
    /*
     * Задать объект Location для метрики
     */
    @objc func setLocation(_ call: CAPPluginCall) {
        let location = Converter.toLocation(location: call.options)
        
        YMMYandexMetrica.setLocation(location)
        
        call.resolve()
    }
    
    /*
     * Отслеживание местоположения (вкл/выкл)
     */
    @objc func setLocationTracking(_ call: CAPPluginCall) {
        let enabled = call.getBool("enabled") ?? true
        
        YMMYandexMetrica.setLocationTracking(enabled)
        
        call.resolve()
    }
    
    /**
     * eCommerce: Открытие страницы
     */
    @objc func showScreenEvent(_ call: CAPPluginCall) {
        let screen = Converter.toECommerceScreen(screen: call.options)
        YMMYandexMetrica.report(eCommerce: .showScreenEvent(screen: screen), onFailure: nil)

        call.resolve()
    }
    
    /**
     * eCommerce: Просмотр карточки товара
     */
    @objc func showProductCardEvent(_ call: CAPPluginCall) {
        do {
            let screen = Converter.toECommerceScreen(screen: call.options["screen"] as? [AnyHashable: Any] ?? [:])
            let product = try Converter.toECommerceProduct(product: call.options["product"] as? [AnyHashable: Any] ?? [:])
            
            YMMYandexMetrica.report(eCommerce: .showProductCardEvent(product: product, screen: screen), onFailure: nil)
            
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
            
            YMMYandexMetrica.report(eCommerce: .showProductDetailsEvent(product: product, referrer: referrer), onFailure: nil)
            
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
            YMMYandexMetrica.report(eCommerce: .addCartItemEvent(cartItem: cartItem), onFailure: nil)
            
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
            YMMYandexMetrica.report(eCommerce: .removeCartItemEvent(cartItem: cartItem), onFailure: nil)
            
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
            
            YMMYandexMetrica.report(eCommerce: .beginCheckoutEvent(order: order), onFailure: nil)
            
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
            
            YMMYandexMetrica.report(eCommerce: .purchaseEvent(order: order), onFailure: nil)
            
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
            YMMYandexMetrica.setUserProfileID(userId)
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
            let profile = try Converter.toUserProfile(user: call.options)
            YMMYandexMetrica.report(profile)
            
            call.resolve()
        } catch {
            call.reject("Undefined Error")
        }
    }
}
