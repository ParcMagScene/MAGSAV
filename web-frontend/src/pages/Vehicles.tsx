import React, { useState, useEffect } from 'react';
import apiService from '../services/api.service';
import { Vehicle } from '../types/entities';
import { VehicleReservation } from '../types';
import DataTable from '../components/DataTable';
import StatCard from '../components/StatCard';
import LoadingState from '../components/LoadingState';
import VehicleDetail from '../components/VehicleDetail';
import { useApiData } from '../hooks/useApiData';
import { usePageContext } from '../contexts/PageContext';
import './Vehicles.css';

type TabType = 'vehicles' | 'reservations';

const Vehicles: React.FC = () => {
  console.log('🚗 [VEHICLES] Composant monté');

  const { setPageTitle } = usePageContext();

  // ✨ Refactorisation : utilisation du hook useApiData (2 appels parallèles)
  const { data: vehicles, loading: loadingVeh, error: errorVeh, reload: reloadVeh } =
    useApiData<Vehicle[]>(() => apiService.getVehicles());

  // TODO: Réservations désactivées - endpoint backend non implémenté
  // const { data: reservations, loading: loadingRes, error: errorRes } =
  //   useApiData<VehicleReservation[]>(() => apiService.getVehicleReservations());
  const reservations: VehicleReservation[] = [];
  const loadingRes = false;
  const errorRes = null;

  const { data: stats, loading: loadingStats, error: errorStats } =
    useApiData(() => apiService.getVehicleStats());

  const [activeTab, setActiveTab] = useState<TabType>('vehicles');
  const [selectedVehicle, setSelectedVehicle] = useState<Vehicle | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);

  // Configuration du header dynamique
  useEffect(() => {
    setPageTitle('🚐 Véhicules');
  }, [setPageTitle]);

  const loading = loadingVeh || loadingStats;
  const error = errorVeh || errorStats;

  console.log('🚗 [VEHICLES] État actuel:', {
    vehicles: vehicles?.length || 0,
    reservations: reservations?.length || 0,
    stats,
    loading: { loadingVeh, loadingRes, loadingStats },
    error: { errorVeh, errorRes, errorStats }
  });

  if (loading) {
    return (
      <div className="page-container">
        <LoadingState message="Chargement des véhicules..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-container">
        <div className="error-container">
          <h2>❌ Erreur</h2>
          <p>{error.message}</p>
          <button onClick={reloadVeh} className="btn-retry">
            Réessayer
          </button>
        </div>
      </div>
    );
  }

  const getStatusBadge = (status: string) => {
    const statusMap: { [key: string]: { label: string; className: string } } = {
      'AVAILABLE': { label: 'Disponible', className: 'status-badge status-available' },
      'IN_USE': { label: 'En utilisation', className: 'status-badge status-in-use' },
      'MAINTENANCE': { label: 'Maintenance', className: 'status-badge status-maintenance' },
      'OUT_OF_SERVICE': { label: 'Hors service', className: 'status-badge status-out-of-service' }
    };
    return statusMap[status] || { label: status, className: 'status-badge' };
  };

  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('fr-FR');
  };

  const vehicleColumns = [
    {
      key: 'licensePlate',
      label: 'Immatriculation',
      render: (value: string) => (
        <span className="registration-plate">{value || '-'}</span>
      )
    },
    {
      key: 'brand',
      label: 'Marque',
      render: (value: string) => value || '-'
    },
    { key: 'model', label: 'Modèle' },
    {
      key: 'type',
      label: 'Type',
      render: (value: string) => {
        if (!value) return '-';
        const typeMap: { [key: string]: string } = {
          'VAN': '🚐 Fourgon',
          'TRUCK': '🚚 Camion',
          'CAR': '🚗 Voiture',
          'UTILITY': '🚛 Utilitaire'
        };
        return typeMap[value] || value;
      }
    },
    {
      key: 'status',
      label: 'Statut',
      render: (value: string) => {
        if (!value) return '-';
        const info = getStatusBadge(value);
        return <span className={info.className}>{info.label}</span>;
      }
    },
    {
      key: 'lastMaintenanceDate',
      label: 'Dernière Maintenance',
      render: (value: string) => formatDate(value)
    }
  ];

  const reservationColumns = [
    { key: 'vehicleRegistration', label: 'Véhicule' },
    {
      key: 'startDate',
      label: 'Début',
      render: (value: string) => formatDate(value)
    },
    {
      key: 'endDate',
      label: 'Fin',
      render: (value: string) => formatDate(value)
    },
    { key: 'purpose', label: 'Objet' },
    { key: 'driver', label: 'Conducteur' },
    {
      key: 'status',
      label: 'Statut',
      render: (value: string) => {
        if (!value) return '-';
        const statusMap: { [key: string]: { label: string; className: string } } = {
          'PENDING': { label: 'En attente', className: 'status-badge status-pending' },
          'CONFIRMED': { label: 'Confirmé', className: 'status-badge status-confirmed' },
          'IN_PROGRESS': { label: 'En cours', className: 'status-badge status-in-progress' },
          'COMPLETED': { label: 'Terminé', className: 'status-badge status-completed' },
          'CANCELLED': { label: 'Annulé', className: 'status-badge status-cancelled' }
        };
        const info = statusMap[value] || { label: value, className: 'status-badge' };
        return <span className={info.className}>{info.label}</span>;
      }
    }
  ];

  return (
    <div className="vehicles-page">
      <div className="filters-bar">
        <div className="filter-group">
          <label>🔍</label>
          <input
            type="text"
            className="filter-input"
            placeholder="Immatriculation, marque..."
          />
        </div>
        <div className="filter-group">
          <label>Type</label>
          <select className="filter-select">
            <option value="">Tous</option>
            <option value="VAN">Fourgon</option>
            <option value="TRUCK">Camion</option>
            <option value="CAR">Voiture</option>
          </select>
        </div>
        <div className="filter-group">
          <label>Statut</label>
          <select className="filter-select">
            <option value="">Tous</option>
            <option value="AVAILABLE">Disponible</option>
            <option value="IN_USE">En utilisation</option>
            <option value="MAINTENANCE">Maintenance</option>
          </select>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary">🔧 Maintenance</button>
          <button className="btn btn-primary">➕ Nouveau Véhicule</button>
        </div>
      </div>

      <div className="tabs-header">
        <button
          className={`tab-button ${activeTab === 'vehicles' ? 'active' : ''}`}
          onClick={() => setActiveTab('vehicles')}
        >
          🚐 Liste des Véhicules ({vehicles?.length || 0})
        </button>
        <button
          className={`tab-button ${activeTab === 'reservations' ? 'active' : ''}`}
          onClick={() => setActiveTab('reservations')}
        >
          📅 Réservations ({reservations?.length || 0})
        </button>
      </div>

      {error && <div className="error-message">❌ {error}</div>}

      <div className="tabs-content">
        {activeTab === 'vehicles' && (
          <DataTable
            columns={vehicleColumns}
            data={vehicles || []}
            loading={loading}
            emptyMessage="Aucun véhicule trouvé"
            selectedItem={selectedVehicle}
            onRowClick={(vehicle) => {
              if (selectedVehicle?.id === vehicle.id) {
                setSelectedVehicle(null);
              } else {
                setSelectedVehicle(vehicle);
                setIsDetailOpen(true);
              }
            }}
          />
        )}

        {activeTab === 'reservations' && (
          <DataTable
            columns={reservationColumns}
            data={reservations || []}
            loading={loading}
            emptyMessage="Aucune réservation trouvée"
          />
        )}
      </div>

      <VehicleDetail
        vehicle={selectedVehicle}
        isOpen={isDetailOpen}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedVehicle(null);
        }}
      />
    </div>
  );
};

export default Vehicles;