declare module '@capacitor/core' {
  interface PluginRegistry {
    AppMetrica: AppMetricaPlugin;
  }
}

export interface AppMetricaPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
