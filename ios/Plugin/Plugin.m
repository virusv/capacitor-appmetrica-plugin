#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(AppMetrica, "AppMetrica",
    CAP_PLUGIN_METHOD(activate, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(reportEvent, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(reportError, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(showScreenEvent, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(showProductCardEvent, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(showProductDetailsEvent, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(addCartItemEvent, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(removeCartItemEvent, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(beginCheckoutEvent, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(purchaseEvent, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setLocation, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setLocationTracking, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setUserProfileId, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(reportUserProfile, CAPPluginReturnPromise);
)
