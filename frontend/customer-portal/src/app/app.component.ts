import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { ChatWidgetComponent } from './shared/components/chat-widget/chat-widget.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, LayoutComponent, ChatWidgetComponent],
  template: `
    <app-layout>
      <router-outlet></router-outlet>
    </app-layout>
    <app-chat-widget></app-chat-widget>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
    }
  `]
})
export class AppComponent implements OnInit {
  constructor(private translate: TranslateService) {
    // Set default language
    this.translate.setDefaultLang('en');

    // Use browser language if available
    const browserLang = this.translate.getBrowserLang();
    const supportedLangs = ['en', 'ru', 'uz', 'kk', 'ar'];

    if (browserLang && supportedLangs.includes(browserLang)) {
      this.translate.use(browserLang);
    }
  }

  ngOnInit(): void {
    // Check for saved language preference
    const savedLang = localStorage.getItem('language');
    if (savedLang) {
      this.translate.use(savedLang);
    }
  }
}
