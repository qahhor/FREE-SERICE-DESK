import { createAction, props } from '@ngrx/store';
import { User, LoginRequest, RegisterRequest, UpdateProfileRequest } from '../../../core/models/user.model';

export const login = createAction(
  '[Auth] Login',
  props<{ request: LoginRequest }>()
);

export const loginSuccess = createAction(
  '[Auth] Login Success',
  props<{ user: User }>()
);

export const loginFailure = createAction(
  '[Auth] Login Failure',
  props<{ error: string }>()
);

export const register = createAction(
  '[Auth] Register',
  props<{ request: RegisterRequest }>()
);

export const registerSuccess = createAction(
  '[Auth] Register Success',
  props<{ user: User }>()
);

export const registerFailure = createAction(
  '[Auth] Register Failure',
  props<{ error: string }>()
);

export const logout = createAction('[Auth] Logout');

export const loadUser = createAction('[Auth] Load User');

export const loadUserSuccess = createAction(
  '[Auth] Load User Success',
  props<{ user: User }>()
);

export const updateProfile = createAction(
  '[Auth] Update Profile',
  props<{ request: UpdateProfileRequest }>()
);

export const updateProfileSuccess = createAction(
  '[Auth] Update Profile Success',
  props<{ user: User }>()
);

export const updateProfileFailure = createAction(
  '[Auth] Update Profile Failure',
  props<{ error: string }>()
);
