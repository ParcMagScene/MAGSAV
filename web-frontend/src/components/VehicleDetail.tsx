import React from 'react';
import { Vehicle } from '../types/entities';
import DetailDrawer from './DetailDrawer';

interface VehicleDetailProps {
  vehicle: Vehicle | null;
  isOpen: boolean;
  onClose: () => void;
  onEdit?: () => void;
}

const VehicleDetail: React.FC<VehicleDetailProps> = ({ vehicle, isOpen, onClose, onEdit }) => {
  const getStatusBadgeClass = (status: string) => {
    const statusMap: { [key: string]: string } = {
      'AVAILABLE': 'detail-badge detail-badge-success',
      'IN_USE': 'detail-badge detail-badge-info',
      'MAINTENANCE': 'detail-badge detail-badge-warning',
      'OUT_OF_ORDER': 'detail-badge detail-badge-danger'
    };
    return statusMap[status] || 'detail-badge';
  };

  const getStatusLabel = (status: string) => {
    const labels: { [key: string]: string } = {
      'AVAILABLE': 'Disponible',
      'IN_USE': 'En utilisation',
      'MAINTENANCE': 'Maintenance',
      'OUT_OF_ORDER': 'Hors service'
    };
    return labels[status] || status;
  };

  return (
    <DetailDrawer
      isOpen={isOpen}
      onClose={onClose}
      title={vehicle?.licensePlate || `${vehicle?.brand || ''} ${vehicle?.model || ''}`.trim() || 'Véhicule'}
      width="700px"
      itemId={vehicle?.id}
      onEdit={onEdit}
    >
      {!vehicle ? null : (
        <>
          <div className="detail-section">
            <h3 className="detail-section-title">Informations générales</h3>
            <div className="detail-row">
              <span className="detail-label">Type</span>
              <span className="detail-value">{vehicle.type || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Immatriculation</span>
              <span className="detail-value">{vehicle.licensePlate || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Statut</span>
              <span className="detail-value">
                <span className={getStatusBadgeClass(vehicle.status || '')}>
                  {getStatusLabel(vehicle.status || '')}
                </span>
              </span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Caractéristiques</h3>
            <div className="detail-row">
              <span className="detail-label">Marque</span>
              <span className="detail-value">{vehicle.brand || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Modèle</span>
              <span className="detail-value">{vehicle.model || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Année</span>
              <span className="detail-value">{vehicle.year || '-'}</span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Kilométrage</h3>
            <div className="detail-row">
              <span className="detail-label">Kilométrage actuel</span>
              <span className="detail-value">
                {vehicle.mileage ? `${vehicle.mileage.toLocaleString('fr-FR')} km` : '-'}
              </span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Dates importantes</h3>
            <div className="detail-row">
              <span className="detail-label">Dernière révision</span>
              <span className="detail-value">
                {vehicle.lastMaintenanceDate
                  ? new Date(vehicle.lastMaintenanceDate).toLocaleDateString('fr-FR')
                  : '-'}
              </span>
            </div>
          </div>
        </>
      )}
    </DetailDrawer>
  );
};

export default VehicleDetail;
