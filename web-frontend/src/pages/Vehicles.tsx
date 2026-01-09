import React, { useState, useEffect } from 'react';
import apiService from '../services/api.service';
import { Vehicle } from '../types/entities';
import { VehicleReservation } from '../types';
import DataTable from '../components/DataTable';
import StatCard from '../components/StatCard';
import LoadingState from '../components/LoadingState';
import VehicleDetail from '../components/VehicleDetail';
import VehicleModal from '../components/VehicleModal';
import { useApiData } from '../hooks/useApiData';
import { usePageContext } from '../contexts/PageContext';
import { translateStatus, formatFrenchDate } from '../utils/translations';
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
  const [isModalOpen, setIsModalOpen] = useState(false);

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
    const label = translateStatus(status, 'vehicle');
    const className = `status-badge status-${status.toLowerCase().replace('_', '-')}`;
    return { label, className };
  };

  const vehicleColumns = [
    {
      key: 'name',
      label: 'Nom',
      render: (value: string) => value || '-'
    },
    {
      key: 'licensePlate',
      label: 'Immatriculation',
      render: (value: string) => (
        <span className="registration-plate">{value || '-'}</span>
      )
    },
    {
      key: 'type',
      label: 'Type',
      render: (value: string) => {
        if (!value) return '-';
        const typeMap: { [key: string]: string } = {
          'VAN': '🚐 Fourgon',
          'VL': '🚐 VL',
          'VL_17M3': '🚐 VL 17m³',
          'VL_20M3': '🚐 VL 20m³',
          'TRUCK': '🚚 Camion',
          'PORTEUR': '🚛 Porteur',
          'TRACTEUR': '🚜 Tracteur',
          'SEMI_REMORQUE': '🚛 Semi-remorque',
          'CAR': '🚗 Voiture',
          'UTILITY': '🚛 Utilitaire'
        };
        return typeMap[value] || value;
      }
    },
    {
      key: 'brand',
      label: 'Marque',
      render: (value: string) => value || '-'
    },
    {
      key: 'model',
      label: 'Modèle',
      render: (value: string) => value || '-'
    },
    {
      key: 'color',
      label: 'Couleur',
      render: (value: string) => value || '-'
    },
    {
      key: 'owner',
      label: 'Propriétaire',
      render: (value: string) => value || '-'
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
      render: (value: string) => formatFrenchDate(value)
    }
  ];

  const reservationColumns = [
    { key: 'vehicleRegistration', label: 'Véhicule' },
    {
      key: 'startDate',
      label: 'Début',
      render: (value: string) => formatFrenchDate(value)
    },
    {
      key: 'endDate',
      label: 'Fin',
      render: (value: string) => formatFrenchDate(value)
    },
    { key: 'purpose', label: 'Objet' },
    { key: 'driver', label: 'Conducteur' },
    {
      key: 'status',
      label: 'Statut',
      render: (value: string) => {
        if (!value) return '-';
        const label = translateStatus(value, 'reservation');
        const className = `status-badge status-${value.toLowerCase().replace('_', '-')}`;
        return <span className={className}>{label}</span>;
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
            <option value="VL">🚐 VL</option>
            <option value="VL_17M3">🚐 VL 17m³</option>
            <option value="VL_20M3">🚐 VL 20m³</option>
            <option value="PORTEUR">🚛 Porteur</option>
            <option value="TRACTEUR">🚜 Tracteur</option>
            <option value="SEMI_REMORQUE">🚛 Semi-remorque</option>
            <option value="OTHER">📦 Autre</option>
          </select>
        </div>
        <div className="filter-group">
          <label>Statut</label>
          <select className="filter-select">
            <option value="">Tous</option>
            <option value="AVAILABLE">Disponible</option>
            <option value="IN_USE">En utilisation</option>
            <option value="MAINTENANCE">En maintenance</option>
            <option value="OUT_OF_ORDER">Hors service</option>
            <option value="RENTED_OUT">Loué externe</option>
            <option value="RESERVED">Réservé</option>
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
                setIsDetailOpen(false);
              } else {
                setSelectedVehicle(vehicle);
                setIsDetailOpen(true);
              }
            }}
            onEdit={(vehicle) => {
              setSelectedVehicle(vehicle);
              setIsModalOpen(true);
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
        onEdit={() => {
          setIsModalOpen(true);
        }}
      />

      {selectedVehicle && isModalOpen && (
        <VehicleModal
          vehicle={selectedVehicle}
          isOpen={isModalOpen}
          onClose={() => {
            setIsModalOpen(false);
          }}
          onSave={async (updatedVehicle) => {
            try {
              await apiService.put(`/api/vehicles/${updatedVehicle.id}`, updatedVehicle);
              setIsModalOpen(false);
              reloadVeh();
            } catch (error) {
              console.error('Erreur lors de la mise à jour:', error);
              alert('Erreur lors de la mise à jour du véhicule');
            }
          }}
        />
      )}
    </div>
  );
};

export default Vehicles;