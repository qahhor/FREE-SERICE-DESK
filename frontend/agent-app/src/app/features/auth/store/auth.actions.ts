import { createAction, props } from '@ngrx/store';
import { User, LoginRequest } from '../../../core/models/user.model';

export const login = createAction(
  '[Auth] Login',
  props<{ request: LoginRequest }>()
);

export const loginSuccess = createAction(
  '[Auth] Login Success',
  props<{ user: User; accessToken: string }>()
);

export const loginFailure = createAction(
  '[Auth] Login Failure',
  props<{ error: string }>()
);

export const logout = createAction('[Auth] Logout');

export const loadUser = createAction('[Auth] Load User');

export const loadUserSuccess = createAction(
  '[Auth] Load User Success',
  props<{ user: User }>()
);
