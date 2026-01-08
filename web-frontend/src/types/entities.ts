/**
 * Types centralisés pour toutes les entités du système MAGSAV
 * Source unique de vérité pour éviter la duplication de types
 */

// ==================== CLIENTS ====================
export interface Client {
  id: number;
  name: string;
  type: 'PARTICULIER' | 'PROFESSIONNEL' | 'ENTREPRISE';
  email?: string;
  phone?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  country?: string;
  active: boolean;
  createdAt: string;
  updatedAt?: string;
  notes?: string;
}

// ==================== CONTRATS ====================
export interface Contract {
  id: number;
  contractNumber: string;
  clientId: number;
  type: 'MAINTENANCE' | 'LOCATION' | 'SERVICE' | 'SUPPORT';
  status: 'DRAFT' | 'ACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'TERMINATED';
  startDate: string;
  endDate?: string;
  amount?: number;
  description?: string;
  active: boolean;
  createdAt: string;
}

// ==================== PERSONNEL ====================
export interface Personnel {
  id: number;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  type: 'PERMANENT' | 'INTERMITTENT' | 'FREELANCE';
  position?: string;
  qualifications?: string[];
  active: boolean;
  hireDate?: string;
  avatar?: string;
}

// ==================== ÉQUIPEMENTS ====================
export interface Equipment {
  id: number;
  name: string;
  description?: string;
  category?: string;
  status: string;
  qrCode?: string;
  brand?: string;
  model?: string;
  serialNumber?: string;
  purchasePrice?: number;
  purchaseDate?: string;
  createdAt?: string;
  updatedAt?: string;
  location?: string;
  notes?: string;
  internalReference?: string;
  weight?: number;
  dimensions?: string;
  warrantyExpiration?: string;
  supplier?: string;
  insuranceValue?: number;
  lastMaintenanceDate?: string;
  nextMaintenanceDate?: string;
  photoPath?: string;
  
  // Aliases pour compatibilité
  internalCode?: string;  // alias de internalReference
  designation?: string;  // alias de name
  photo?: string;  // alias de photoPath
  categoryId?: number;  // à mapper depuis category
}

// ==================== VÉHICULES ====================
export interface Vehicle {
  id: number;
  licensePlate: string;
  brand: string;
  model: string;
  type: 'VAN' | 'TRUCK' | 'CAR' | 'UTILITY';
  year?: number;
  status: 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE' | 'OUT_OF_SERVICE';
  mileage?: number;
  lastMaintenanceDate?: string;
  nextMaintenanceDate?: string;
  photo?: string;
}

export interface VehicleReservation {
  id: number;
  vehicleId: number;
  personnelId?: number;
  startDate: string;
  endDate: string;
  purpose?: string;
  status: 'PENDING' | 'CONFIRMED' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
}

// ==================== SAV ====================
export interface ServiceRequest {
  id: number;
  requestNumber: string;
  title: string;
  description?: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'WAITING' | 'RESOLVED' | 'CLOSED' | 'CANCELLED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  requestDate: string;
  clientId?: number;
  equipmentId?: number;
  assignedTo?: number;
}

export interface Repair {
  id: number;
  repairNumber: string;
  description: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  startDate?: string;
  endDate?: string;
  cost?: number;
  technicianId?: number;
}

export interface RMA {
  id: number;
  rmaNumber: string;
  reason: string;
  status: 'REQUESTED' | 'APPROVED' | 'SHIPPED' | 'RECEIVED' | 'REFUNDED' | 'REJECTED';
  requestDate: string;
  equipmentId?: number;
  clientId?: number;
  notes?: string;
}

// ==================== VENTES & INSTALLATIONS ====================
export interface Project {
  id: number;
  projectNumber: string;
  name: string;
  clientId?: number;
  status: 'DRAFT' | 'QUOTE' | 'APPROVED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  startDate?: string;
  endDate?: string;
  budget?: number;
  description?: string;
}

// ==================== FOURNISSEURS ====================
export interface Supplier {
  id: number;
  name: string;
  type?: string;
  email?: string;
  phone?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  country?: string;
  active: boolean;
  website?: string;
  notes?: string;
}

// ==================== PLANNING ====================
export interface PlanningEvent {
  id: number;
  title: string;
  description?: string;
  startDate: string;
  endDate: string;
  type: 'MAINTENANCE' | 'INSTALLATION' | 'FORMATION' | 'INTERVENTION' | 'MEETING';
  personnelId?: number;
  vehicleId?: number;
  clientId?: number;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
}

// ==================== STATISTIQUES ====================
export interface DashboardStats {
  totalClients?: number;
  activeContracts?: number;
  totalEquipment?: number;
  availableEquipment?: number;
  inUseEquipment?: number;
  maintenanceEquipment?: number;
  totalPersonnel?: number;
  activePersonnel?: number;
  openServiceRequests?: number;
  pendingRepairs?: number;
  activeRMAs?: number;
  activeProjects?: number;
  pendingMaterialRequests?: number;
  totalVehicles?: number;
  availableVehicles?: number;
}

export interface ServiceRequestStats {
  total?: number;
  open?: number;
  openRequests?: number;
  inProgress?: number;
  resolved?: number;
  pendingRepairs?: number;
  activeRMAs?: number;
  resolvedThisMonth?: number;
  byPriority?: {
    low?: number;
    medium?: number;
    high?: number;
    urgent?: number;
  };
}
