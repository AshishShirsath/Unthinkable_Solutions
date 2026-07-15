import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {
  ApiResponse,
  Compatibility,
  InterestRequest,
  Listing,
  TenantProfile,
  AdminUser,
  AdminListing,
  ChatRoomResponse,
  ChatMessageResponse
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  getListings(filters?: { city?: string; minBudget?: number; maxBudget?: number }) {
    let params = new HttpParams();
    if (filters?.city) params = params.set('city', filters.city);
    if (filters?.minBudget != null) params = params.set('minBudget', filters.minBudget);
    if (filters?.maxBudget != null) params = params.set('maxBudget', filters.maxBudget);
    return this.http.get<ApiResponse<Listing[]>>(`${environment.apiUrl}/listings`, { params });
  }

  getListing(id: number) {
    return this.http.get<ApiResponse<Listing>>(`${environment.apiUrl}/listings/${id}`);
  }

  getCompatibility(listingId: number) {
    return this.http.get<ApiResponse<Compatibility>>(
      `${environment.apiUrl}/listings/${listingId}/compatibility`
    );
  }

  getMyListings() {
    return this.http.get<ApiResponse<Listing[]>>(`${environment.apiUrl}/listings/my`);
  }

  createListing(payload: Record<string, unknown>) {
    return this.http.post<ApiResponse<Listing>>(`${environment.apiUrl}/listings`, payload);
  }

  updateListing(id: number, payload: Record<string, unknown>) {
    return this.http.put<ApiResponse<Listing>>(`${environment.apiUrl}/listings/${id}`, payload);
  }

  deleteListing(id: number) {
    return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/listings/${id}`);
  }

  markFilled(id: number) {
    return this.http.put<ApiResponse<Listing>>(`${environment.apiUrl}/listings/${id}/fill`, {});
  }

  getTenantProfile() {
    return this.http.get<ApiResponse<TenantProfile>>(`${environment.apiUrl}/tenant-profile/me`);
  }

  saveTenantProfile(payload: Record<string, unknown>) {
    return this.http.post<ApiResponse<TenantProfile>>(`${environment.apiUrl}/tenant-profile`, payload);
  }

  sendInterest(listingId: number, message?: string) {
    return this.http.post<ApiResponse<InterestRequest>>(`${environment.apiUrl}/interests`, {
      listingId,
      message
    });
  }

  getSentInterests() {
    return this.http.get<ApiResponse<InterestRequest[]>>(`${environment.apiUrl}/interests/sent`);
  }

  getReceivedInterests() {
    return this.http.get<ApiResponse<InterestRequest[]>>(`${environment.apiUrl}/interests/received`);
  }

  acceptInterest(id: number) {
    return this.http.put<ApiResponse<InterestRequest>>(`${environment.apiUrl}/interests/${id}/accept`, {});
  }

  declineInterest(id: number) {
    return this.http.put<ApiResponse<InterestRequest>>(`${environment.apiUrl}/interests/${id}/decline`, {});
  }

  getAdminUsers() {
    return this.http.get<ApiResponse<AdminUser[]>>(`${environment.apiUrl}/admin/users`);
  }

  getAdminListings() {
    return this.http.get<ApiResponse<AdminListing[]>>(`${environment.apiUrl}/admin/listings`);
  }

  disableUser(id: number) {
    return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/admin/users/${id}`);
  }

  deleteAdminListing(id: number) {
    return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/admin/listings/${id}`);
  }

  uploadFiles(files: File[]) {
    const formData = new FormData();
    for (let i = 0; i < files.length; i++) {
      formData.append('files', files[i]);
    }
    return this.http.post<ApiResponse<string[]>>(`${environment.apiUrl}/files/upload`, formData);
  }

  getChatRooms() {
    return this.http.get<ApiResponse<ChatRoomResponse[]>>(`${environment.apiUrl}/chat/rooms`);
  }

  getChatMessages(roomId: number) {
    return this.http.get<ApiResponse<ChatMessageResponse[]>>(`${environment.apiUrl}/chat/rooms/${roomId}/messages`);
  }

  sendChatMessage(roomId: number, content: string) {
    return this.http.post<ApiResponse<ChatMessageResponse>>(`${environment.apiUrl}/chat/rooms/${roomId}/messages`, {
      roomId,
      content
    });
  }
}
