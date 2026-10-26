import { useRouter } from "expo-router";
import React, { useEffect, useState } from "react";
import { Alert, Platform, ScrollView, Text, View } from "react-native";
import { api } from "@/api/client";
import type {
  ActivityLevel,
  Goal,
  ProfileRequest,
  Sex,
} from "@/api/types";
import { Button, Field, OptionGroup } from "@/components/ui";
import { useSession } from "@/state/session";

const GOALS: { value: Goal; label: string }[] = [
  { value: "BULK", label: "Bulk" },
  { value: "CUT", label: "Cut" },
  { value: "RECOMP", label: "Recomp" },
];

const SEXES: { value: Sex; label: string }[] = [
  { value: "MALE", label: "Male" },
  { value: "FEMALE", label: "Female" },
];

const ACTIVITY: { value: ActivityLevel; label: string }[] = [
  { value: "SEDENTARY", label: "Sedentary" },
  { value: "LIGHT", label: "Light" },
  { value: "MODERATE", label: "Moderate" },
  { value: "ACTIVE", label: "Active" },
  { value: "VERY_ACTIVE", label: "Very active" },
];

function notify(title: string, message: string) {
  if (Platform.OS === "web") {
    // Alert.alert is a no-op on web; fall back to the browser dialog.
    window.alert(`${title}\n\n${message}`);
  } else {
    Alert.alert(title, message);
  }
}

export default function ProfileScreen() {
  const router = useRouter();
  const { profile, setProfile, loading } = useSession();

  const [name, setName] = useState("");
  const [sex, setSex] = useState<Sex | null>(null);
  const [goal, setGoal] = useState<Goal | null>(null);
  const [activity, setActivity] = useState<ActivityLevel | null>(null);
  const [age, setAge] = useState("");
  const [height, setHeight] = useState("");
  const [weight, setWeight] = useState("");
  const [saving, setSaving] = useState(false);

  // Hydrate the form if a profile already exists.
  useEffect(() => {
    if (profile) {
      setName(profile.name ?? "");
      setSex(profile.sex);
      setGoal(profile.goal);
      setActivity(profile.activityLevel);
      setAge(String(profile.age));
      setHeight(String(profile.heightCm));
      setWeight(String(profile.weightKg));
    }
  }, [profile]);

  async function onSubmit() {
    if (!sex || !goal || !activity) {
      notify("Missing info", "Pick your sex, goal, and activity level.");
      return;
    }
    const ageN = Number(age);
    const heightN = Number(height);
    const weightN = Number(weight);
    if (!ageN || !heightN || !weightN) {
      notify("Missing info", "Enter valid age, height, and weight.");
      return;
    }

    const body: ProfileRequest = {
      name: name.trim() || "Athlete",
      sex,
      goal,
      activityLevel: activity,
      age: ageN,
      heightCm: heightN,
      weightKg: weightN,
    };

    setSaving(true);
    try {
      const saved = profile
        ? await api.updateProfile(profile.id, body)
        : await api.createProfile(body);
      setProfile(saved);
      router.push("/diet");
    } catch (e) {
      notify("Couldn't save", (e as Error).message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <View className="flex-1 bg-ink" />;

  return (
    <ScrollView
      className="flex-1 bg-ink"
      contentContainerClassName="p-5 pb-16"
      keyboardShouldPersistTaps="handled"
    >
      <Text className="mb-1 text-2xl font-bold text-slate-50">
        Let's optimize your diet
      </Text>
      <Text className="mb-6 text-base text-slate-400">
        Tell us about you. We'll compute your targets for bulk, cut, or recomp.
      </Text>

      <OptionGroup label="Goal" options={GOALS} value={goal} onChange={setGoal} />
      <Field label="Name" value={name} onChangeText={setName} placeholder="Alex" />
      <OptionGroup label="Sex" options={SEXES} value={sex} onChange={setSex} />

      <View className="flex-row gap-3">
        <Field
          label="Age"
          value={age}
          onChangeText={setAge}
          keyboardType="numeric"
          placeholder="30"
          className="flex-1"
        />
        <Field
          label="Height (cm)"
          value={height}
          onChangeText={setHeight}
          keyboardType="numeric"
          placeholder="180"
          className="flex-1"
        />
        <Field
          label="Weight (kg)"
          value={weight}
          onChangeText={setWeight}
          keyboardType="numeric"
          placeholder="80"
          className="flex-1"
        />
      </View>

      <OptionGroup
        label="Activity level"
        options={ACTIVITY}
        value={activity}
        onChange={setActivity}
      />

      <Button
        label={profile ? "Update & continue" : "Continue"}
        onPress={onSubmit}
        loading={saving}
        className="mt-4"
      />
    </ScrollView>
  );
}
