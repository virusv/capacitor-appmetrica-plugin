// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorAppmetricaPlugin",
    platforms: [.iOS(.v15)],
    products: [
        .library(name: "CapacitorAppmetricaPlugin", targets: ["CapacitorAppmetricaPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0"),
        .package(url: "https://github.com/appmetrica/appmetrica-sdk-ios", .upToNextMinor(from: "5.14.0"))
    ],
    targets: [
        .target(
            name: "CapacitorAppmetricaPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm"),
                .product(name: "AppMetricaCore", package: "appmetrica-sdk-ios"),
                .product(name: "AppMetricaCrashes", package: "appmetrica-sdk-ios"),
                .product(name: "AppMetricaAdSupport", package: "appmetrica-sdk-ios"),
                .product(name: "AppMetricaWebKit", package: "appmetrica-sdk-ios"),
                .product(name: "AppMetricaScreenshot", package: "appmetrica-sdk-ios"),
                .product(name: "AppMetricaIDSync", package: "appmetrica-sdk-ios")
            ],
            path: "ios/Plugin",
            exclude: ["Info.plist", "Plugin.h"]
        )
    ]
)
