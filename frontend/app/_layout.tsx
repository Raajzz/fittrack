import "../global.css";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { SessionProvider } from "@/state/session";

export default function RootLayout() {
  return (
    <SafeAreaProvider>
      <SessionProvider>
        <StatusBar style="light" />
        <Stack
          screenOptions={{
            headerStyle: { backgroundColor: "#0f172a" },
            headerTintColor: "#f1f5f9",
            headerTitleStyle: { fontWeight: "700" },
            contentStyle: { backgroundColor: "#0f172a" },
          }}
        >
          <Stack.Screen name="index" options={{ title: "Your Profile" }} />
          <Stack.Screen name="diet" options={{ title: "Weekly Diet" }} />
          <Stack.Screen name="plan" options={{ title: "Your Plan" }} />
        </Stack>
      </SessionProvider>
    </SafeAreaProvider>
  );
}
