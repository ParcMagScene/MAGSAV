import React from 'react';
import DetailDrawer from './DetailDrawer';

interface Project {
    id: number;
    name: string;
    code?: string;
    clientId?: number;
    clientName?: string;
    startDate?: string;
    endDate?: string;
    status?: string;
    totalAmount?: number;
    description?: string;
}

interface SalesInstallationDetailProps {
    project: Project | null;
    isOpen: boolean;
    onClose: () => void;
}

const SalesInstallationDetail: React.FC<SalesInstallationDetailProps> = ({ project, isOpen, onClose }) => {
    return (
        <DetailDrawer
            isOpen={isOpen}
            onClose={onClose}
            title={project?.name || project?.code || 'Projet'}
            width="700px"
            itemId={project?.id}
        >
            {!project ? null : (
                <>
                    <div className="detail-section">
                        <h3>📋 Informations générales</h3>
                        <div className="detail-grid">
                            <div className="detail-item">
                                <span className="detail-label">Nom du projet</span>
                                <span className="detail-value">{project.name}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Code</span>
                                <span className="detail-value">{project.code || '-'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Client</span>
                                <span className="detail-value">{project.clientName || '-'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Statut</span>
                                <span className={`status-badge status-${project.status?.toLowerCase()}`}>
                                    {project.status || 'Non défini'}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="detail-section">
                        <h3>📅 Dates</h3>
                        <div className="detail-grid">
                            <div className="detail-item">
                                <span className="detail-label">Date de début</span>
                                <span className="detail-value">
                                    {project.startDate ? new Date(project.startDate).toLocaleDateString('fr-FR') : '-'}
                                </span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Date de fin</span>
                                <span className="detail-value">
                                    {project.endDate ? new Date(project.endDate).toLocaleDateString('fr-FR') : '-'}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="detail-section">
                        <h3>💰 Financier</h3>
                        <div className="detail-grid">
                            <div className="detail-item">
                                <span className="detail-label">Montant total</span>
                                <span className="detail-value">
                                    {project.totalAmount ? `${project.totalAmount.toLocaleString('fr-FR')} €` : '-'}
                                </span>
                            </div>
                        </div>
                    </div>

                    {project.description && (
                        <div className="detail-section">
                            <h3>📝 Description</h3>
                            <p className="detail-notes">{project.description}</p>
                        </div>
                    )}
                </>
            )}
        </DetailDrawer>
    );
};

export default SalesInstallationDetail;
