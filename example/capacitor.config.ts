import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'ru.inaliv.demoapp.appmetrica',
  appName: 'capacitor-appmetrica-demoapp',
  webDir: 'dist',
  plugins: {
    SplashScreen: {
      launchShowDuration: 0,
    },
  },
  cordova: {}
};

export default config;
