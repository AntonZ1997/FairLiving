import { CanActivateFn } from '@angular/router';
import { clearLastHouseholdId, setLastHouseholdId } from './household-selection-service';

export const rememberHouseholdGuard: CanActivateFn = route => {
  const householdId = route.paramMap.get('householdId');

  if(householdId) {
    setLastHouseholdId(householdId)
  }

  return true;
};

export const forgetHouseholdGuard: CanActivateFn = () => {
  clearLastHouseholdId();
  return true;
}
