// Mirrors the backend REST contract (see ../../backend/README.md).
// This is the ONLY place the frontend assumes anything about the backend —
// swap the backend implementation freely as long as it honors these shapes.

export type Sex = "MALE" | "FEMALE";
export type Goal = "BULK" | "CUT" | "RECOMP";
export type ActivityLevel =
  | "SEDENTARY"
  | "LIGHT"
  | "MODERATE"
  | "ACTIVE"
  | "VERY_ACTIVE";
export type Weekday =
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";
export type MealInputType = "TEXT" | "PHOTO";

export interface ProfileRequest {
  name: string;
  sex: Sex;
  age: number;
  heightCm: number;
  weightKg: number;
  activityLevel: ActivityLevel;
  goal: Goal;
}

export interface Profile extends ProfileRequest {
  id: number;
}

export interface MealInput {
  inputType: MealInputType;
  description?: string;
  photoUrl?: string;
  calories?: number | null;
  protein?: number | null;
  carbs?: number | null;
  fat?: number | null;
}

export interface DietDayRequest {
  weekday: Weekday;
  meals: MealInput[];
}

export interface CopyDayRequest {
  sourceWeekday: Weekday;
  targetWeekdays: Weekday[];
}

export interface Macros {
  calories: number;
  protein: number;
  carbs: number;
  fat: number;
}

export interface SupplementSuggestion {
  name: string;
  reason: string;
  servingInfo: string;
  perServing: Macros;
  suggestedServings: number;
}

export interface EstimateRequest {
  imageBase64?: string | null;
  mediaType?: string | null;
  description?: string | null;
  photoUrl?: string | null;
}

export interface EstimationResult {
  macros: Macros;
  confident: boolean;
  note: string;
}

export interface PlanResponse {
  userId: number;
  goal: Goal;
  bmr: number;
  maintenanceCalories: number;
  target: Macros;
  currentDailyAverage: Macros;
  dailyAdjustment: Macros;
  loggedDays: number;
  guidance: string[];
  alternatives: SupplementSuggestion[];
}
