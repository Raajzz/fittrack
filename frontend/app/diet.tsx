import { useRouter } from "expo-router";
import * as ImagePicker from "expo-image-picker";
import React, { useState } from "react";
import {
  Alert,
  Image,
  Platform,
  Pressable,
  ScrollView,
  Text,
  View,
} from "react-native";
import { api } from "@/api/client";
import type { MealInput, Weekday } from "@/api/types";
import { Button, Card, Field } from "@/components/ui";
import { useSession } from "@/state/session";

const WEEKDAYS: { value: Weekday; label: string }[] = [
  { value: "MONDAY", label: "Mon" },
  { value: "TUESDAY", label: "Tue" },
  { value: "WEDNESDAY", label: "Wed" },
  { value: "THURSDAY", label: "Thu" },
  { value: "FRIDAY", label: "Fri" },
  { value: "SATURDAY", label: "Sat" },
  { value: "SUNDAY", label: "Sun" },
];

type Week = Record<Weekday, MealInput[]>;

const emptyWeek: Week = {
  MONDAY: [],
  TUESDAY: [],
  WEDNESDAY: [],
  THURSDAY: [],
  FRIDAY: [],
  SATURDAY: [],
  SUNDAY: [],
};

function notify(title: string, message: string) {
  if (Platform.OS === "web") window.alert(`${title}\n\n${message}`);
  else Alert.alert(title, message);
}

function num(v: string): number | null {
  const n = Number(v);
  return v.trim() === "" || Number.isNaN(n) ? null : n;
}

export default function DietScreen() {
  const router = useRouter();
  const { profile } = useSession();

  const [week, setWeek] = useState<Week>(emptyWeek);
  const [day, setDay] = useState<Weekday>("MONDAY");
  const [copyTargets, setCopyTargets] = useState<Weekday[]>([]);
  const [busy, setBusy] = useState(false);

  // New-meal form
  const [desc, setDesc] = useState("");
  const [cal, setCal] = useState("");
  const [pro, setPro] = useState("");
  const [carb, setCarb] = useState("");
  const [fat, setFat] = useState("");
  const [photoUri, setPhotoUri] = useState<string | null>(null);
  const [photoBase64, setPhotoBase64] = useState<string | null>(null);
  const [photoMime, setPhotoMime] = useState<string | null>(null);
  const [estimating, setEstimating] = useState(false);

  if (!profile) {
    return (
      <View className="flex-1 items-center justify-center bg-ink p-6">
        <Text className="mb-4 text-center text-slate-300">
          Set up your profile first.
        </Text>
        <Button label="Go to profile" onPress={() => router.replace("/")} />
      </View>
    );
  }

  const meals = week[day];

  function resetForm() {
    setDesc("");
    setCal("");
    setPro("");
    setCarb("");
    setFat("");
    setPhotoUri(null);
    setPhotoBase64(null);
    setPhotoMime(null);
  }

  async function pickPhoto() {
    const perm = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!perm.granted) {
      notify("Permission needed", "Allow photo access to log meals from pictures.");
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.5,
      base64: true, // needed so we can send bytes to the AI estimator
    });
    if (!result.canceled) {
      const asset = result.assets[0];
      setPhotoUri(asset.uri);
      setPhotoBase64(asset.base64 ?? null);
      setPhotoMime(asset.mimeType ?? "image/jpeg");
    }
  }

  async function estimatePhoto() {
    if (!photoBase64) {
      notify("No photo", "Add a photo first, then estimate.");
      return;
    }
    setEstimating(true);
    try {
      const result = await api.estimateMeal({
        imageBase64: photoBase64,
        mediaType: photoMime,
        description: desc.trim() || undefined,
      });
      if (!result.confident) {
        notify("Couldn't estimate", result.note);
        return;
      }
      const m = result.macros;
      setCal(String(Math.round(m.calories)));
      setPro(String(Math.round(m.protein)));
      setCarb(String(Math.round(m.carbs)));
      setFat(String(Math.round(m.fat)));
      notify("Estimated", result.note);
    } catch (e) {
      notify("Estimation failed", (e as Error).message);
    } finally {
      setEstimating(false);
    }
  }

  function addMeal() {
    if (!desc.trim() && !photoUri) {
      notify("Empty meal", "Add a description or a photo.");
      return;
    }
    const meal: MealInput = photoUri
      ? {
          inputType: "PHOTO",
          description: desc.trim() || "Meal photo",
          photoUrl: photoUri,
          calories: num(cal),
          protein: num(pro),
          carbs: num(carb),
          fat: num(fat),
        }
      : {
          inputType: "TEXT",
          description: desc.trim(),
          calories: num(cal),
          protein: num(pro),
          carbs: num(carb),
          fat: num(fat),
        };
    setWeek((w) => ({ ...w, [day]: [...w[day], meal] }));
    resetForm();
  }

  function removeMeal(index: number) {
    setWeek((w) => ({ ...w, [day]: w[day].filter((_, i) => i !== index) }));
  }

  async function saveDay() {
    if (!profile) return;
    setBusy(true);
    try {
      await api.logDay(profile.id, { weekday: day, meals });
      notify("Saved", `${day[0] + day.slice(1).toLowerCase()} logged.`);
    } catch (e) {
      notify("Couldn't save", (e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  function toggleCopyTarget(d: Weekday) {
    setCopyTargets((t) =>
      t.includes(d) ? t.filter((x) => x !== d) : [...t, d],
    );
  }

  async function copyDay() {
    if (!profile) return;
    if (copyTargets.length === 0) {
      notify("Pick days", "Select which days to copy this one to.");
      return;
    }
    setBusy(true);
    try {
      // Persist the source first so the backend has it, then copy.
      await api.logDay(profile.id, { weekday: day, meals });
      await api.copyDay(profile.id, {
        sourceWeekday: day,
        targetWeekdays: copyTargets,
      });
      setWeek((w) => {
        const next = { ...w };
        copyTargets.forEach((t) => {
          if (t !== day) next[t] = meals.map((m) => ({ ...m }));
        });
        return next;
      });
      setCopyTargets([]);
      notify("Copied", `Copied ${day.toLowerCase()} to ${copyTargets.length} day(s).`);
    } catch (e) {
      notify("Couldn't copy", (e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  async function generatePlan() {
    if (!profile) return;
    setBusy(true);
    try {
      // Flush any days that have meals but may not be saved yet.
      await Promise.all(
        (Object.keys(week) as Weekday[])
          .filter((d) => week[d].length > 0)
          .map((d) => api.logDay(profile.id, { weekday: d, meals: week[d] })),
      );
      router.push("/plan");
    } catch (e) {
      notify("Couldn't save days", (e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <ScrollView
      className="flex-1 bg-ink"
      contentContainerClassName="p-5 pb-16"
      keyboardShouldPersistTaps="handled"
    >
      <Text className="mb-3 text-base text-slate-400">
        Log what you eat each day — type it in or snap a photo. Then copy a day to
        others if your meals repeat.
      </Text>

      {/* Day selector */}
      <View className="mb-4 flex-row flex-wrap gap-2">
        {WEEKDAYS.map((d) => {
          const active = d.value === day;
          const count = week[d.value].length;
          return (
            <Pressable
              key={d.value}
              onPress={() => setDay(d.value)}
              className={`rounded-xl border px-3 py-2 ${
                active ? "border-brand bg-brand/20" : "border-slate-700 bg-surface"
              }`}
            >
              <Text
                className={`text-sm font-semibold ${
                  active ? "text-brand" : "text-slate-300"
                }`}
              >
                {d.label}
                {count > 0 ? ` · ${count}` : ""}
              </Text>
            </Pressable>
          );
        })}
      </View>

      {/* Logged meals for the day */}
      {meals.length === 0 ? (
        <Card className="mb-4">
          <Text className="text-slate-400">No meals logged for this day yet.</Text>
        </Card>
      ) : (
        meals.map((m, i) => (
          <Card key={i} className="mb-3">
            <View className="flex-row items-start justify-between">
              <View className="flex-1 pr-3">
                <Text className="mb-1 text-xs font-semibold uppercase text-brand">
                  {m.inputType === "PHOTO" ? "Photo" : "Text"}
                </Text>
                <Text className="text-base text-slate-100">{m.description}</Text>
                {m.photoUrl ? (
                  <Image
                    source={{ uri: m.photoUrl }}
                    className="mt-2 h-32 w-full rounded-xl"
                    resizeMode="cover"
                  />
                ) : null}
                <Text className="mt-2 text-sm text-slate-400">
                  {m.calories != null ? `${m.calories} kcal` : "kcal —"} ·{" "}
                  {m.protein != null ? `${m.protein}P` : "—P"} ·{" "}
                  {m.carbs != null ? `${m.carbs}C` : "—C"} ·{" "}
                  {m.fat != null ? `${m.fat}F` : "—F"}
                </Text>
              </View>
              <Pressable onPress={() => removeMeal(i)} className="px-2 py-1">
                <Text className="text-cut">Remove</Text>
              </Pressable>
            </View>
          </Card>
        ))
      )}

      {/* Add meal */}
      <Card className="mb-4">
        <Text className="mb-3 text-base font-semibold text-slate-100">
          Add a meal
        </Text>
        <Field
          label="What did you eat?"
          value={desc}
          onChangeText={setDesc}
          placeholder="200g chicken breast, 1 cup rice"
        />
        <View className="flex-row gap-2">
          <Field label="kcal" value={cal} onChangeText={setCal} keyboardType="numeric" className="flex-1" placeholder="520" />
          <Field label="P (g)" value={pro} onChangeText={setPro} keyboardType="numeric" className="flex-1" placeholder="55" />
          <Field label="C (g)" value={carb} onChangeText={setCarb} keyboardType="numeric" className="flex-1" placeholder="45" />
          <Field label="F (g)" value={fat} onChangeText={setFat} keyboardType="numeric" className="flex-1" placeholder="8" />
        </View>

        {photoUri ? (
          <Image
            source={{ uri: photoUri }}
            className="mb-3 h-32 w-full rounded-xl"
            resizeMode="cover"
          />
        ) : null}

        {photoUri ? (
          <Button
            label="Estimate macros with AI"
            variant="ghost"
            onPress={estimatePhoto}
            loading={estimating}
            className="mb-3"
          />
        ) : null}

        <View className="flex-row gap-3">
          <Button
            label={photoUri ? "Change photo" : "Add photo"}
            variant="ghost"
            onPress={pickPhoto}
            className="flex-1"
          />
          <Button label="Add meal" onPress={addMeal} className="flex-1" />
        </View>
        <Text className="mt-2 text-xs text-slate-500">
          Add a photo and tap “Estimate macros with AI,” or type the macros
          yourself. For photos they can also be left blank and estimated later.
        </Text>
      </Card>

      {/* Copy day */}
      <Card className="mb-4">
        <Text className="mb-2 text-base font-semibold text-slate-100">
          Copy this day to…
        </Text>
        <View className="mb-2 flex-row flex-wrap gap-2">
          {WEEKDAYS.filter((d) => d.value !== day).map((d) => {
            const active = copyTargets.includes(d.value);
            return (
              <Pressable
                key={d.value}
                onPress={() => toggleCopyTarget(d.value)}
                className={`rounded-full border px-3 py-1.5 ${
                  active ? "border-brand bg-brand/20" : "border-slate-700 bg-ink"
                }`}
              >
                <Text className={active ? "text-brand" : "text-slate-400"}>
                  {d.label}
                </Text>
              </Pressable>
            );
          })}
        </View>
        <Button
          label="Copy to selected days"
          variant="ghost"
          onPress={copyDay}
          loading={busy}
        />
      </Card>

      <View className="flex-row gap-3">
        <Button
          label="Save day"
          variant="ghost"
          onPress={saveDay}
          loading={busy}
          className="flex-1"
        />
        <Button
          label="Generate plan"
          onPress={generatePlan}
          loading={busy}
          className="flex-1"
        />
      </View>
    </ScrollView>
  );
}
