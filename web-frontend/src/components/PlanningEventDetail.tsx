import React from 'react';
import DetailDrawer from './DetailDrawer';

interface PlanningEvent {
    id: string;
    type: 'PERSONNEL' | 'VEHICLE';
    resourceId: number;
    resourceName: string;
    projectId?: number;
    projectName?: string;
    startDate: string;
    endDate: string;
    description?: string;
    status: string;
}

interface PlanningEventDetailProps {
    event: PlanningEvent | null;
    isOpen: boolean;
    onClose: () => void;
    onEdit?: () => void;
}

const PlanningEventDetail: React.FC<PlanningEventDetailProps> = ({ event, isOpen, onClose, onEdit }) => {
    return (
        <DetailDrawer
            isOpen={isOpen}
            onClose={onClose}
            title={event?.projectName || event?.resourceName || 'Événement'}
            width="700px"
            itemId={event?.id}
            onEdit={onEdit}
        >
            {!event ? null : (
                <>
                    <div className="detail-section">
                        <h3>📋 Informations générales</h3>
                        <div className="detail-grid">
                            <div className="detail-item">
                                <span className="detail-label">Type</span>
                                <span className="detail-value">
                                    {event.type === 'PERSONNEL' ? '👤 Personnel' : '🚐 Véhicule'}
                                </span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Ressource</span>
                                <span className="detail-value">{event.resourceName}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Projet</span>
                                <span className="detail-value">{event.projectName || '-'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Statut</span>
                                <span className={`status-badge status-${event.status.toLowerCase()}`}>
                                    {event.status}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="detail-section">
                        <h3>📅 Période</h3>
                        <div className="detail-grid">
                            <div className="detail-item">
                                <span className="detail-label">Date de début</span>
                                <span className="detail-value">
                                    {new Date(event.startDate).toLocaleDateString('fr-FR')}
                                </span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Date de fin</span>
                                <span className="detail-value">
                                    {new Date(event.endDate).toLocaleDateString('fr-FR')}
                                </span>
                            </div>
                        </div>
                    </div>

                    {event.description && (
                        <div className="detail-section">
                            <h3>📝 Description</h3>
                            <p className="detail-notes">{event.description}</p>
                        </div>
                    )}
                </>
            )}
        </DetailDrawer>
    );
};

export default PlanningEventDetail;
