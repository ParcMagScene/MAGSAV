import React, { useState, useEffect } from 'react';
import apiService from '../services/api.service';
import DataTable from '../components/DataTable';
import StatCard from '../components/StatCard';
import LoadingState from '../components/LoadingState';
import ServiceRequestDetail from '../components/ServiceRequestDetail';
import RepairDetail from '../components/RepairDetail';
import RMADetail from '../components/RMADetail';
import { useApiData } from '../hooks/useApiData';
import { usePageContext } from '../contexts/PageContext';
import { ServiceRequest, Repair, RMA, ServiceRequestStats } from '../types/entities';
import './ServiceRequests.css';

type TabType = 'requests' | 'repairs' | 'rma';

const ServiceRequests: React.FC = () => {
  const { setPageTitle } = usePageContext();
  const [activeTab, setActiveTab] = useState<TabType>('requests');
  const [selectedRequest, setSelectedRequest] = useState<ServiceRequest | null>(null);
  const [selectedRepair, setSelectedRepair] = useState<Repair | null>(null);
  const [selectedRMA, setSelectedRMA] = useState<RMA | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);

  useEffect(() => {
    setPageTitle('🔧 SAV & Interventions');
  }, [setPageTitle]);

  // ✨ Refactorisation : 4 appels API avec useApiData
  const { data: serviceRequests, loading: loadingRequests } = useApiData<ServiceRequest[]>(
    () => apiService.getServiceRequests()
  );
  const { data: repairs, loading: loadingRepairs } = useApiData<Repair[]>(
    () => apiService.getRepairs()
  );
  const { data: rmas, loading: loadingRMAs } = useApiData<RMA[]>(
    () => apiService.getRMAs()
  );
  const { data: stats, loading: loadingStats } = useApiData<ServiceRequestStats>(
    () => apiService.getServiceRequestStats()
  );

  const loading = loadingRequests || loadingRepairs || loadingRMAs || loadingStats;

  if (loading) {
    return (
      <div className="page-container">
        <LoadingState message="Chargement des données SAV..." />
      </div>
    );
  }

  const requestColumns = [
    { key: 'requestNumber', label: 'N° Demande', width: '120px' },
    { key: 'title', label: 'Titre' },
    {
      key: 'status',
      label: 'Statut',
      render: (value: string) => (
        <span className={`status-badge status-${value.toLowerCase()}`}>{value}</span>
      ),
    },
    {
      key: 'priority',
      label: 'Priorité',
      render: (value: string) => (
        <span className={`priority-badge priority-${value.toLowerCase()}`}>{value}</span>
      ),
    },
    {
      key: 'requestDate',
      label: 'Date',
      render: (value: string) => new Date(value).toLocaleDateString('fr-FR'),
    }
  ];

  const repairColumns = [
    { key: 'repairNumber', label: 'N° Réparation', width: '120px' },
    { key: 'description', label: 'Description' },
    {
      key: 'status',
      label: 'Statut',
      render: (value: string) => (
        <span className={`status-badge status-${value.toLowerCase()}`}>{value}</span>
      ),
    },
    {
      key: 'priority',
      label: 'Priorité',
      render: (value: string) => (
        <span className={`priority-badge priority-${value.toLowerCase()}`}>{value}</span>
      ),
    },
    {
      key: 'startDate',
      label: 'Début',
      render: (value: string) => value ? new Date(value).toLocaleDateString('fr-FR') : '-',
    },
  ];

  const rmaColumns = [
    { key: 'rmaNumber', label: 'N° RMA', width: '120px' },
    { key: 'reason', label: 'Raison' },
    {
      key: 'status',
      label: 'Statut',
      render: (value: string) => (
        <span className={`status-badge status-${value.toLowerCase()}`}>{value}</span>
      ),
    },
    {
      key: 'requestDate',
      label: 'Date Demande',
      render: (value: string) => new Date(value).toLocaleDateString('fr-FR'),
    },
  ];

  return (
    <div className="service-requests-page">
      <div className="filters-bar">
        <div className="filter-group">
          <label>🔍</label>
          <input
            type="text"
            className="filter-input"
            placeholder="N° demande, titre..."
          />
        </div>
        <div className="filter-group">
          <label>Statut</label>
          <select className="filter-select">
            <option value="">Tous</option>
            <option value="pending">En attente</option>
            <option value="in-progress">En cours</option>
            <option value="completed">Terminé</option>
          </select>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary">📄 Exporter</button>
          <button className="btn btn-primary">➕ Nouvelle Demande</button>
        </div>
      </div>

      <div className="tabs-header">
        <button
          className={`tab-button ${activeTab === 'requests' ? 'active' : ''}`}
          onClick={() => setActiveTab('requests')}
        >
          📋 Demandes d'Intervention ({serviceRequests?.length || 0})
        </button>
        <button
          className={`tab-button ${activeTab === 'repairs' ? 'active' : ''}`}
          onClick={() => setActiveTab('repairs')}
        >
          🔧 Réparations ({repairs?.length || 0})
        </button>
        <button
          className={`tab-button ${activeTab === 'rma' ? 'active' : ''}`}
          onClick={() => setActiveTab('rma')}
        >
          ↩️ RMA ({rmas?.length || 0})
        </button>
      </div>

      <div className="tabs-content">
        {activeTab === 'requests' && (
          <DataTable
            data={serviceRequests || []}
            columns={requestColumns}
            loading={loading}
            emptyMessage="Aucune demande d'intervention"
            selectedItem={selectedRequest}
            onRowClick={(request: ServiceRequest) => {
              if (selectedRequest?.id === request.id) {
                setSelectedRequest(null);
              } else {
                setSelectedRequest(request);
                setSelectedRepair(null);
                setSelectedRMA(null);
                setIsDetailOpen(true);
              }
            }}
          />
        )}
        {activeTab === 'repairs' && (
          <DataTable
            data={repairs || []}
            columns={repairColumns}
            loading={loading}
            emptyMessage="Aucune réparation"
            selectedItem={selectedRepair}
            onRowClick={(repair: Repair) => {
              if (selectedRepair?.id === repair.id) {
                setSelectedRepair(null);
              } else {
                setSelectedRepair(repair);
                setSelectedRequest(null);
                setSelectedRMA(null);
                setIsDetailOpen(true);
              }
            }}
          />
        )}
        {activeTab === 'rma' && (
          <DataTable
            data={rmas || []}
            columns={rmaColumns}
            loading={loading}
            emptyMessage="Aucun RMA"
            selectedItem={selectedRMA}
            onRowClick={(rma: RMA) => {
              if (selectedRMA?.id === rma.id) {
                setSelectedRMA(null);
              } else {
                setSelectedRMA(rma);
                setSelectedRequest(null);
                setSelectedRepair(null);
                setIsDetailOpen(true);
              }
            }}
          />
        )}
      </div>

      <ServiceRequestDetail
        serviceRequest={selectedRequest}
        isOpen={isDetailOpen && activeTab === 'requests'}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedRequest(null);
        }}
      />

      <RepairDetail
        repair={selectedRepair}
        isOpen={isDetailOpen && activeTab === 'repairs'}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedRepair(null);
        }}
      />

      <RMADetail
        rma={selectedRMA}
        isOpen={isDetailOpen && activeTab === 'rma'}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedRMA(null);
        }}
      />
    </div>
  );
};

export default ServiceRequests;