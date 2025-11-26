import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { LayoutComponent } from './shared/components/layout/layout.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, LayoutComponent],
  template: `
    <app-layout>
      <router-outlet></router-outlet>
    </app-layout>
  `,
  styles: [`
    :host {
      display: block;
      height: 100%;
    }
  `]
})
export class AppComponent implements OnInit {
  constructor(private translate: TranslateService) {
    this.translate.setDefaultLang('en');
    this.translate.use(localStorage.getItem('lang') || 'en');
  }

  ngOnInit(): void {
    const darkMode = localStorage.getItem('darkMode') === 'true';
    if (darkMode) {
      document.body.classList.add('dark-theme');
    }
  }
}
