import AsyncStorage from "@react-native-async-storage/async-storage";
import React, {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import type { Profile } from "@/api/types";

const STORAGE_KEY = "fittrack.profile";

interface SessionValue {
  profile: Profile | null;
  loading: boolean;
  setProfile: (p: Profile) => void;
  clear: () => void;
}

const SessionContext = createContext<SessionValue | undefined>(undefined);

export function SessionProvider({ children }: { children: React.ReactNode }) {
  const [profile, setProfileState] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    AsyncStorage.getItem(STORAGE_KEY)
      .then((raw) => {
        if (raw) setProfileState(JSON.parse(raw));
      })
      .finally(() => setLoading(false));
  }, []);

  const value = useMemo<SessionValue>(
    () => ({
      profile,
      loading,
      setProfile: (p) => {
        setProfileState(p);
        AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(p)).catch(() => {});
      },
      clear: () => {
        setProfileState(null);
        AsyncStorage.removeItem(STORAGE_KEY).catch(() => {});
      },
    }),
    [profile, loading],
  );

  return (
    <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
  );
}

export function useSession(): SessionValue {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error("useSession must be used within SessionProvider");
  return ctx;
}
