import React, { useState, useEffect } from 'react';
import logger from '../services/logger.service';
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
  logger.debug('VEHICLES - Composant monté');

  const { setPageTitle } = usePageContext();

  // ÃƒÂ¢Ã…â€œÃ‚Â¨ Refactorisation : utilisation du hook useApiData (2 appels parallÃƒÆ’Ã‚Â¨les)
  const { data: vehicles, loading: loadingVeh, error: errorVeh, reload: reloadVeh } =
    useApiData<Vehicle[]>(() => apiService.getVehicles());

  // TODO: RÃƒÆ’Ã‚Â©servations dÃƒÆ’Ã‚Â©sactivÃƒÆ’Ã‚Â©es - endpoint backend non implÃƒÆ’Ã‚Â©mentÃƒÆ’Ã‚Â©
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
    setPageTitle('ÃƒÂ°Ã…Â¸Ã…Â¡Ã‚Â VÃƒÆ’Ã‚Â©hicules');
  }, [setPageTitle]);

  const loading = loadingVeh || loadingStats;
  const error = errorVeh || errorStats;

  logger.debug('ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â‚¬â€ [VEHICLES] ÃƒÆ’Ã¢â‚¬Â°tat actuel:', {
    vehicles: vehicles?.length || 0,
    reservations: reservations?.length || 0,
    stats,
    loading: { loadingVeh, loadingRes, loadingStats },
    error: { errorVeh, errorRes, errorStats }
  });

  if (loading) {
    return (
      <div className="page-container">
        <LoadingState message="Chargement des vÃƒÆ’Ã‚Â©hicules..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-container">
        <div className="error-container">
          <h2>ÃƒÂ¢Ã‚ÂÃ…â€™ Erreur</h2>
          <p>{error.message}</p>
          <button onClick={reloadVeh} className="btn-retry">
            RÃƒÆ’Ã‚Â©essayer
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
          'VAN': 'ÃƒÂ°Ã…Â¸Ã…Â¡Ã‚Â Fourgon',
          'VL': 'ÃƒÂ°Ã…Â¸Ã…Â¡Ã‚Â VL',
          'VL_17M3': 'ÃƒÂ°Ã…Â¸Ã…Â¡Ã‚Â VL 17mÃƒâ€šÃ‚Â³',
          'VL_20M3': 'ÃƒÂ°Ã…Â¸Ã…Â¡Ã‚Â VL 20mÃƒâ€šÃ‚Â³',
          'TRUCK': 'ÃƒÂ°Ã…Â¸Ã…Â¡Ã…Â¡ Camion',
          'PORTEUR': 'ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â‚¬Âº Porteur',
          'TRACTEUR': 'ÃƒÂ°Ã…Â¸Ã…Â¡Ã…â€œ Tracteur',
          'SEMI_REMORQUE': 'ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â‚¬Âº Semi-remorque',
          'CAR': 'ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â‚¬â€ Voiture',
          'UTILITY': 'ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â‚¬Âº Utilitaire'
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
      label: 'ModÃƒÆ’Ã‚Â¨le',
      render: (value: string) => value || '-'
    },
    {
      key: 'color',
      label: 'Couleur',
      render: (value: string) => value || '-'
    },
    {
      key: 'owner',
      label: 'PropriÃƒÆ’Ã‚Â©taire',
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
      label: 'DerniÃƒÆ’Ã‚Â¨re Maintenance',
      render: (value: string) => formatFrenchDate(value)
    }
  ];

  const reservationColumns = [
    { key: 'vehicleRegistration', label: 'VÃƒÆ’Ã‚Â©hicule' },
    {
      key: 'startDate',
      label: 'DÃƒÆ’Ã‚Â©but',
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
          <label>ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ‚Â</label>
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
            <option value="VL">ÃƒÂ°Ã…Â¸Ã…Â¡Ã‚Â VL</option>
            <option value="VL_17M3">ÃƒÂ°Ã…Â¸Ã…Â¡Ã‚Â VL 17mÃƒâ€šÃ‚Â³</option>
            <option value="VL_20M3">ÃƒÂ°Ã…Â¸Ã…Â¡Ã‚Â VL 20mÃƒâ€šÃ‚Â³</option>
            <option value="PORTEUR">ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â‚¬Âº Porteur</option>
            <option value="TRACTEUR">ÃƒÂ°Ã…Â¸Ã…Â¡Ã…â€œ Tracteur</option>
            <option value="SEMI_REMORQUE">ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â‚¬Âº Semi-remorque</option>
            <option value="OTHER">ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã‚Â¦ Autre</option>
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
            <option value="RENTED_OUT">LouÃƒÆ’Ã‚Â© externe</option>
            <option value="RESERVED">RÃƒÆ’Ã‚Â©servÃƒÆ’Ã‚Â©</option>
          </select>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary">ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ‚Â§ Maintenance</button>
          <button className="btn btn-primary">ÃƒÂ¢Ã…Â¾Ã¢â‚¬Â¢ Nouveau VÃƒÆ’Ã‚Â©hicule</button>
        </div>
      </div>

      <div className="tabs-header">
        <button
          className={`tab-button ${activeTab === 'vehicles' ? 'active' : ''}`}
          onClick={() => setActiveTab('vehicles')}
        >
          ÃƒÂ°Ã…Â¸Ã…Â¡Ã‚Â Liste des VÃƒÆ’Ã‚Â©hicules ({vehicles?.length || 0})
        </button>
        <button
          className={`tab-button ${activeTab === 'reservations' ? 'active' : ''}`}
          onClick={() => setActiveTab('reservations')}
        >
          ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã¢â‚¬Â¦ RÃƒÆ’Ã‚Â©servations ({reservations?.length || 0})
        </button>
      </div>

      {error && <div className="error-message">ÃƒÂ¢Ã‚ÂÃ…â€™ {error}</div>}

      <div className="tabs-content">
        {activeTab === 'vehicles' && (
          <DataTable
            columns={vehicleColumns}
            data={vehicles || []}
            loading={loading}
            emptyMessage="Aucun vÃƒÆ’Ã‚Â©hicule trouvÃƒÆ’Ã‚Â©"
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
            emptyMessage="Aucune rÃƒÆ’Ã‚Â©servation trouvÃƒÆ’Ã‚Â©e"
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
              logger.error('Erreur lors de la mise ÃƒÆ’Ã‚Â  jour:', error);
              alert('Erreur lors de la mise ÃƒÆ’Ã‚Â  jour du vÃƒÆ’Ã‚Â©hicule');
            }
          }}
        />
      )}
    </div>
  );
};

export default Vehicles;