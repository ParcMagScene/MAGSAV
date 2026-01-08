import React from 'react';
import { Equipment } from '../types';
import DetailDrawer from './DetailDrawer';

interface EquipmentDetailProps {
  equipment: Equipment | null;
  isOpen: boolean;
  onClose: () => void;
}

const EquipmentDetail: React.FC<EquipmentDetailProps> = ({ equipment, isOpen, onClose }) => {
  const getStatusBadgeClass = (status: string) => {
    const statusMap: { [key: string]: string } = {
      'AVAILABLE': 'detail-badge detail-badge-success',
      'IN_USE': 'detail-badge detail-badge-info',
      'MAINTENANCE': 'detail-badge detail-badge-warning',
      'OUT_OF_ORDER': 'detail-badge detail-badge-danger',
      'RESERVED': 'detail-badge detail-badge-info',
      'RETIRED': 'detail-badge detail-badge-danger'
    };
    return statusMap[status] || 'detail-badge';
  };

  const getStatusLabel = (status: string) => {
    const labels: { [key: string]: string } = {
      'AVAILABLE': 'Disponible',
      'IN_USE': 'En utilisation',
      'MAINTENANCE': 'Maintenance',
      'OUT_OF_ORDER': 'Hors service',
      'RESERVED': 'Réservé',
      'RETIRED': 'Retiré'
    };
    return labels[status] || status;
  };

  return (
    <DetailDrawer
      isOpen={isOpen}
      onClose={onClose}
      title={equipment?.name || equipment?.internalReference || 'Équipement'}
      width="700px"
      itemId={equipment?.id}
    >
      {!equipment ? null : (
        <>
          <div className="detail-section">
            <h3 className="detail-section-title">Informations générales</h3>
            <div className="detail-row">
              <span className="detail-label">Nom</span>
              <span className="detail-value">{equipment.name || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Référence interne</span>
              <span className="detail-value">{equipment.internalReference || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Catégorie</span>
              <span className="detail-value">{equipment.category || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Statut</span>
              <span className="detail-value">
                <span className={getStatusBadgeClass(equipment.status || '')}>
                  {getStatusLabel(equipment.status || '')}
                </span>
              </span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Détails techniques</h3>
            <div className="detail-row">
              <span className="detail-label">N° Série</span>
              <span className="detail-value">{equipment.serialNumber || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Marque</span>
              <span className="detail-value">{equipment.brand || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Modèle</span>
              <span className="detail-value">{equipment.model || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Description</span>
              <span className="detail-value">{equipment.description || '-'}</span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Acquisition</h3>
            <div className="detail-row">
              <span className="detail-label">Prix d'achat</span>
              <span className="detail-value">
                {equipment.purchasePrice
                  ? `${equipment.purchasePrice.toLocaleString('fr-FR')} €`
                  : '-'}
              </span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Fournisseur</span>
              <span className="detail-value">{equipment.supplier || '-'}</span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Localisation</h3>
            <div className="detail-row">
              <span className="detail-label">Emplacement</span>
              <span className="detail-value">{equipment.location || '-'}</span>
            </div>
          </div>

          {equipment.notes && (
            <div className="detail-section">
              <h3 className="detail-section-title">Notes</h3>
              <div className="detail-value" style={{ whiteSpace: 'pre-wrap' }}>
                {equipment.notes}
              </div>
            </div>
          )}
        </>
      )}
    </DetailDrawer>
  );
};

export default EquipmentDetail;
