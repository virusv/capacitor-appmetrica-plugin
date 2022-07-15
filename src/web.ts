import { WebPlugin } from '@capacitor/core';
import { AppMetricaPlugin } from './definitions';

export class AppMetricaWeb extends WebPlugin implements AppMetricaPlugin {
  constructor() {
    super({
      name: 'AppMetrica',
      platforms: ['web'],
    });
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}

const AppMetrica = new AppMetricaWeb();

export { AppMetrica };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(AppMetrica);
