import { Routes } from '@angular/router';
import { HomePageComponent } from './pages/home-page/home-page.component';
import { LoginPageComponent } from './pages/login-page/login-page.component';
import { RegisterPageComponent } from './pages/register-page/register-page.component';
import { ListingsPageComponent } from './pages/listings-page/listings-page.component';
import { ListingDetailPageComponent } from './pages/listing-detail-page/listing-detail-page.component';
import { TenantDashboardComponent } from './pages/tenant-dashboard/tenant-dashboard.component';
import { OwnerDashboardComponent } from './pages/owner-dashboard/owner-dashboard.component';
import { OwnerListingFormComponent } from './pages/owner-listing-form/owner-listing-form.component';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { ChatPageComponent } from './pages/chat-page/chat-page.component';
import { authGuard, roleGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', component: HomePageComponent },
  { path: 'login', component: LoginPageComponent },
  { path: 'register', component: RegisterPageComponent },
  { path: 'listings', component: ListingsPageComponent },
  { path: 'listings/:id', component: ListingDetailPageComponent },
  { path: 'tenant', component: TenantDashboardComponent, canActivate: [authGuard, roleGuard('TENANT')] },
  { path: 'tenant/profile', component: TenantDashboardComponent, canActivate: [authGuard, roleGuard('TENANT')] },
  { path: 'owner', component: OwnerDashboardComponent, canActivate: [authGuard, roleGuard('OWNER')] },
  { path: 'owner/create', component: OwnerListingFormComponent, canActivate: [authGuard, roleGuard('OWNER')] },
  { path: 'admin', component: AdminDashboardComponent, canActivate: [authGuard, roleGuard('ADMIN')] },
  { path: 'chat', component: ChatPageComponent, canActivate: [authGuard] },
  { path: 'chat/:roomId', component: ChatPageComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
