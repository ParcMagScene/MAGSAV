import React from 'react';
import { Repair } from '../types/entities';
import DetailDrawer from './DetailDrawer';

interface RepairDetailProps {
    repair: Repair | null;
    isOpen: boolean;
    onClose: () => void;
}

const RepairDetail: React.FC<RepairDetailProps> = ({
    repair,
    isOpen,
    onClose
}) => {
    const getStatusBadgeClass = (status: string) => {
        const statusMap: { [key: string]: string } = {
            'PENDING': 'detail-badge detail-badge-info',
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

    const getStatusLabel = (status: string) => {
        const labels: { [key: string]: string } = {
            'PENDING': 'En attente',
            'IN_PROGRESS': 'En cours',
            'COMPLETED': 'Terminée',
            'CANCELLED': 'Annulée'
        };
        return labels[status] || status;
    };

    return (
        <DetailDrawer
            isOpen={isOpen}
            onClose={onClose}
            title={repair?.repairNumber || 'Réparation'}
            width="700px"
            itemId={repair?.id}
        >
            {!repair ? null : (
                <>
                    <div className="detail-section">
                        <h3 className="detail-section-title">Informations générales</h3>
                        <div className="detail-row">
                            <span className="detail-label">N° Réparation</span>
                            <span className="detail-value">{repair.repairNumber || '-'}</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">Statut</span>
                            <span className="detail-value">
                                <span className={getStatusBadgeClass(repair.status || '')}>
                                    {getStatusLabel(repair.status || '')}
                                </span>
                            </span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">Priorité</span>
                            <span className="detail-value">
                                <span className={getPriorityBadgeClass(repair.priority || '')}>
                                    {getPriorityLabel(repair.priority || '')}
                                </span>
                            </span>
                        </div>
                    </div>

                    <div className="detail-section">
                        <h3 className="detail-section-title">Description</h3>
                        <div className="detail-value" style={{ whiteSpace: 'pre-wrap' }}>
                            {repair.description || '-'}
                        </div>
                    </div>

                    <div className="detail-section">
                        <h3 className="detail-section-title">Dates</h3>
                        <div className="detail-row">
                            <span className="detail-label">Date de début</span>
                            <span className="detail-value">
                                {repair.startDate
                                    ? new Date(repair.startDate).toLocaleDateString('fr-FR')
                                    : '-'}
                            </span>
                        </div>
                        {repair.endDate && (
                            <div className="detail-row">
                                <span className="detail-label">Date de fin</span>
                                <span className="detail-value">
                                    {new Date(repair.endDate).toLocaleDateString('fr-FR')}
                                </span>
                            </div>
                        )}
                    </div>

                    {repair.cost && (
                        <div className="detail-section">
                            <h3 className="detail-section-title">Coût</h3>
                            <div className="detail-row">
                                <span className="detail-label">Coût</span>
                                <span className="detail-value">{repair.cost.toFixed(2)} €</span>
                            </div>
                        </div>
                    )}

                    {repair.technicianId && (
                        <div className="detail-section">
                            <h3 className="detail-section-title">Technicien</h3>
                            <div className="detail-row">
                                <span className="detail-label">Technicien ID</span>
                                <span className="detail-value">#{repair.technicianId}</span>
                            </div>
                        </div>
                    )}
                </>
            )}
        </DetailDrawer>
    );
};

export default RepairDetail;
