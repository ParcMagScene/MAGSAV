import React from 'react';
import { ServiceRequest } from '../types/entities';
import DetailDrawer from './DetailDrawer';

interface ServiceRequestDetailProps {
  serviceRequest: ServiceRequest | null;
  isOpen: boolean;
  onClose: () => void;
}

const ServiceRequestDetail: React.FC<ServiceRequestDetailProps> = ({
  serviceRequest,
  isOpen,
  onClose
}) => {
  const getStatusBadgeClass = (status: string) => {
    const statusMap: { [key: string]: string } = {
      'OPEN': 'detail-badge detail-badge-info',
      'IN_PROGRESS': 'detail-badge detail-badge-warning',
      'COMPLETED': 'detail-badge detail-badge-success',
      'CANCELLED': 'detail-badge detail-badge-danger'
    };
    return statusMap[status] || 'detail-badge';
  };

  const getPriorityBadgeClass = (priority: string) => {
    const priorityMap: { [key: string]: string } = {
      'LOW': 'detail-badge detail-badge-info',
      'MEDIUM': 'detail-badge detail-badge-warning',
      'HIGH': 'detail-badge detail-badge-danger',
      'URGENT': 'detail-badge detail-badge-danger'
    };
    return priorityMap[priority] || 'detail-badge';
  };

  const getPriorityLabel = (priority: string) => {
    const labels: { [key: string]: string } = {
      'LOW': 'Basse',
      'MEDIUM': 'Moyenne',
      'HIGH': 'Haute',
      'URGENT': 'Urgente'
    };
    return labels[priority] || priority;
  };

  return (
    <DetailDrawer
      isOpen={isOpen}
      onClose={onClose}
      title={serviceRequest?.title || serviceRequest?.requestNumber || 'Demande SAV'}
      width="700px"
      itemId={serviceRequest?.id}
    >
      {!serviceRequest ? null : (
        <>
          <div className="detail-section">
            <h3 className="detail-section-title">Informations générales</h3>
            <div className="detail-row">
              <span className="detail-label">N° Demande</span>
              <span className="detail-value">{serviceRequest.requestNumber || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Titre</span>
              <span className="detail-value">{serviceRequest.title || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Statut</span>
              <span className="detail-value">
                <span className={getStatusBadgeClass(serviceRequest.status || '')}>
                  {serviceRequest.status}
                </span>
              </span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Priorité</span>
              <span className="detail-value">
                <span className={getPriorityBadgeClass(serviceRequest.priority || '')}>
                  {getPriorityLabel(serviceRequest.priority || '')}
                </span>
              </span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Description</h3>
            <div className="detail-value" style={{ whiteSpace: 'pre-wrap' }}>
              {serviceRequest.description || '-'}
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Dates</h3>
            <div className="detail-row">
              <span className="detail-label">Date de demande</span>
              <span className="detail-value">
                {serviceRequest.requestDate
                  ? new Date(serviceRequest.requestDate).toLocaleDateString('fr-FR')
                  : '-'}
              </span>
            </div>
          </div>

          {serviceRequest.assignedTo && (
            <div className="detail-section">
              <h3 className="detail-section-title">Assignation</h3>
              <div className="detail-row">
                <span className="detail-label">Assigné à</span>
                <span className="detail-value">Technicien #{serviceRequest.assignedTo}</span>
              </div>
            </div>
          )}
        </>
      )}
    </DetailDrawer>
  );
};

export default ServiceRequestDetail;
