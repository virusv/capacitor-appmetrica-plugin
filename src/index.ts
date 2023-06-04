import { registerPlugin } from '@capacitor/core';

import type { AppMetricaPlugin } from './definitions';

const AppMetrica = registerPlugin<AppMetricaPlugin>('AppMetrica', {
  web: () => import('./web').then(m => new m.AppMetricaWeb()),
});

export * from './definitions';
export { AppMetrica };
