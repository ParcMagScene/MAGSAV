import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

class ApiService {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 30000,
    });

    // Intercepteur pour les requêtes
    this.client.interceptors.request.use(
      (config) => {
        // 🔍 LOG: Tracer toutes les requêtes API
        console.log('🌐 [API REQUEST]', {
          method: config.method?.toUpperCase(),
          url: config.url,
          baseURL: config.baseURL,
          fullURL: `${config.baseURL}${config.url}`,
          headers: config.headers,
          params: config.params,
          data: config.data
        });

        // Ajouter le token JWT si disponible
        const token = localStorage.getItem('authToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
          console.log('🔐 [AUTH] Token ajouté à la requête');
        } else {
          console.warn('⚠️ [AUTH] Aucun token trouvé dans localStorage');
        }
        return config;
      },
      (error) => {
        console.error('❌ [API REQUEST ERROR]', error);
        return Promise.reject(error);
      }
    );

    // Intercepteur pour les réponses
    this.client.interceptors.response.use(
      (response) => {
        // 🔍 LOG: Tracer toutes les réponses API réussies
        console.log('✅ [API RESPONSE]', {
          status: response.status,
          url: response.config.url,
          dataType: Array.isArray(response.data) ? 'Array' : typeof response.data,
          dataLength: Array.isArray(response.data) ? response.data.length : 'N/A',
          data: response.data
        });
        return response;
      },
      (error) => {
        // 🔍 LOG: Tracer toutes les erreurs API
        console.error('❌ [API ERROR]', {
          status: error.response?.status,
          statusText: error.response?.statusText,
          url: error.config?.url,
          method: error.config?.method,
          message: error.message,
          responseData: error.response?.data,
          fullError: error
        });

        if (error.response?.status === 401) {
          console.warn('🔒 [AUTH] Non authentifié - Redirection vers login');
          // Rediriger vers la page de login si non authentifié
          localStorage.removeItem('authToken');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Méthodes génériques
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    console.log(`📥 [API.GET] Appel: ${url}`);
    const response = await this.client.get<T>(url, config);
    console.log(`📦 [API.GET] Réponse reçue:`, response.data);
    return response.data;
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    console.log(`📤 [API.POST] Appel: ${url}`, { data });
    const response = await this.client.post<T>(url, data, config);
    console.log(`📦 [API.POST] Réponse reçue:`, response.data);
    return response.data;
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    console.log(`🔄 [API.PUT] Appel: ${url}`, { data });
    const response = await this.client.put<T>(url, data, config);
    console.log(`📦 [API.PUT] Réponse reçue:`, response.data);
    return response.data;
  }

  async patch<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.patch<T>(url, data, config);
    return response.data;
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.delete<T>(url, config);
    return response.data;
  }

  // ==================== SAV ====================
  // Service Requests
  getServiceRequests() {
    return this.get<any[]>('/service-requests');
  }

  getServiceRequestById(id: number) {
    return this.get<any>(`/service-requests/${id}`);
  }

  createServiceRequest(data: any) {
    return this.post<any>('/service-requests', data);
  }

  updateServiceRequest(id: number, data: any) {
    return this.put<any>(`/service-requests/${id}`, data);
  }

  deleteServiceRequest(id: number) {
    return this.delete<void>(`/service-requests/${id}`);
  }

  getServiceRequestsByStatus(status: string) {
    return this.get<any[]>(`/service-requests/status/${status}`);
  }

  getServiceRequestsByClient(clientId: number) {
    return this.get<any[]>(`/service-requests/client/${clientId}`);
  }

  getServiceRequestStats() {
    return this.get<any>('/service-requests/stats');
  }

  // Repairs
  getRepairs() {
    return this.get<any[]>('/repairs');
  }

  getRepairById(id: number) {
    return this.get<any>(`/repairs/${id}`);
  }

  createRepair(data: any) {
    return this.post<any>('/repairs', data);
  }

  updateRepair(id: number, data: any) {
    return this.put<any>(`/repairs/${id}`, data);
  }

  deleteRepair(id: number) {
    return this.delete<void>(`/repairs/${id}`);
  }

  getRepairsByStatus(status: string) {
    return this.get<any[]>(`/repairs/status/${status}`);
  }

  getRepairStats() {
    return this.get<any>('/repairs/stats');
  }

  // RMA
  getRMAs() {
    return this.get<any[]>('/rma');
  }

  getRMAById(id: number) {
    return this.get<any>(`/rma/${id}`);
  }

  createRMA(data: any) {
    return this.post<any>('/rma', data);
  }

  updateRMA(id: number, data: any) {
    return this.put<any>(`/rma/${id}`, data);
  }

  deleteRMA(id: number) {
    return this.delete<void>(`/rma/${id}`);
  }

  authorizeRMA(id: number) {
    return this.post<any>(`/rma/${id}/authorize`);
  }

  getRMAsByStatus(status: string) {
    return this.get<any[]>(`/rma/status/${status}`);
  }

  getRMAStats() {
    return this.get<any>('/rma/stats');
  }

  // ==================== EQUIPMENT ====================
  getEquipment() {
    return this.get<any[]>('/equipment');
  }

  getEquipmentById(id: number) {
    return this.get<any>(`/equipment/${id}`);
  }

  createEquipment(data: any) {
    return this.post<any>('/equipment', data);
  }

  updateEquipment(id: number, data: any) {
    return this.put<any>(`/equipment/${id}`, data);
  }

  deleteEquipment(id: number) {
    return this.delete<void>(`/equipment/${id}`);
  }

  getEquipmentByStatus(status: string) {
    return this.get<any[]>(`/equipment/status/${status}`);
  }

  getEquipmentStats() {
    return this.get<any>('/equipment/stats');
  }

  // Categories
  getCategories() {
    return this.get<any[]>('/categories');
  }

  getCategoryById(id: number) {
    return this.get<any>(`/categories/${id}`);
  }

  createCategory(data: any) {
    return this.post<any>('/categories', data);
  }

  updateCategory(id: number, data: any) {
    return this.put<any>(`/categories/${id}`, data);
  }

  deleteCategory(id: number) {
    return this.delete<void>(`/categories/${id}`);
  }

  // ==================== CLIENTS ====================
  getClients() {
    return this.get<any[]>('/clients');
  }

  getClientById(id: number) {
    return this.get<any>(`/clients/${id}`);
  }

  createClient(data: any) {
    return this.post<any>('/clients', data);
  }

  updateClient(id: number, data: any) {
    return this.put<any>(`/clients/${id}`, data);
  }

  deleteClient(id: number) {
    return this.delete<void>(`/clients/${id}`);
  }

  getActiveClients() {
    return this.get<any[]>('/clients/active');
  }

  // ==================== CONTRACTS ====================
  getContracts() {
    return this.get<any[]>('/contracts');
  }

  getContractById(id: number) {
    return this.get<any>(`/contracts/${id}`);
  }

  createContract(data: any) {
    return this.post<any>('/contracts', data);
  }

  updateContract(id: number, data: any) {
    return this.put<any>(`/contracts/${id}`, data);
  }

  deleteContract(id: number) {
    return this.delete<void>(`/contracts/${id}`);
  }

  getContractsByStatus(status: string) {
    return this.get<any[]>(`/contracts/status/${status}`);
  }

  getContractsByClient(clientId: number) {
    return this.get<any[]>(`/contracts/client/${clientId}`);
  }

  // ==================== PROJECTS ====================
  getProjects() {
    return this.get<any[]>('/projects');
  }

  getProjectById(id: number) {
    return this.get<any>(`/projects/${id}`);
  }

  createProject(data: any) {
    return this.post<any>('/projects', data);
  }

  updateProject(id: number, data: any) {
    return this.put<any>(`/projects/${id}`, data);
  }

  deleteProject(id: number) {
    return this.delete<void>(`/projects/${id}`);
  }

  getProjectsByStatus(status: string) {
    return this.get<any[]>(`/projects/status/${status}`);
  }

  getProjectsByClient(clientId: number) {
    return this.get<any[]>(`/projects/client/${clientId}`);
  }

  // ==================== VEHICLES ====================
  getVehicles() {
    return this.get<any[]>('/vehicles');
  }

  getVehicleById(id: number) {
    return this.get<any>(`/vehicles/${id}`);
  }

  createVehicle(data: any) {
    return this.post<any>('/vehicles', data);
  }

  updateVehicle(id: number, data: any) {
    return this.put<any>(`/vehicles/${id}`, data);
  }

  deleteVehicle(id: number) {
    return this.delete<void>(`/vehicles/${id}`);
  }

  getVehiclesByStatus(status: string) {
    return this.get<any[]>(`/vehicles/status/${status}`);
  }

  getVehicleStats() {
    return this.get<any>('/vehicles/statistics');
  }

  // Vehicle Reservations
  getVehicleReservations() {
    return this.get<any[]>('/vehicle-reservations');
  }

  getVehicleReservationById(id: number) {
    return this.get<any>(`/vehicle-reservations/${id}`);
  }

  createVehicleReservation(data: any) {
    return this.post<any>('/vehicle-reservations', data);
  }

  updateVehicleReservation(id: number, data: any) {
    return this.put<any>(`/vehicle-reservations/${id}`, data);
  }

  deleteVehicleReservation(id: number) {
    return this.delete<void>(`/vehicle-reservations/${id}`);
  }

  // ==================== PERSONNEL ====================
  getPersonnel() {
    return this.get<any[]>('/personnel');
  }

  getPersonnelById(id: number) {
    return this.get<any>(`/personnel/${id}`);
  }

  createPersonnel(data: any) {
    return this.post<any>('/personnel', data);
  }

  updatePersonnel(id: number, data: any) {
    return this.put<any>(`/personnel/${id}`, data);
  }

  deletePersonnel(id: number) {
    return this.delete<void>(`/personnel/${id}`);
  }

  getActivePersonnel() {
    return this.get<any[]>('/personnel/active');
  }

  getPersonnelStats() {
    return this.get<any>('/personnel/stats');
  }

  // Qualifications
  getQualifications() {
    return this.get<any[]>('/qualifications');
  }

  getQualificationById(id: number) {
    return this.get<any>(`/qualifications/${id}`);
  }

  createQualification(data: any) {
    return this.post<any>('/qualifications', data);
  }

  updateQualification(id: number, data: any) {
    return this.put<any>(`/qualifications/${id}`, data);
  }

  deleteQualification(id: number) {
    return this.delete<void>(`/qualifications/${id}`);
  }

  // ==================== PLANNING ====================
  getPlanningStatistics(startDate?: string, endDate?: string) {
    // Dates par défaut : 30 jours avant/après aujourd'hui
    const now = new Date();
    const defaultStart = startDate || new Date(now.getFullYear(), now.getMonth(), 1).toISOString();
    const defaultEnd = endDate || new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString();

    return this.get<any>('/planning/statistics', {
      params: { startDate: defaultStart, endDate: defaultEnd }
    });
  }

  checkAvailability(resourceType: string, resourceId: number, startDate: string, endDate: string) {
    return this.get<any>(`/planning/availability`, {
      params: { resourceType, resourceId, startDate, endDate }
    });
  }

  detectConflicts(startDate: string, endDate: string) {
    return this.get<any[]>(`/planning/conflicts`, {
      params: { startDate, endDate }
    });
  }

  getCompleteSchedule(startDate?: string, endDate?: string) {
    // Dates par défaut : 30 jours avant/après aujourd'hui
    const now = new Date();
    const defaultStart = startDate || new Date(now.getFullYear(), now.getMonth(), 1).toISOString();
    const defaultEnd = endDate || new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString();

    return this.get<any[]>(`/planning`, {
      params: { startDate: defaultStart, endDate: defaultEnd }
    });
  }

  // ==================== SUPPLIERS ====================
  getSuppliers() {
    return this.get<any[]>('/suppliers');
  }

  getSupplierById(id: number) {
    return this.get<any>(`/suppliers/${id}`);
  }

  createSupplier(data: any) {
    return this.post<any>('/suppliers', data);
  }

  updateSupplier(id: number, data: any) {
    return this.put<any>(`/suppliers/${id}`, data);
  }

  deleteSupplier(id: number) {
    return this.delete<void>(`/suppliers/${id}`);
  }

  getActiveSuppliers() {
    return this.get<any[]>('/suppliers/active');
  }

  // Material Requests
  getMaterialRequests() {
    return this.get<any[]>('/material-requests');
  }

  getMaterialRequestById(id: number) {
    return this.get<any>(`/material-requests/${id}`);
  }

  createMaterialRequest(data: any) {
    return this.post<any>('/material-requests', data);
  }

  updateMaterialRequest(id: number, data: any) {
    return this.put<any>(`/material-requests/${id}`, data);
  }

  deleteMaterialRequest(id: number) {
    return this.delete<void>(`/material-requests/${id}`);
  }

  getMaterialRequestsByStatus(status: string) {
    return this.get<any[]>(`/material-requests/status/${status}`);
  }

  approveMaterialRequest(id: number) {
    return this.post<any>(`/material-requests/${id}/approve`);
  }

  // Grouped Orders
  getGroupedOrders() {
    return this.get<any[]>('/grouped-orders');
  }

  getGroupedOrderById(id: number) {
    return this.get<any>(`/grouped-orders/${id}`);
  }

  createGroupedOrder(data: any) {
    return this.post<any>('/grouped-orders', data);
  }

  updateGroupedOrder(id: number, data: any) {
    return this.put<any>(`/grouped-orders/${id}`, data);
  }

  deleteGroupedOrder(id: number) {
    return this.delete<void>(`/grouped-orders/${id}`);
  }

  getGroupedOrdersByStatus(status: string) {
    return this.get<any[]>(`/grouped-orders/status/${status}`);
  }

  // ==================== DASHBOARD ====================
  getDashboardStats() {
    return this.get<any>('/dashboard/stats');
  }

  getRecentActivity() {
    return this.get<any[]>('/dashboard/recent-activity');
  }

  // ==================== EXPORT/IMPORT ====================
  getExportStatistics() {
    return this.get<any>('/export-import/statistics');
  }

  exportEquipmentCSV() {
    return this.get<Blob>('/export-import/equipment/csv', {
      responseType: 'blob'
    });
  }

  exportVehiclesCSV() {
    return this.get<Blob>('/export-import/vehicles/csv', {
      responseType: 'blob'
    });
  }

  exportPersonnelCSV() {
    return this.get<Blob>('/export-import/personnel/csv', {
      responseType: 'blob'
    });
  }
}

export default new ApiService();
