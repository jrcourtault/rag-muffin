import { Injectable, inject } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';

import { authConfig } from '@/auth.config';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private oauthService = inject(OAuthService);

  get email(): string {
    return this.oauthService.getIdentityClaims()?.['email'] ?? '';
  }

  get isAuthenticated(): boolean {
    return this.oauthService.hasValidAccessToken();
  }

  get isAdmin(): boolean {
    const token = this.oauthService.getAccessToken();
    if (!token) return false;
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(atob(base64));
    const roles: string[] = payload?.realm_access?.roles ?? [];
    return roles.includes('ADMIN');
  }

  async init(): Promise<void> {
    this.oauthService.configure(authConfig);
    this.oauthService.setupAutomaticSilentRefresh();
    await this.oauthService.loadDiscoveryDocumentAndTryLogin();
    if (!this.oauthService.hasValidAccessToken()) {
      this.oauthService.initCodeFlow();
      // Retourne une Promise qui ne se résout jamais : l'APP_INITIALIZER reste bloqué,
      // Angular ne rend rien, et le navigateur redirige vers Keycloak sans flash.
      // La Promise pendante est garbage-collectée lors de la navigation.
      return new Promise<void>(() => {});
    }
  }

  logout(): void {
    this.oauthService.logOut();
  }
}
