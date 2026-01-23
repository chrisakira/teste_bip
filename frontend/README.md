# Frontend (Angular 17)

Single-page dashboard that consumes the Spring Boot backend (`backend-module`) to manage benefícios and perform saldo transfers with optimistic locking guarantees.

## Stack & Highlights

- Angular 17 standalone app with SSR enabled
- Dark-blue visual system inspired by night terminals
- Reactive Forms for CRUD + transfer flows
- `BeneficioService` centralizes REST access to `/api/v1/beneficios`
- Http interceptors replaced by `provideHttpClient(withFetch())`

## Running locally

1. Install dependencies once:
	```bash
	cd frontend
	npm install
	```
2. Make sure the backend is running (default: `http://localhost:8080`).
3. Start the dev server:
	```bash
	npm start
	```
4. Open [http://localhost:4200](http://localhost:4200) to use the dashboard.

## API base URL

Change `API_BASE_URL` in `src/app/config/api.config.ts` if the backend runs elsewhere (e.g. containers or tunnels).

## Tests

Run the unit suite with:

```bash
npm run test -- --watch=false --browsers=ChromeHeadless
```

The provided specs mock HTTP calls to validate CRUD behavior and form validation rules.

## Build

```bash
npm run build
```

Artifacts will be generated in `dist/bip-app/` (including the SSR server bundle).
