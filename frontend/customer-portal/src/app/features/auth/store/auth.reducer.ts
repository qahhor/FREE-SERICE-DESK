import { createReducer, on } from '@ngrx/store';
import { User } from '../../../core/models/user.model';
import * as AuthActions from './auth.actions';

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

export const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null
};

export const authReducer = createReducer(
  initialState,
  on(AuthActions.login, state => ({
    ...state,
    isLoading: true,
    error: null
  })),
  on(AuthActions.loginSuccess, (state, { user }) => ({
    ...state,
    user,
    isAuthenticated: true,
    isLoading: false,
    error: null
  })),
  on(AuthActions.loginFailure, (state, { error }) => ({
    ...state,
    isLoading: false,
    error
  })),
  on(AuthActions.logout, () => initialState),
  on(AuthActions.updateProfile, state => ({
    ...state,
    isLoading: true
  })),
  on(AuthActions.updateProfileSuccess, (state, { user }) => ({
    ...state,
    user,
    isLoading: false
  })),
  on(AuthActions.updateProfileFailure, (state, { error }) => ({
    ...state,
    isLoading: false,
    error
  }))
);
