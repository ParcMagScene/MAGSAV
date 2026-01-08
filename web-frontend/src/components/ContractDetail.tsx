import React from 'react';
import { Contract } from '../types/entities';
import DetailDrawer from './DetailDrawer';

interface ContractDetailProps {
  contract: Contract | null;
  isOpen: boolean;
  onClose: () => void;
}

const ContractDetail: React.FC<ContractDetailProps> = ({ contract, isOpen, onClose }) => {
  const getStatusBadgeClass = (status: string) => {
    const statusMap: { [key: string]: string } = {
      'ACTIVE': 'detail-badge detail-badge-success',
      'EXPIRED': 'detail-badge detail-badge-danger',
      'PENDING': 'detail-badge detail-badge-warning',
      'CANCELLED': 'detail-badge detail-badge-danger'
    };
    return statusMap[status] || 'detail-badge';
  };

  const getStatusLabel = (status: string) => {
    const labels: { [key: string]: string } = {
      'ACTIVE': 'Actif',
      'EXPIRED': 'Expiré',
      'PENDING': 'En attente',
      'CANCELLED': 'Annulé'
    };
    return labels[status] || status;
  };

  return (
    <DetailDrawer
      isOpen={isOpen}
      onClose={onClose}
      title={contract?.contractNumber || 'Contrat'}
      width="700px"
      itemId={contract?.id}
    >
      {!contract ? null : (
        <>
          <div className="detail-section">
            <h3 className="detail-section-title">Informations générales</h3>
            <div className="detail-row">
              <span className="detail-label">N° Contrat</span>
              <span className="detail-value">{contract.contractNumber || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Statut</span>
              <span className="detail-value">
                <span className={getStatusBadgeClass(contract.status || '')}>
                  {getStatusLabel(contract.status || '')}
                </span>
              </span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Période</h3>
            <div className="detail-row">
              <span className="detail-label">Date début</span>
              <span className="detail-value">
                {contract.startDate
                  ? new Date(contract.startDate).toLocaleDateString('fr-FR')
                  : '-'}
              </span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Date fin</span>
              <span className="detail-value">
                {contract.endDate
                  ? new Date(contract.endDate).toLocaleDateString('fr-FR')
                  : '-'}
              </span>
            </div>
          </div>

          {contract.description && (
            <div className="detail-section">
              <h3 className="detail-section-title">Description</h3>
              <div className="detail-value" style={{ whiteSpace: 'pre-wrap' }}>
                {contract.description}
              </div>
            </div>
          )}
        </>
      )}
    </DetailDrawer>
  );
};

export default ContractDetail;
