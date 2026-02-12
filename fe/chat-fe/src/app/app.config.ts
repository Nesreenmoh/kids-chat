import { ApplicationConfig, provideBrowserGlobalErrorListeners,importProvidersFrom } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(), // HttpClientModule replacement
    importProvidersFrom(FormsModule) // FormsModule replacement
  ]
};
