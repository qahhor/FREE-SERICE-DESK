import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { TranslateModule } from '@ngx-translate/core';
import { HttpClient } from '@angular/common/http';
import { Subject, interval, takeUntil, switchMap, startWith } from 'rxjs';
import { environment } from '../../../../environments/environment';

interface CurrencyRate {
  id: number;
  code: string;
  currency: string;
  nameRu: string;
  nameUz: string;
  nameEn: string;
  nominal: string;
  rate: string;
  diff: string;
  date: string;
}

interface CurrencyRatesResponse {
  rates: CurrencyRate[];
  date: string;
  updatedAt: string;
  source: string;
}

interface ConversionResult {
  fromCurrency: string;
  toCurrency: string;
  amount: number;
  result: number;
  rate: number;
  inverseRate: number;
  date: string;
  calculatedAt: string;
}

@Component({
  selector: 'app-currency-exchange',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTableModule,
    MatSortModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatTooltipModule,
    MatChipsModule,
    TranslateModule
  ],
  template: `
    <div class="currency-exchange">
      <div class="page-header">
        <div>
          <h1>{{ 'currency.title' | translate }}</h1>
          <p class="subtitle">
            {{ 'currency.source' | translate }}: CBU.uz
            <span *ngIf="lastUpdate">• {{ 'currency.updated' | translate }}: {{ lastUpdate | date:'short' }}</span>
          </p>
        </div>
        <button mat-icon-button (click)="refreshRates()" [disabled]="loading">
          <mat-icon>refresh</mat-icon>
        </button>
      </div>

      <mat-tab-group>
        <!-- Rates Tab -->
        <mat-tab [label]="'currency.rates' | translate">
          <div class="tab-content">
            <!-- Popular currencies cards -->
            <div class="popular-rates" *ngIf="popularRates.length">
              <mat-card *ngFor="let rate of popularRates" class="rate-card">
                <div class="rate-header">
                  <span class="currency-code">{{ rate.currency }}</span>
                  <span class="currency-name">{{ rate.nameEn }}</span>
                </div>
                <div class="rate-value">
                  {{ rate.rate }} <span class="uzs">UZS</span>
                </div>
                <div class="rate-nominal" *ngIf="rate.nominal !== '1'">
                  {{ 'currency.perNominal' | translate:{ nominal: rate.nominal } }}
                </div>
                <div class="rate-diff" [class.up]="isUp(rate.diff)" [class.down]="isDown(rate.diff)">
                  <mat-icon>{{ getDiffIcon(rate.diff) }}</mat-icon>
                  {{ formatDiff(rate.diff) }}
                </div>
              </mat-card>
            </div>

            <!-- All rates table -->
            <mat-card class="rates-table-card">
              <div class="table-header">
                <h3>{{ 'currency.allRates' | translate }}</h3>
                <mat-form-field appearance="outline" class="search-field">
                  <mat-icon matPrefix>search</mat-icon>
                  <input matInput [(ngModel)]="searchQuery" [placeholder]="'currency.searchCurrency' | translate">
                </mat-form-field>
              </div>

              <table mat-table [dataSource]="filteredRates" matSort (matSortChange)="sortData($event)">
                <ng-container matColumnDef="currency">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'currency.code' | translate }}</th>
                  <td mat-cell *matCellDef="let rate">
                    <strong>{{ rate.currency }}</strong>
                  </td>
                </ng-container>

                <ng-container matColumnDef="name">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'currency.name' | translate }}</th>
                  <td mat-cell *matCellDef="let rate">{{ rate.nameEn }}</td>
                </ng-container>

                <ng-container matColumnDef="nominal">
                  <th mat-header-cell *matHeaderCellDef>{{ 'currency.nominal' | translate }}</th>
                  <td mat-cell *matCellDef="let rate">{{ rate.nominal }}</td>
                </ng-container>

                <ng-container matColumnDef="rate">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'currency.rate' | translate }} (UZS)</th>
                  <td mat-cell *matCellDef="let rate">
                    <strong>{{ rate.rate }}</strong>
                  </td>
                </ng-container>

                <ng-container matColumnDef="diff">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'currency.change' | translate }}</th>
                  <td mat-cell *matCellDef="let rate">
                    <span class="diff" [class.up]="isUp(rate.diff)" [class.down]="isDown(rate.diff)">
                      <mat-icon>{{ getDiffIcon(rate.diff) }}</mat-icon>
                      {{ formatDiff(rate.diff) }}
                    </span>
                  </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
              </table>
            </mat-card>
          </div>
        </mat-tab>

        <!-- Converter Tab -->
        <mat-tab [label]="'currency.converter' | translate">
          <div class="tab-content">
            <mat-card class="converter-card">
              <h3>{{ 'currency.convertTitle' | translate }}</h3>

              <div class="converter-form">
                <div class="converter-row">
                  <mat-form-field appearance="outline" class="amount-field">
                    <mat-label>{{ 'currency.amount' | translate }}</mat-label>
                    <input matInput type="number" [(ngModel)]="convertAmount" (ngModelChange)="convert()">
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="currency-field">
                    <mat-label>{{ 'currency.from' | translate }}</mat-label>
                    <mat-select [(ngModel)]="fromCurrency" (selectionChange)="convert()">
                      <mat-option value="UZS">UZS - Uzbekistan Som</mat-option>
                      <mat-option *ngFor="let rate of allRates" [value]="rate.currency">
                        {{ rate.currency }} - {{ rate.nameEn }}
                      </mat-option>
                    </mat-select>
                  </mat-form-field>
                </div>

                <button mat-icon-button class="swap-btn" (click)="swapCurrencies()">
                  <mat-icon>swap_vert</mat-icon>
                </button>

                <div class="converter-row">
                  <mat-form-field appearance="outline" class="amount-field result">
                    <mat-label>{{ 'currency.result' | translate }}</mat-label>
                    <input matInput [value]="conversionResult?.result | number:'1.2-4'" readonly>
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="currency-field">
                    <mat-label>{{ 'currency.to' | translate }}</mat-label>
                    <mat-select [(ngModel)]="toCurrency" (selectionChange)="convert()">
                      <mat-option value="UZS">UZS - Uzbekistan Som</mat-option>
                      <mat-option *ngFor="let rate of allRates" [value]="rate.currency">
                        {{ rate.currency }} - {{ rate.nameEn }}
                      </mat-option>
                    </mat-select>
                  </mat-form-field>
                </div>
              </div>

              <div class="rate-info" *ngIf="conversionResult">
                <p>
                  1 {{ fromCurrency }} = {{ conversionResult.rate | number:'1.4-6' }} {{ toCurrency }}
                </p>
                <p>
                  1 {{ toCurrency }} = {{ conversionResult.inverseRate | number:'1.4-6' }} {{ fromCurrency }}
                </p>
                <p class="date">
                  {{ 'currency.rateDate' | translate }}: {{ conversionResult.date }}
                </p>
              </div>
            </mat-card>
          </div>
        </mat-tab>
      </mat-tab-group>

      <!-- Loading overlay -->
      <div class="loading-overlay" *ngIf="loading">
        <mat-spinner diameter="48"></mat-spinner>
      </div>
    </div>
  `,
  styles: [`
    .currency-exchange {
      max-width: 1200px;
      margin: 0 auto;
      padding: 24px;
      position: relative;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24px;

      h1 {
        margin: 0 0 4px;
        font-size: 24px;
        font-weight: 500;
      }

      .subtitle {
        margin: 0;
        font-size: 14px;
        color: rgba(0, 0, 0, 0.6);
      }
    }

    .tab-content {
      padding: 24px 0;
    }

    .popular-rates {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }

    .rate-card {
      padding: 16px;

      .rate-header {
        margin-bottom: 8px;

        .currency-code {
          font-size: 18px;
          font-weight: 600;
          margin-right: 8px;
        }

        .currency-name {
          font-size: 13px;
          color: rgba(0, 0, 0, 0.6);
        }
      }

      .rate-value {
        font-size: 20px;
        font-weight: 500;
        margin-bottom: 4px;

        .uzs {
          font-size: 14px;
          color: rgba(0, 0, 0, 0.5);
        }
      }

      .rate-nominal {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.5);
        margin-bottom: 8px;
      }

      .rate-diff {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 14px;

        mat-icon {
          font-size: 18px;
          width: 18px;
          height: 18px;
        }

        &.up {
          color: #4caf50;
        }

        &.down {
          color: #f44336;
        }
      }
    }

    .rates-table-card {
      padding: 20px;

      .table-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        h3 {
          margin: 0;
          font-size: 18px;
          font-weight: 500;
        }

        .search-field {
          width: 250px;
        }
      }

      table {
        width: 100%;

        .diff {
          display: flex;
          align-items: center;
          gap: 4px;

          mat-icon {
            font-size: 16px;
            width: 16px;
            height: 16px;
          }

          &.up {
            color: #4caf50;
          }

          &.down {
            color: #f44336;
          }
        }
      }
    }

    .converter-card {
      max-width: 500px;
      margin: 0 auto;
      padding: 24px;

      h3 {
        margin: 0 0 24px;
        text-align: center;
        font-size: 20px;
        font-weight: 500;
      }
    }

    .converter-form {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;

      .converter-row {
        display: flex;
        gap: 16px;
        width: 100%;

        .amount-field {
          flex: 1;
        }

        .currency-field {
          width: 200px;
        }
      }

      .swap-btn {
        margin: 8px 0;
      }
    }

    .rate-info {
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid rgba(0, 0, 0, 0.12);
      text-align: center;

      p {
        margin: 4px 0;
        font-size: 14px;
        color: rgba(0, 0, 0, 0.7);

        &.date {
          margin-top: 12px;
          font-size: 12px;
          color: rgba(0, 0, 0, 0.5);
        }
      }
    }

    .loading-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 10;
    }
  `]
})
export class CurrencyExchangeComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private moduleApiUrl = `${environment.apiUrl}/modules/currency-exchange/api`;

  loading = false;
  allRates: CurrencyRate[] = [];
  popularRates: CurrencyRate[] = [];
  filteredRates: CurrencyRate[] = [];
  lastUpdate: Date | null = null;
  searchQuery = '';

  // Converter
  convertAmount = 1;
  fromCurrency = 'USD';
  toCurrency = 'UZS';
  conversionResult: ConversionResult | null = null;

  displayedColumns = ['currency', 'name', 'nominal', 'rate', 'diff'];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadRates();

    // Auto-refresh every 30 minutes
    interval(30 * 60 * 1000).pipe(
      takeUntil(this.destroy$),
      startWith(0)
    ).subscribe(() => {
      this.loadRates();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadRates(): void {
    this.loading = true;

    this.http.get<CurrencyRatesResponse>(`${this.moduleApiUrl}/rates`).subscribe({
      next: (response) => {
        this.allRates = response.rates;
        this.filteredRates = [...this.allRates];
        this.lastUpdate = new Date(response.updatedAt);
        this.loading = false;
        this.loadPopularRates();
      },
      error: (err) => {
        console.error('Failed to load rates', err);
        this.loading = false;
      }
    });
  }

  loadPopularRates(): void {
    this.http.get<CurrencyRate[]>(`${this.moduleApiUrl}/rates/popular`).subscribe({
      next: (rates) => {
        this.popularRates = rates;
      }
    });
  }

  refreshRates(): void {
    this.loadRates();
  }

  get filteredRatesData(): CurrencyRate[] {
    if (!this.searchQuery) {
      return this.allRates;
    }
    const query = this.searchQuery.toLowerCase();
    return this.allRates.filter(rate =>
      rate.currency.toLowerCase().includes(query) ||
      rate.nameEn.toLowerCase().includes(query) ||
      rate.nameRu?.toLowerCase().includes(query)
    );
  }

  sortData(sort: Sort): void {
    if (!sort.active || sort.direction === '') {
      this.filteredRates = [...this.allRates];
      return;
    }

    this.filteredRates = [...this.allRates].sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case 'currency':
          return this.compare(a.currency, b.currency, isAsc);
        case 'name':
          return this.compare(a.nameEn, b.nameEn, isAsc);
        case 'rate':
          return this.compare(parseFloat(a.rate), parseFloat(b.rate), isAsc);
        case 'diff':
          return this.compare(parseFloat(a.diff), parseFloat(b.diff), isAsc);
        default:
          return 0;
      }
    });
  }

  compare(a: number | string, b: number | string, isAsc: boolean): number {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  isUp(diff: string): boolean {
    return parseFloat(diff) > 0;
  }

  isDown(diff: string): boolean {
    return parseFloat(diff) < 0;
  }

  getDiffIcon(diff: string): string {
    const value = parseFloat(diff);
    if (value > 0) return 'trending_up';
    if (value < 0) return 'trending_down';
    return 'trending_flat';
  }

  formatDiff(diff: string): string {
    const value = parseFloat(diff);
    if (value > 0) return `+${diff}`;
    return diff;
  }

  convert(): void {
    if (!this.convertAmount || !this.fromCurrency || !this.toCurrency) {
      return;
    }

    this.http.post<ConversionResult>(`${this.moduleApiUrl}/convert`, {
      fromCurrency: this.fromCurrency,
      toCurrency: this.toCurrency,
      amount: this.convertAmount
    }).subscribe({
      next: (result) => {
        this.conversionResult = result;
      },
      error: (err) => {
        console.error('Conversion failed', err);
      }
    });
  }

  swapCurrencies(): void {
    const temp = this.fromCurrency;
    this.fromCurrency = this.toCurrency;
    this.toCurrency = temp;
    this.convert();
  }
}
