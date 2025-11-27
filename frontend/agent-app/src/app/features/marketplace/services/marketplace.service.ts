import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import {
  MarketplaceModule,
  ModuleInstallation,
  ModuleSearchRequest,
  InstallModuleRequest,
  CategoryInfo,
  ModuleCategory,
  ModuleWidget,
  ModuleMenuItem
} from '../models/marketplace.models';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class MarketplaceService {
  private readonly apiUrl = `${environment.apiUrl}/marketplace`;
  private readonly modulesUrl = `${environment.apiUrl}/modules`;

  private installedModulesSubject = new BehaviorSubject<ModuleInstallation[]>([]);
  public installedModules$ = this.installedModulesSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadInstalledModules();
  }

  // Browse marketplace

  searchModules(request: ModuleSearchRequest): Observable<Page<MarketplaceModule>> {
    return this.http.post<Page<MarketplaceModule>>(`${this.apiUrl}/search`, request);
  }

  getFeaturedModules(): Observable<MarketplaceModule[]> {
    return this.http.get<MarketplaceModule[]>(`${this.apiUrl}/featured`);
  }

  getNewestModules(): Observable<MarketplaceModule[]> {
    return this.http.get<MarketplaceModule[]>(`${this.apiUrl}/newest`);
  }

  getPopularModules(): Observable<MarketplaceModule[]> {
    return this.http.get<MarketplaceModule[]>(`${this.apiUrl}/popular`);
  }

  getOfficialModules(): Observable<MarketplaceModule[]> {
    return this.http.get<MarketplaceModule[]>(`${this.apiUrl}/official`);
  }

  getModuleDetails(moduleId: string): Observable<MarketplaceModule> {
    return this.http.get<MarketplaceModule>(`${this.apiUrl}/modules/${moduleId}`);
  }

  getCategories(): Observable<CategoryInfo[]> {
    return this.http.get<CategoryInfo[]>(`${this.apiUrl}/categories`);
  }

  getModulesByCategory(category: ModuleCategory, page = 0, size = 20): Observable<Page<MarketplaceModule>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<MarketplaceModule>>(`${this.apiUrl}/categories/${category}`, { params });
  }

  // Module installation

  installModule(request: InstallModuleRequest): Observable<ModuleInstallation> {
    return this.http.post<ModuleInstallation>(`${this.modulesUrl}/install`, request)
      .pipe(tap(() => this.loadInstalledModules()));
  }

  uninstallModule(moduleId: string): Observable<ModuleInstallation> {
    return this.http.delete<ModuleInstallation>(`${this.modulesUrl}/${moduleId}`)
      .pipe(tap(() => this.loadInstalledModules()));
  }

  enableModule(moduleId: string): Observable<ModuleInstallation> {
    return this.http.post<ModuleInstallation>(`${this.modulesUrl}/${moduleId}/enable`, {})
      .pipe(tap(() => this.loadInstalledModules()));
  }

  disableModule(moduleId: string): Observable<ModuleInstallation> {
    return this.http.post<ModuleInstallation>(`${this.modulesUrl}/${moduleId}/disable`, {})
      .pipe(tap(() => this.loadInstalledModules()));
  }

  updateModuleConfiguration(moduleId: string, configuration: Record<string, any>): Observable<ModuleInstallation> {
    return this.http.put<ModuleInstallation>(`${this.modulesUrl}/${moduleId}/configuration`, configuration)
      .pipe(tap(() => this.loadInstalledModules()));
  }

  updateModule(moduleId: string, version?: string): Observable<ModuleInstallation> {
    let params = new HttpParams();
    if (version) {
      params = params.set('version', version);
    }
    return this.http.post<ModuleInstallation>(`${this.modulesUrl}/${moduleId}/update`, {}, { params })
      .pipe(tap(() => this.loadInstalledModules()));
  }

  getInstalledModules(): Observable<ModuleInstallation[]> {
    return this.http.get<ModuleInstallation[]>(`${this.modulesUrl}/installed`);
  }

  getInstallationDetails(moduleId: string): Observable<ModuleInstallation> {
    return this.http.get<ModuleInstallation>(`${this.modulesUrl}/${moduleId}/installation`);
  }

  // Module UI elements

  getWidgets(): Observable<ModuleWidget[]> {
    return this.http.get<ModuleWidget[]>(`${this.modulesUrl}/widgets`);
  }

  getMenuItems(): Observable<ModuleMenuItem[]> {
    return this.http.get<ModuleMenuItem[]>(`${this.modulesUrl}/menu-items`);
  }

  // Utilities

  isModuleInstalled(moduleId: string): boolean {
    const modules = this.installedModulesSubject.getValue();
    return modules.some(m => m.moduleId === moduleId && m.enabled);
  }

  getInstalledModule(moduleId: string): ModuleInstallation | undefined {
    return this.installedModulesSubject.getValue().find(m => m.moduleId === moduleId);
  }

  private loadInstalledModules(): void {
    this.getInstalledModules().subscribe({
      next: (modules) => this.installedModulesSubject.next(modules),
      error: (err) => console.error('Failed to load installed modules', err)
    });
  }

  refreshInstalledModules(): void {
    this.loadInstalledModules();
  }
}
