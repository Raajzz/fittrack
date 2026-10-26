import { useRouter } from "expo-router";
import React, { useCallback, useEffect, useState } from "react";
import { ActivityIndicator, ScrollView, Text, View } from "react-native";
import { api } from "@/api/client";
import type { Goal, Macros, PlanResponse } from "@/api/types";
import { Button, Card } from "@/components/ui";
import { useSession } from "@/state/session";

const GOAL_COLOR: Record<Goal, string> = {
  BULK: "text-bulk",
  CUT: "text-cut",
  RECOMP: "text-recomp",
};

function MacroRow({ macros }: { macros: Macros }) {
  return (
    <View className="flex-row justify-between">
      {[
        { k: "Protein", v: macros.protein, s: "g" },
        { k: "Carbs", v: macros.carbs, s: "g" },
        { k: "Fat", v: macros.fat, s: "g" },
      ].map((m) => (
        <View key={m.k} className="items-center">
          <Text className="text-lg font-bold text-slate-100">
            {Math.round(m.v)}
            {m.s}
          </Text>
          <Text className="text-xs text-slate-400">{m.k}</Text>
        </View>
      ))}
    </View>
  );
}

function AdjustmentPill({ label, value, unit }: { label: string; value: number; unit: string }) {
  const up = value > 0;
  const flat = Math.abs(value) < (unit === "kcal" ? 75 : 10);
  const color = flat ? "text-slate-300" : up ? "text-brand" : "text-cut";
  const sign = flat ? "±" : up ? "+" : "−";
  return (
    <View className="items-center">
      <Text className={`text-base font-bold ${color}`}>
        {sign}
        {Math.abs(Math.round(value))}
        {unit === "kcal" ? "" : unit}
      </Text>
      <Text className="text-xs text-slate-400">{label}</Text>
    </View>
  );
}

export default function PlanScreen() {
  const router = useRouter();
  const { profile } = useSession();
  const [plan, setPlan] = useState<PlanResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    if (!profile) return;
    setLoading(true);
    setError(null);
    try {
      setPlan(await api.getPlan(profile.id));
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  }, [profile]);

  useEffect(() => {
    load();
  }, [load]);

  if (!profile) {
    return (
      <View className="flex-1 items-center justify-center bg-ink p-6">
        <Text className="mb-4 text-slate-300">Set up your profile first.</Text>
        <Button label="Go to profile" onPress={() => router.replace("/")} />
      </View>
    );
  }

  if (loading) {
    return (
      <View className="flex-1 items-center justify-center bg-ink">
        <ActivityIndicator color="#22c55e" size="large" />
      </View>
    );
  }

  if (error || !plan) {
    return (
      <View className="flex-1 items-center justify-center bg-ink p-6">
        <Text className="mb-4 text-center text-cut">{error ?? "No plan yet."}</Text>
        <Button label="Retry" onPress={load} />
      </View>
    );
  }

  return (
    <ScrollView className="flex-1 bg-ink" contentContainerClassName="p-5 pb-16">
      {/* Goal + maintenance */}
      <View className="mb-4 flex-row items-end justify-between">
        <View>
          <Text className="text-sm text-slate-400">Your goal</Text>
          <Text className={`text-2xl font-bold ${GOAL_COLOR[plan.goal]}`}>
            {plan.goal}
          </Text>
        </View>
        <View className="items-end">
          <Text className="text-sm text-slate-400">Maintenance</Text>
          <Text className="text-xl font-semibold text-slate-100">
            {Math.round(plan.maintenanceCalories)} kcal
          </Text>
        </View>
      </View>

      {/* Daily target */}
      <Card className="mb-4">
        <Text className="mb-1 text-sm text-slate-400">Daily target</Text>
        <Text className="mb-3 text-3xl font-bold text-slate-50">
          {Math.round(plan.target.calories)}{" "}
          <Text className="text-lg font-normal text-slate-400">kcal</Text>
        </Text>
        <MacroRow macros={plan.target} />
      </Card>

      {/* Adjustment vs. current */}
      <Card className="mb-4">
        <Text className="mb-1 text-sm text-slate-400">
          Adjust from your current intake
          {plan.loggedDays > 0
            ? ` (avg of ${plan.loggedDays} logged day${plan.loggedDays === 1 ? "" : "s"})`
            : ""}
        </Text>
        <View className="mt-2 flex-row justify-between">
          <AdjustmentPill label="kcal/day" value={plan.dailyAdjustment.calories} unit="kcal" />
          <AdjustmentPill label="Protein" value={plan.dailyAdjustment.protein} unit="g" />
          <AdjustmentPill label="Carbs" value={plan.dailyAdjustment.carbs} unit="g" />
          <AdjustmentPill label="Fat" value={plan.dailyAdjustment.fat} unit="g" />
        </View>
      </Card>

      {/* Guidance */}
      <Card className="mb-4">
        <Text className="mb-2 text-base font-semibold text-slate-100">Coaching</Text>
        {plan.guidance.map((line, i) => (
          <View key={i} className="mb-2 flex-row">
            <Text className="mr-2 text-brand">•</Text>
            <Text className="flex-1 text-slate-200">{line}</Text>
          </View>
        ))}
      </Card>

      {/* Alternatives / supplements */}
      {plan.alternatives.length > 0 && (
        <View className="mb-4">
          <Text className="mb-2 text-base font-semibold text-slate-100">
            Can't eat that much? Alternatives
          </Text>
          {plan.alternatives.map((s, i) => (
            <Card key={i} className="mb-3">
              <View className="flex-row items-center justify-between">
                <Text className="text-base font-semibold text-slate-50">{s.name}</Text>
                <Text className="text-sm font-semibold text-brand">
                  ×{s.suggestedServings}
                </Text>
              </View>
              <Text className="mt-1 text-sm text-slate-300">{s.reason}</Text>
              <Text className="mt-2 text-xs text-slate-400">
                {s.servingInfo} · {Math.round(s.perServing.calories)} kcal,{" "}
                {Math.round(s.perServing.protein)}P / {Math.round(s.perServing.carbs)}C /{" "}
                {Math.round(s.perServing.fat)}F per serving
              </Text>
            </Card>
          ))}
        </View>
      )}

      <View className="flex-row gap-3">
        <Button
          label="Edit diet"
          variant="ghost"
          onPress={() => router.push("/diet")}
          className="flex-1"
        />
        <Button label="Refresh" onPress={load} className="flex-1" />
      </View>
    </ScrollView>
  );
}
