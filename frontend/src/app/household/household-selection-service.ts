
const LOCAL_STORAGE_KEY: string = 'fairliving.lastHouseholdId';


  export function getLastHouseholdId(): string | null {
    try {
      return localStorage.getItem(LOCAL_STORAGE_KEY);
    } catch {
      return null;
    }
  }

  export function setLastHouseholdId(householdId: string): void {
    try {
      localStorage.setItem(LOCAL_STORAGE_KEY, householdId);
    } catch {}
  }

  export function clearLastHouseholdId(): void {
    try {
      localStorage.removeItem(LOCAL_STORAGE_KEY);
    } catch {}
  }

