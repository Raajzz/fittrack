# FitTrack — Frontend

Cross-platform app (**iOS, Android, Web**) built with **Expo (React Native)** and
**NativeWind (Tailwind CSS)**. It is a standalone client: its only dependency on the
backend is the HTTP base URL in `EXPO_PUBLIC_API_URL`. Swap the backend for any
service that implements the same REST contract and this app doesn't change.

## Tech
- Expo SDK 51, React Native 0.74, expo-router (file-based nav)
- NativeWind 4 (Tailwind utility classes in RN)
- TypeScript, AsyncStorage (keeps the active profile across reloads)

## Screens
| Route        | Purpose                                                              |
|--------------|---------------------------------------------------------------------|
| `/` (index)  | Onboarding — body stats + goal (bulk / cut / recomp)                 |
| `/diet`      | Weekly diet logging (Mon–Sun), text **or** photo, copy a day to many |
| `/plan`      | Crafted plan — target macros, eat-more/less deltas, supplement swaps |

## Setup
Requires Node 18+.
```bash
cd frontend
npm install
cp .env.example .env        # then edit EXPO_PUBLIC_API_URL if needed
npm run start               # press w (web), a (Android), i (iOS)
```

### Pointing at the backend
Set `EXPO_PUBLIC_API_URL` in `.env`:
- Web / iOS simulator → `http://localhost:8080`
- Android emulator → `http://10.0.2.2:8080`
- Physical device → your machine's LAN IP, e.g. `http://192.168.1.20:8080`
- Production → `https://api.yourdomain.com`

## Deploying (independent of the backend)
**Web (static):**
```bash
npm run export:web          # outputs ./dist
# or build a container with the API URL baked in:
docker build --build-arg EXPO_PUBLIC_API_URL=https://api.yourdomain.com -t fittrack-web .
docker run -p 8080:80 fittrack-web
```
Any static host works too (Netlify, Vercel, S3+CloudFront, nginx) — serve `dist/`.

**Native (iOS/Android):** build with EAS and submit to the stores:
```bash
npx eas build --platform all
```

## Notes
- **Photo meals + AI estimation:** the picker captures the image (incl. base64).
  Tap **“Estimate macros with AI”** to send the bytes to `POST /api/meals/estimate`;
  the backend (Claude Opus 4.8 vision) returns estimated macros that pre-fill the
  form. If the backend has no `ANTHROPIC_API_KEY`, estimation returns a
  not-confident result and you enter macros manually — the app works either way.
  Macros can also be left blank and estimated later on the saved meal.
- The entire backend integration lives in `src/api/` — `types.ts` (contract) and
  `client.ts` (fetch wrapper). That's the only place to touch if the API moves.
