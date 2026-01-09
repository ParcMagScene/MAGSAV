import React, { useState } from 'react';
import './Modal.css';
import { ServiceRequest } from '../types/entities';

interface ValidationModalProps {
    isOpen: boolean;
    onClose: () => void;
    onValidate: (actionType: 'DIAGNOSTIC' | 'INTERNAL_REPAIR' | 'RMA' | 'SCRAP') => void;
    request: ServiceRequest | null;
}

const ValidationModal: React.FC<ValidationModalProps> = ({
    isOpen,
    onClose,
    onValidate,
    request
}) => {
    const [selectedAction, setSelectedAction] = useState<'DIAGNOSTIC' | 'INTERNAL_REPAIR' | 'RMA' | 'SCRAP' | null>(null);

    if (!isOpen) return null;

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (selectedAction) {
            onValidate(selectedAction);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>✅ Valider la demande {request?.requestNumber}</h2>
                    <button className="modal-close" onClick={onClose}>✕</button>
                </div>

                <form onSubmit={handleSubmit} className="modal-body">
                    <div className="validation-options">
                        {/* Détails de la demande */}
                        <div style={{ marginBottom: '24px', padding: '16px', backgroundColor: '#f8f9fa', borderRadius: '8px' }}>
                            <h3 style={{ marginTop: 0, marginBottom: '12px', fontSize: '16px' }}>📋 Détails de la demande</h3>
                            <div style={{ display: 'grid', gridTemplateColumns: '150px 1fr', gap: '8px', fontSize: '14px' }}>
                                <strong>Titre:</strong>
                                <span>{request?.title}</span>

                                <strong>Description:</strong>
                                <span style={{ whiteSpace: 'pre-wrap' }}>{request?.description || '-'}</span>

                                {request?.equipmentInternalReference && (
                                    <>
                                        <strong>Code LOCMAT:</strong>
                                        <span>{request.equipmentInternalReference}</span>
                                    </>
                                )}

                                {request?.equipmentQrCode && (
                                    <>
                                        <strong>UID:</strong>
                                        <span>{request.equipmentQrCode}</span>
                                    </>
                                )}
                            </div>
                        </div>

                        <p style={{ marginBottom: '20px', color: '#666' }}>
                            Choisissez la démarche à suivre pour cette demande d'intervention :
                        </p>

                        <div className="action-choice">
                            <label className="action-option">
                                <input
                                    type="radio"
                                    name="action"
                                    value="DIAGNOSTIC"
                                    checked={selectedAction === 'DIAGNOSTIC'}
                                    onChange={() => setSelectedAction('DIAGNOSTIC')}
                                />
                                <div className="action-content">
                                    <span className="action-icon">🔍</span>
                                    <div>
                                        <strong>Diagnostique Interne</strong>
                                        <p>Créer une fiche de diagnostique pour analyse et évaluation</p>
                                    </div>
                                </div>
                            </label>

                            <label className="action-option">
                                <input
                                    type="radio"
                                    name="action"
                                    value="INTERNAL_REPAIR"
                                    checked={selectedAction === 'INTERNAL_REPAIR'}
                                    onChange={() => setSelectedAction('INTERNAL_REPAIR')}
                                />
                                <div className="action-content">
                                    <span className="action-icon">🔧</span>
                                    <div>
                                        <strong>Réparation interne</strong>
                                        <p>Créer une fiche de réparation pour intervention en interne</p>
                                    </div>
                                </div>
                            </label>

                            <label className="action-option">
                                <input
                                    type="radio"
                                    name="action"
                                    value="RMA"
                                    checked={selectedAction === 'RMA'}
                                    onChange={() => setSelectedAction('RMA')}
                                />
                                <div className="action-content">
                                    <span className="action-icon">📦</span>
                                    <div>
                                        <strong>RMA (Retour fournisseur)</strong>
                                        <p>Créer une fiche RMA pour retour au fournisseur</p>
                                    </div>
                                </div>
                            </label>

                            <label className="action-option">
                                <input
                                    type="radio"
                                    name="action"
                                    value="SCRAP"
                                    checked={selectedAction === 'SCRAP'}
                                    onChange={() => setSelectedAction('SCRAP')}
                                />
                                <div className="action-content">
                                    <span className="action-icon">🗑️</span>
                                    <div>
                                        <strong>Mise au rebut</strong>
                                        <p>Marquer l'équipement comme hors service</p>
                                    </div>
                                </div>
                            </label>
                        </div>
                    </div>

                    <div className="modal-footer">
                        <button type="button" onClick={onClose} className="btn btn-secondary">
                            Annuler
                        </button>
                        <button
                            type="submit"
                            className={`btn ${selectedAction ? 'btn-success' : 'btn-primary'}`}
                            disabled={!selectedAction}
                        >
                            Confirmer
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ValidationModal;
