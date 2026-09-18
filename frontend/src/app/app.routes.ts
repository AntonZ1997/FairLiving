import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { authGuard } from './auth/auth.guard';
import { getLastHouseholdId } from './household/household-selection-service';
import { forgetHouseholdGuard, rememberHouseholdGuard } from './household/household.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register').then(m => m.Register)
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: () => {
          const householdId = getLastHouseholdId();
          return householdId ? `/households/${householdId}` : '/households';
        }
      },
      {
        path: 'join/:invitationId',
        redirectTo: route => `/households?join=${encodeURIComponent(route.params['invitationId'])}`
      },
      {
        path: 'households',
        canActivate: [forgetHouseholdGuard],
        loadComponent: () => import('./household/household-list/household-list').then(m => m.HouseholdList)
      },
      {
        path: 'households/:householdId',
        canActivate: [rememberHouseholdGuard],
        loadComponent: () => import('./household/household-dashboard/household-dashboard').then(m => m.HouseholdDashboard),
      }
    ]
  },
  {path: '**', redirectTo: ''}
];
