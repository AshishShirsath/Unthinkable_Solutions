export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'TENANT' | 'OWNER' | 'ADMIN';
}

export interface UserSession {
  accessToken: string;
  refreshToken: string;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'TENANT' | 'OWNER' | 'ADMIN';
}

export interface Listing {
  id: number;
  title: string;
  description: string;
  city: string;
  locality: string;
  address: string;
  rent: number;
  deposit: number;
  availableFrom: string;
  roomType: string;
  furnishingStatus: string;
  status: string;
  ownerName: string;
  ownerEmail: string;
  imageUrls: string[];
  createdAt: string;
}

export interface TenantProfile {
  id: number;
  userId: number;
  preferredCity: string;
  preferredLocality: string;
  minBudget: number;
  maxBudget: number;
  moveInDate: string;
  description: string;
}

export interface Compatibility {
  listingId: number;
  score: number;
  explanation: string;
  llmUsed: boolean;
}

export interface InterestRequest {
  id: number;
  tenantId: number;
  tenantName: string;
  tenantEmail: string;
  listingId: number;
  listingTitle: string;
  listingCity: string;
  status: string;
  compatibilityScore: number;
  scoreExplanation: string;
  message: string;
  chatRoomId: number;
  createdAt: string;
}

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  role: string;
  enabled: boolean;
}

export interface AdminListing {
  id: number;
  title: string;
  city: string;
  rent: number;
  status: string;
  owner: string;
  deleted: boolean;
}

export interface ChatMessageResponse {
  id: number;
  roomId: number;
  senderId: number;
  senderName: string;
  content: string;
  sentAt: string;
}

export interface ChatRoomResponse {
  id: number;
  tenantId: number;
  tenantName: string;
  ownerId: number;
  ownerName: string;
  listingId: number;
  listingTitle: string;
  createdAt: string;
  lastMessage?: ChatMessageResponse;
}
