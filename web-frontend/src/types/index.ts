// Types centralisés pour l'application MAGSAV-3.0

// ==================== SAV ====================
export interface ServiceRequest {
  id: number;
  requestNumber: string;
  title: string;
  description?: string;
  status: ServiceRequestStatus;
  priority: ServiceRequestPriority;
  requestDate: string;
  resolvedDate?: string;
  clientId?: number;
  equipmentId?: number;
  equipmentQrCode?: string;
  equipmentInternalReference?: string;
  assignedToId?: number;
  estimatedCost?: number;
  actualCost?: number;
  createdAt: string;
  updatedAt: string;
}

export enum ServiceRequestStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  WAITING_PARTS = 'WAITING_PARTS',
  RESOLVED = 'RESOLVED',
  CANCELLED = 'CANCELLED'
}

export enum ServiceRequestPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
}

export interface Repair {
  id: number;
  repairNumber: string;
  equipmentId?: number;
  equipmentQrCode?: string;
  equipmentInternalReference?: string;
  serviceRequestId?: number;
  description: string;
  diagnosis?: string;
  solution?: string;
  status: RepairStatus;
  priority: RepairPriority;
  startDate?: string;
  completionDate?: string;
  technicianId?: number;
  cost?: number;
  partsUsed?: string;
  createdAt: string;
  updatedAt: string;
}

export enum RepairStatus {
  DIAGNOSTIC = 'DIAGNOSTIC',
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

export enum RepairPriority {
  LOW = 'LOW',
  NORMAL = 'NORMAL',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export interface RMA {
  id: number;
  rmaNumber: string;
  equipmentId?: number;
  equipmentQrCode?: string;
  equipmentInternalReference?: string;
  supplierId?: number;
  reason: RMAReasonType;
  description: string;
  status: RMAStatus;
  requestDate: string;
  authorizedDate?: string;
  returnDate?: string;
  replacementDate?: string;
  refundAmount?: number;
  trackingNumber?: string;
  createdAt: string;
  updatedAt: string;
}

export enum RMAStatus {
  INITIATED = 'INITIATED',
  AUTHORIZED = 'AUTHORIZED',
  SHIPPED = 'SHIPPED',
  RECEIVED = 'RECEIVED',
  INSPECTING = 'INSPECTING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  REPLACED = 'REPLACED',
  REFUNDED = 'REFUNDED',
  CLOSED = 'CLOSED'
}

export enum RMAReasonType {
  MANUFACTURING_DEFECT = 'MANUFACTURING_DEFECT',
  DAMAGE_IN_TRANSIT = 'DAMAGE_IN_TRANSIT',
  WRONG_ITEM = 'WRONG_ITEM',
  NOT_AS_DESCRIBED = 'NOT_AS_DESCRIBED',
  PERFORMANCE_ISSUE = 'PERFORMANCE_ISSUE',
  OTHER = 'OTHER'
}

// ==================== EQUIPMENT ====================
export interface Equipment {
  id: number;
  name: string;
  description?: string;
  category?: string;              // Famille (CSV[0])
  subCategory?: string;           // Catégorie (CSV[1])
  specificCategory?: string;      // Type (CSV[2])
  status: string;
  qrCode?: string;                // UID généré
  brand?: string;                 // Marque (CSV[3])
  model?: string;                 // Modèle (extrait)
  serialNumber?: string;          // N° de Série (CSV[7])
  purchasePrice?: number;         // Prix d'achat (CSV[9])
  purchaseDate?: string;
  createdAt?: string;
  updatedAt?: string;
  location?: string;              // Emplacement (CSV[4])
  zone?: string;
  notes?: string;
  internalReference?: string;     // Code LOCMAT (CSV[5])
  weight?: number;
  dimensions?: string;
  warrantyExpiration?: string;
  supplier?: string;
  insuranceValue?: number;        // Valeur (CSV[10])
  quantityInStock?: number;       // Qté (CSV[8])
  quantityInTransfer?: number;
  quantityOut?: number;
  quantityInRepair?: number;
  quantityMissing?: number;
  quantityInScrap?: number;
  lastMaintenanceDate?: string;
  nextMaintenanceDate?: string;
  photoPath?: string;
  sourceSheet?: string;

  // Aliases pour compatibilité
  internalCode?: string;
  designation?: string;
  photo?: string;
  categoryId?: number;
}

export enum EquipmentStatus {
  AVAILABLE = 'AVAILABLE',
  IN_USE = 'IN_USE',
  MAINTENANCE = 'MAINTENANCE',
  REPAIR = 'REPAIR',
  RETIRED = 'RETIRED',
  LOST = 'LOST'
}

export interface Category {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
  createdAt: string;
  updatedAt: string;
}

// ==================== CLIENTS & CONTRACTS ====================
export interface Client {
  id: number;
  name: string;
  type: ClientType;
  email?: string;
  phone?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  country?: string;
  taxId?: string;
  active: boolean;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export enum ClientType {
  INDIVIDUAL = 'INDIVIDUAL',
  COMPANY = 'COMPANY',
  ASSOCIATION = 'ASSOCIATION',
  PUBLIC = 'PUBLIC'
}

export interface Contract {
  id: number;
  contractNumber: string;
  clientId: number;
  clientName?: string;  // Ajouté pour affichage
  projectId?: number;
  title: string;
  description?: string;
  type?: string;  // Ajouté pour filtrage
  status: ContractStatus;
  startDate: string;
  endDate?: string;
  value?: number;
  paymentTerms?: string;
  createdAt: string;
  updatedAt: string;
}

export enum ContractStatus {
  DRAFT = 'DRAFT',
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

// ==================== PROJECTS ====================
export interface Project {
  id: number;
  projectNumber: string;
  name: string;
  title?: string;  // Alias pour name
  clientName?: string;  // Ajouté pour affichage
  description?: string;
  clientId?: number;
  status: ProjectStatus;
  startDate?: string;
  endDate?: string;
  budget?: number;
  actualCost?: number;
  location?: string;
  managerPersonnelId?: number;
  createdAt: string;
  updatedAt: string;
}

export enum ProjectStatus {
  PLANNING = 'PLANNING',
  IN_PROGRESS = 'IN_PROGRESS',
  ON_HOLD = 'ON_HOLD',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

// ==================== VEHICLES ====================
export interface Vehicle {
  id: number;
  registrationNumber: string;
  registration?: string;  // Alias pour registrationNumber
  make?: string;  // Alias pour brand
  brand: string;
  model: string;
  type: VehicleType;
  year?: number;
  vin?: string;
  acquisitionDate?: string;
  status: VehicleStatus;
  mileage?: number;
  fuelType?: string;
  capacity?: string;
  lastMaintenanceDate?: string;  // Ajouté
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export enum VehicleType {
  VAN = 'VAN',
  TRUCK = 'TRUCK',
  CAR = 'CAR',
  TRAILER = 'TRAILER',
  UTILITY = 'UTILITY',  // Ajouté
  OTHER = 'OTHER'
}

export enum VehicleStatus {
  AVAILABLE = 'AVAILABLE',
  IN_USE = 'IN_USE',
  MAINTENANCE = 'MAINTENANCE',
  OUT_OF_SERVICE = 'OUT_OF_SERVICE'
}

export interface VehicleReservation {
  id: number;
  vehicleId: number;
  vehicleRegistration?: string;  // Ajouté pour affichage
  driver?: string;  // Ajouté pour affichage
  purpose?: string;
  personnelId?: number;
  projectId?: number;
  startDate: string;
  endDate: string;
  status: ReservationStatus;
  createdAt: string;
  updatedAt: string;
}

export enum ReservationStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

// ==================== PERSONNEL ====================
export interface Personnel {
  id: number;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  type: PersonnelType;
  position?: string;
  department?: string;
  hireDate?: string;
  active: boolean;
  skills?: string;
  qualifications?: string[];  // Ajouté comme tableau
  certifications?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export enum PersonnelType {
  PERMANENT = 'PERMANENT',
  EMPLOYEE = 'EMPLOYEE',  // Ajouté
  CONTRACTOR = 'CONTRACTOR',
  FREELANCE = 'FREELANCE',
  INTERN = 'INTERN',
  TEMP = 'TEMP',  // Ajouté
  INTERMITTENT = 'INTERMITTENT'  // Ajouté
}

export interface Qualification {
  id: number;
  name: string;
  description?: string;
  validityPeriod?: number;
  createdAt: string;
  updatedAt: string;
}

// ==================== SUPPLIERS ====================
export interface Supplier {
  id: number;
  name: string;
  code?: string;
  email?: string;
  phone?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  country?: string;
  website?: string;
  contactPerson?: string;
  paymentTerms?: string;
  active: boolean;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MaterialRequest {
  id: number;
  requestNumber: string;
  requestedBy: number;
  projectId?: number;
  description: string;
  status: MaterialRequestStatus;
  priority: string;
  requestDate: string;
  neededByDate?: string;
  approvedBy?: number;
  approvedDate?: string;
  estimatedCost?: number;
  createdAt: string;
  updatedAt: string;
}

export enum MaterialRequestStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  APPROVED = 'APPROVED',
  ORDERED = 'ORDERED',
  RECEIVED = 'RECEIVED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED'
}

export interface GroupedOrder {
  id: number;
  orderNumber: string;
  supplierId?: number;
  status: GroupedOrderStatus;
  orderDate: string;
  expectedDelivery?: string;
  actualDelivery?: string;
  totalAmount?: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export enum GroupedOrderStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  CONFIRMED = 'CONFIRMED',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED'
}

// ==================== PLANNING ====================
export interface PlanningEvent {
  id: string;
  type: 'PERSONNEL' | 'VEHICLE';
  resourceId: number;
  resourceName: string;
  projectId?: number;
  projectName?: string;
  startDate: string;
  endDate: string;
  description?: string;
  status: string;
}

export interface PlanningStatistics {
  totalEvents: number;
  personnelEvents: number;
  vehicleEvents: number;
  activeProjects: number;
}

// ==================== DASHBOARD ====================
export interface DashboardStats {
  totalEquipment: number;
  availableEquipment: number;
  inUseEquipment: number;
  maintenanceEquipment: number;
  totalVehicles: number;
  availableVehicles: number;
  totalPersonnel: number;
  activePersonnel: number;
  openServiceRequests: number;
  pendingRepairs: number;
  activeRMAs: number;
  activeProjects: number;
  activeContracts: number;
  pendingMaterialRequests: number;
}

// ==================== API RESPONSES ====================
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// ==================== COMMON ====================
export interface SelectOption {
  value: string | number;
  label: string;
}

export interface FilterConfig {
  field: string;
  operator: 'eq' | 'ne' | 'gt' | 'lt' | 'gte' | 'lte' | 'like' | 'in';
  value: any;
}
