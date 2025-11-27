import { createReducer, on } from '@ngrx/store';
import { User } from '../../../core/models/user.model';
import * as AuthActions from './auth.actions';

export interface AuthState {
  user: User | null;
  accessToken: string | null;
  loading: boolean;
  error: string | null;
}

export const initialState: AuthState = {
  user: null,
  accessToken: null,
  loading: false,
  error: null
};

export const authReducer = createReducer(
  initialState,
  on(AuthActions.login, state => ({ ...state, loading: true, error: null })),
  on(AuthActions.loginSuccess, (state, { user, accessToken }) => ({
    ...state,
    user,
    accessToken,
    loading: false,
    error: null
  })),
  on(AuthActions.loginFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),
  on(AuthActions.logout, () => initialState),
  on(AuthActions.loadUser, state => ({ ...state, loading: true })),
  on(AuthActions.loadUserSuccess, (state, { user }) => ({
    ...state,
    user,
    loading: false
  }))
);
