import { Routes } from '@angular/router';

export const CHANNELS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./channels.component').then(m => m.ChannelsComponent)
  }
];
