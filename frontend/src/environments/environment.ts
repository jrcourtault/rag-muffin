export const environment = {
  production: false,
  apiBaseUrl: '', // Vide en dev : les appels /api/* passent par le proxy Angular (proxy.conf.json → localhost:8080)
  keycloak: {
    issuer: 'http://localhost:8081/realms/rag-muffin',
    clientId: 'frontend',
  },
};
