import { ApplicationConfig, inject, isDevMode, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { providePrimeNG } from 'primeng/config';
import Material from '@primeuix/themes/material';
import { definePreset } from '@primeuix/themes';
import { provideTransloco, TranslocoLoader } from '@jsverse/transloco';
import { provideTranslocoLocale } from '@jsverse/transloco-locale';
import { provideOAuthClient } from 'angular-oauth2-oidc';

import { routes } from './app.routes';
import { MessageService } from 'primeng/api';
import { loadingInterceptor } from '@/interceptors/loading.interceptor';
import { errorInterceptor } from '@/interceptors/error.interceptor';
import { provideApiConfiguration } from '@/api/backend/api-configuration';
import { environment } from '@/environments/environment';
import { AuthService } from '@/services/auth.service';

import fr from '@/i18n/fr.json';
import en from '@/i18n/en.json';

const translations: Record<string, Record<string, string>> = { fr, en };

export class InlineTranslocoLoader implements TranslocoLoader {
  getTranslation(lang: string) {
    return Promise.resolve(translations[lang]);
  }
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([loadingInterceptor, errorInterceptor]), withInterceptorsFromDi()),
    provideOAuthClient({
      resourceServer: {
        allowedUrls: [environment.apiBaseUrl || '/api'],
        sendAccessToken: true,
      },
    }),
    provideAppInitializer(() => inject(AuthService).init()),
    MessageService, // pour les Toaster
    provideApiConfiguration(environment.apiBaseUrl),
    provideTransloco({
      config: {
        availableLangs: ['fr', 'en'],
        defaultLang: 'fr',
        reRenderOnLangChange: true,
        prodMode: !isDevMode(),
      },
      loader: InlineTranslocoLoader,
    }),
    provideTranslocoLocale({
      langToLocaleMapping: {
        fr: 'fr-FR',
        en: 'en-GB',
      },
    }),
    providePrimeNG({
      theme: {
        preset: definePreset(Material, {
          semantic: {
            primary: {
              50: '{blue.50}',
              100: '{blue.100}',
              200: '{blue.200}',
              300: '{blue.300}',
              400: '{blue.400}',
              500: '{blue.500}',
              600: '{blue.600}',
              700: '{blue.700}',
              800: '{blue.800}',
              900: '{blue.900}',
              950: '{blue.950}',
            },
          },
        }),
      },
    }),
  ],
};
