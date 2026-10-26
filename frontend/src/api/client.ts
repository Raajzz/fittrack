import type {
  CopyDayRequest,
  DietDayRequest,
  EstimateRequest,
  EstimationResult,
  PlanResponse,
  Profile,
  ProfileRequest,
} from "./types";

// The backend base URL is the ONLY wiring between the two apps.
// Set EXPO_PUBLIC_API_URL at build/run time; defaults to local dev.
const BASE_URL =
  process.env.EXPO_PUBLIC_API_URL?.replace(/\/$/, "") ?? "http://localhost:8080";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body?.error) message = body.error;
    } catch {
      // non-JSON error body; keep the default message
    }
    throw new Error(message);
  }
  // Some endpoints (rare) may return empty bodies.
  const text = await res.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
}

export const api = {
  baseUrl: BASE_URL,

  createProfile(body: ProfileRequest): Promise<Profile> {
    return request<Profile>("/api/profiles", {
      method: "POST",
      body: JSON.stringify(body),
    });
  },

  updateProfile(id: number, body: ProfileRequest): Promise<Profile> {
    return request<Profile>(`/api/profiles/${id}`, {
      method: "PUT",
      body: JSON.stringify(body),
    });
  },

  logDay(userId: number, body: DietDayRequest): Promise<unknown> {
    return request(`/api/users/${userId}/diet/day`, {
      method: "PUT",
      body: JSON.stringify(body),
    });
  },

  copyDay(userId: number, body: CopyDayRequest): Promise<unknown> {
    return request(`/api/users/${userId}/diet/copy`, {
      method: "POST",
      body: JSON.stringify(body),
    });
  },

  getPlan(userId: number): Promise<PlanResponse> {
    return request<PlanResponse>(`/api/users/${userId}/plan`);
  },

  /** Stateless macro estimate from a photo (base64) and/or description. */
  estimateMeal(body: EstimateRequest): Promise<EstimationResult> {
    return request<EstimationResult>("/api/meals/estimate", {
      method: "POST",
      body: JSON.stringify(body),
    });
  },
};
