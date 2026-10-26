import React from "react";
import {
  ActivityIndicator,
  Pressable,
  Text,
  TextInput,
  TextInputProps,
  View,
} from "react-native";

export function Card({
  children,
  className = "",
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <View className={`rounded-2xl bg-surface p-4 ${className}`}>{children}</View>
  );
}

export function Button({
  label,
  onPress,
  variant = "primary",
  loading = false,
  disabled = false,
  className = "",
}: {
  label: string;
  onPress: () => void;
  variant?: "primary" | "ghost";
  loading?: boolean;
  disabled?: boolean;
  className?: string;
}) {
  const base =
    variant === "primary"
      ? "bg-brand active:bg-brand-dark"
      : "bg-transparent border border-slate-600 active:bg-slate-800";
  const isDisabled = disabled || loading;
  return (
    <Pressable
      onPress={onPress}
      disabled={isDisabled}
      className={`flex-row items-center justify-center rounded-xl px-5 py-3 ${base} ${
        isDisabled ? "opacity-50" : ""
      } ${className}`}
    >
      {loading ? (
        <ActivityIndicator color="#fff" />
      ) : (
        <Text
          className={`text-base font-semibold ${
            variant === "primary" ? "text-ink" : "text-slate-100"
          }`}
        >
          {label}
        </Text>
      )}
    </Pressable>
  );
}

export function Field({
  label,
  className = "",
  ...props
}: { label: string } & TextInputProps & { className?: string }) {
  return (
    <View className={`mb-3 ${className}`}>
      <Text className="mb-1 text-sm font-medium text-slate-300">{label}</Text>
      <TextInput
        placeholderTextColor="#64748b"
        className="rounded-xl border border-slate-700 bg-ink px-4 py-3 text-base text-slate-100"
        {...props}
      />
    </View>
  );
}

export function OptionGroup<T extends string>({
  label,
  options,
  value,
  onChange,
}: {
  label: string;
  options: { value: T; label: string }[];
  value: T | null;
  onChange: (v: T) => void;
}) {
  return (
    <View className="mb-3">
      <Text className="mb-2 text-sm font-medium text-slate-300">{label}</Text>
      <View className="flex-row flex-wrap gap-2">
        {options.map((opt) => {
          const active = opt.value === value;
          return (
            <Pressable
              key={opt.value}
              onPress={() => onChange(opt.value)}
              className={`rounded-full border px-4 py-2 ${
                active
                  ? "border-brand bg-brand/20"
                  : "border-slate-700 bg-surface"
              }`}
            >
              <Text
                className={`text-sm font-medium ${
                  active ? "text-brand" : "text-slate-300"
                }`}
              >
                {opt.label}
              </Text>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}
