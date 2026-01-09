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
  name: string;
  licensePlate: string;
  brand: string;
  model: string;
  color?: string;
  owner?: string;
  type: 'VAN' | 'VL' | 'VL_17M3' | 'VL_20M3' | 'TRUCK' | 'PORTEUR' | 'TRACTEUR' | 'SEMI_REMORQUE' | 'SCENE_MOBILE' | 'TRAILER' | 'CAR' | 'MOTORCYCLE' | 'OTHER';
  year?: number;
  status: 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE' | 'OUT_OF_ORDER' | 'RENTED_OUT' | 'RESERVED';
  fuelType?: 'GASOLINE' | 'DIESEL' | 'ELECTRIC' | 'HYBRID' | 'GPL' | 'OTHER';
  mileage?: number;
  lastMaintenanceDate?: string;
  nextMaintenanceDate?: string;
  notes?: string;
  photo?: string;
  photoPath?: string;
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
  status: 'PENDING' | 'VALIDATED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  requestDate: string;
  clientId?: number;
  equipmentId?: number;
  assignedTo?: number;
  equipmentQrCode?: string;
  equipmentInternalReference?: string;
  validationAction?: 'DIAGNOSTIC' | 'INTERNAL_REPAIR' | 'RMA' | 'SCRAP';
  relatedRepairId?: number;
  relatedRmaId?: number;
}

export interface Repair {
  id: number;
  repairNumber: string;
  description: string;
  status: 'IN_PROGRESS' | 'WAITING_PARTS' | 'COMPLETED' | 'CANCELLED' | 'DIAGNOSTIC' | 'PENDING';
  priority: 'LOW' | 'MEDIUM' | 'NORMAL' | 'HIGH' | 'URGENT';
  startDate?: string;
  endDate?: string;
  cost?: number;
  technicianId?: number;
  equipmentQrCode?: string;
  equipmentInternalReference?: string;
  equipmentName?: string;
  equipmentSerialNumber?: string;
  problemDescription?: string;
  serviceRequestId?: number;
}

export interface RMA {
  id: number;
  rmaNumber: string;
  reason: string;
  status: 'REQUEST_PENDING' | 'REQUEST_VALIDATED' | 'SHIPPED' | 'RETURNED' | 'REJECTED' | 'REQUESTED' | 'APPROVED' | 'RECEIVED' | 'REFUNDED';
  requestDate: string;
  equipmentId?: number;
  equipmentQrCode?: string;
  equipmentInternalReference?: string;
  clientId?: number;
  notes?: string;
  description?: string;
  equipmentName?: string;
  equipmentSerialNumber?: string;
  priority?: 'LOW' | 'MEDIUM' | 'NORMAL' | 'HIGH' | 'URGENT';
  serviceRequestId?: number;
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
