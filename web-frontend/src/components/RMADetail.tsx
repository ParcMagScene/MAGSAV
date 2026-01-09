import React from 'react';
import { RMA } from '../types/entities';
import DetailDrawer from './DetailDrawer';

interface RMADetailProps {
    rma: RMA | null;
    isOpen: boolean;
    onClose: () => void;
}

const RMADetail: React.FC<RMADetailProps> = ({
    rma,
    isOpen,
    onClose
}) => {
    const getStatusBadgeClass = (status: string) => {
        const statusMap: { [key: string]: string } = {
            'REQUESTED': 'detail-badge detail-badge-info',
            'APPROVED': 'detail-badge detail-badge-success',
            'SHIPPED': 'detail-badge detail-badge-warning',
            'RECEIVED': 'detail-badge detail-badge-success',
            'REFUNDED': 'detail-badge detail-badge-success',
            'REJECTED': 'detail-badge detail-badge-danger'
        };
        return statusMap[status] || 'detail-badge';
    };

    const getStatusLabel = (status: string) => {
        const labels: { [key: string]: string } = {
            'REQUESTED': 'Demandé',
            'APPROVED': 'Approuvé',
            'SHIPPED': 'Expédié',
            'RECEIVED': 'Reçu',
            'REFUNDED': 'Remboursé',
            'REJECTED': 'Rejeté'
        };
        return labels[status] || status;
    };

    return (
        <DetailDrawer
            isOpen={isOpen}
            onClose={onClose}
            title={rma?.rmaNumber || 'RMA'}
            width="700px"
            itemId={rma?.id}
        >
            {!rma ? null : (
                <>
                    <div className="detail-section">
                        <h3 className="detail-section-title">Informations générales</h3>
                        <div className="detail-row">
                            <span className="detail-label">N° RMA</span>
                            <span className="detail-value">{rma.rmaNumber || '-'}</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">Code LOCMAT</span>
                            <span className="detail-value">{rma.equipmentInternalReference || '-'}</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">UID</span>
                            <span className="detail-value">{rma.equipmentQrCode || '-'}</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">Statut</span>
                            <span className="detail-value">
                                <span className={getStatusBadgeClass(rma.status || '')}>
                                    {getStatusLabel(rma.status || '')}
                                </span>
                            </span>
                        </div>
                    </div>

                    <div className="detail-section">
                        <h3 className="detail-section-title">Raison du retour</h3>
                        <div className="detail-value" style={{ whiteSpace: 'pre-wrap' }}>
                            {rma.reason || '-'}
                        </div>
                    </div>

                    {rma.notes && (
                        <div className="detail-section">
                            <h3 className="detail-section-title">Notes</h3>
                            <div className="detail-value" style={{ whiteSpace: 'pre-wrap' }}>
                                {rma.notes}
                            </div>
                        </div>
                    )}

                    <div className="detail-section">
                        <h3 className="detail-section-title">Dates</h3>
                        <div className="detail-row">
                            <span className="detail-label">Date de demande</span>
                            <span className="detail-value">
                                {rma.requestDate
                                    ? new Date(rma.requestDate).toLocaleDateString('fr-FR')
                                    : '-'}
                            </span>
                        </div>
                    </div>

                    {(rma.equipmentId || rma.clientId) && (
                        <div className="detail-section">
                            <h3 className="detail-section-title">Références</h3>
                            {rma.equipmentId && (
                                <div className="detail-row">
                                    <span className="detail-label">Équipement</span>
                                    <span className="detail-value">#{rma.equipmentId}</span>
                                </div>
                            )}
                            {rma.clientId && (
                                <div className="detail-row">
                                    <span className="detail-label">Client</span>
                                    <span className="detail-value">#{rma.clientId}</span>
                                </div>
                            )}
                        </div>
                    )}
                </>
            )}
        </DetailDrawer>
    );
};

export default RMADetail;
