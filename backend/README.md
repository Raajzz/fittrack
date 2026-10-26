# FitTrack — Backend

Standalone Spring Boot service that powers diet optimization for **bulk / cut / recomp**.
It is fully independent: it knows nothing about any particular frontend. Any client
(the React Native app in `../frontend`, a future web dashboard, a CLI, another service)
can use it by speaking the REST/JSON contract below.

## Tech
- Java 17, Spring Boot 3.2 (Web, Data JPA, Validation)
- H2 in-memory by default; swap to Postgres/MySQL via `DB_*` env vars — no code change
- Optional: Anthropic Java SDK (Claude Opus 4.8 vision) for photo → macro estimation

## Run locally
Requires JDK 17 and Maven.
```bash
cd backend
mvn spring-boot:run        # starts on http://localhost:8080
mvn test                   # runs the nutrition-engine unit tests
```

## Run with Docker (independent deployment)
```bash
cd backend
docker build -t fittrack-backend .
docker run -p 8080:8080 \
  -e CORS_ALLOWED_ORIGINS=https://your-frontend.example.com \
  fittrack-backend
```

## Configuration (all env-driven — deployment is decoupled from the build)
| Env var                 | Default                                   | Purpose                        |
|-------------------------|-------------------------------------------|--------------------------------|
| `PORT`                  | `8080`                                    | HTTP port                      |
| `DB_URL`                | `jdbc:h2:mem:fittrack;DB_CLOSE_DELAY=-1`  | JDBC URL                       |
| `DB_USERNAME`           | `sa`                                      | DB user                        |
| `DB_PASSWORD`           | *(empty)*                                 | DB password                    |
| `DB_DRIVER`             | `org.h2.Driver`                           | JDBC driver class              |
| `CORS_ALLOWED_ORIGINS`  | Expo dev ports                            | Comma-separated client origins |
| `ANTHROPIC_API_KEY`     | *(empty)*                                 | Enables Claude photo estimation; manual entry if unset |

## The nutrition engine (what makes this app useful)
1. **BMR** — Mifflin–St Jeor from sex/age/height/weight.
2. **TDEE** — BMR × activity multiplier (maintenance calories).
3. **Target** — TDEE × goal factor: **Bulk +12%**, **Cut −22%**, **Recomp ±0%**.
4. **Macros** — protein by bodyweight (1.8 g/kg bulk, 2.2 g/kg cut & recomp),
   fat at 25% of calories, carbs backfilled.
5. **Plan** — compares the target to the user's *logged* intake (averaged over
   logged days) and returns per-macro "eat more / eat less" adjustments.
6. **Alternatives** — converts any gap into concrete supplement/food swaps
   (whey, mass gainer, olive oil, oats, fiber).

---

## API contract (the only coupling point)

Base path: `/api`

### Profiles
`POST /api/profiles` — create. Body:
```json
{
  "name": "Alex",
  "sex": "MALE",                 // MALE | FEMALE
  "age": 30,
  "heightCm": 180,
  "weightKg": 80,
  "activityLevel": "MODERATE",   // SEDENTARY | LIGHT | MODERATE | ACTIVE | VERY_ACTIVE
  "goal": "BULK"                 // BULK | CUT | RECOMP
}
```
Returns `201` with the saved profile (includes generated `id`).

`GET /api/profiles/{id}` — fetch.
`PUT /api/profiles/{id}` — update (same body as create).

### Weekly diet logging
`PUT /api/users/{userId}/diet/day` — create/replace one weekday's meals. Body:
```json
{
  "weekday": "MONDAY",           // MONDAY ... SUNDAY
  "meals": [
    { "inputType": "TEXT",  "description": "200g chicken, 1 cup rice",
      "calories": 520, "protein": 55, "carbs": 45, "fat": 8 },
    { "inputType": "PHOTO", "description": "lunch photo",
      "photoUrl": "https://.../img.jpg" }   // macros optional; estimated later
  ]
}
```

`POST /api/users/{userId}/diet/copy` — copy one day onto many. Body:
```json
{ "sourceWeekday": "MONDAY", "targetWeekdays": ["TUESDAY", "WEDNESDAY", "THURSDAY"] }
```

`GET /api/users/{userId}/diet` — the full logged week.

### AI photo → macro estimation (optional)
Powered by Claude Opus 4.8 vision with structured output. Works only when
`ANTHROPIC_API_KEY` is set; otherwise these return `confident: false` with a note
asking the user to enter macros manually (the app stays fully functional).

`POST /api/meals/estimate` — stateless estimate (no saved meal needed). Body:
```json
{ "imageBase64": "<base64 bytes, no data: prefix>", "mediaType": "image/jpeg",
  "description": "optional text to refine the estimate" }
```
Returns:
```json
{ "macros": { "calories": 540, "protein": 42, "carbs": 38, "fat": 20 },
  "confident": true, "note": "Grilled chicken with rice; assumed a ~200g portion." }
```

`POST /api/meals/{mealId}/estimate` — estimate an already-logged meal and write the
macros back onto it (same body; base64 optional, falls back to the meal's stored
`photoUrl`/description). Marks the meal `estimated: true` so it counts toward the plan.

### Plan
`GET /api/users/{userId}/plan` — the crafted plan:
```json
{
  "userId": 1,
  "goal": "BULK",
  "bmr": 1780,
  "maintenanceCalories": 2759,
  "target":            { "calories": 3090, "protein": 144, "carbs": 430, "fat": 86 },
  "currentDailyAverage":{ "calories": 2400, "protein": 110, "carbs": 300, "fat": 70 },
  "dailyAdjustment":   { "calories": 690,  "protein": 34,  "carbs": 130, "fat": 16 },
  "loggedDays": 3,
  "guidance": ["Eat about 690 kcal MORE per day ...", "Add ~34g protein per day.", "..."],
  "alternatives": [
    { "name": "Whey protein", "reason": "You're ~34g short on protein...",
      "servingInfo": "~1 scoop", "perServing": {"calories":120,"protein":24,"carbs":3,"fat":1.5},
      "suggestedServings": 2 }
  ]
}
```

> **Note on photo meals:** photos are stored with their URL; macro estimation
> (manual or an AI vision service) is a pluggable step. A photo with no macros
> contributes 0 to the daily total until estimated, and is flagged `estimated=false`.
