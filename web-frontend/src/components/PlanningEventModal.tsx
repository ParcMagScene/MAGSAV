import React, { useState, useEffect } from 'react';
import './Modal.css';

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

interface PlanningEventModalProps {
    event: PlanningEvent;
    isOpen: boolean;
    onClose: () => void;
    onSave: (event: PlanningEvent) => void;
}

const PlanningEventModal: React.FC<PlanningEventModalProps> = ({ event, isOpen, onClose, onSave }) => {
    const [formData, setFormData] = useState<PlanningEvent>(event);

    useEffect(() => {
        setFormData(event);
    }, [event]);

    if (!isOpen) return null;

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: name === 'resourceId' || name === 'projectId' ? (value ? Number(value) : undefined) : value
        }));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSave(formData);
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>✏️ Modifier l'événement</h2>
                    <button className="modal-close" onClick={onClose}>✕</button>
                </div>

                <form onSubmit={handleSubmit} className="modal-body">
                    <div className="form-grid">
                        <div className="form-field">
                            <label>Type *</label>
                            <select name="type" value={formData.type} onChange={handleChange} required>
                                <option value="PERSONNEL">👤 Personnel</option>
                                <option value="VEHICLE">🚐 Véhicule</option>
                            </select>
                        </div>

                        <div className="form-field">
                            <label>ID Ressource *</label>
                            <input
                                type="number"
                                name="resourceId"
                                value={formData.resourceId}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="form-field">
                            <label>Nom de la ressource</label>
                            <input
                                type="text"
                                name="resourceName"
                                value={formData.resourceName}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>ID Projet</label>
                            <input
                                type="number"
                                name="projectId"
                                value={formData.projectId || ''}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Nom du projet</label>
                            <input
                                type="text"
                                name="projectName"
                                value={formData.projectName || ''}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Statut</label>
                            <select name="status" value={formData.status} onChange={handleChange}>
                                <option value="PENDING">En attente</option>
                                <option value="CONFIRMED">Confirmé</option>
                                <option value="ACTIVE">Actif</option>
                                <option value="COMPLETED">Terminé</option>
                                <option value="CANCELLED">Annulé</option>
                            </select>
                        </div>

                        <div className="form-field">
                            <label>Date de début *</label>
                            <input
                                type="datetime-local"
                                name="startDate"
                                value={formData.startDate.slice(0, 16)}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="form-field">
                            <label>Date de fin *</label>
                            <input
                                type="datetime-local"
                                name="endDate"
                                value={formData.endDate.slice(0, 16)}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="form-field full-width">
                            <label>Description</label>
                            <textarea
                                name="description"
                                value={formData.description || ''}
                                onChange={handleChange}
                                rows={4}
                            />
                        </div>
                    </div>

                    <div className="modal-footer">
                        <button type="button" onClick={onClose} className="btn btn-secondary">
                            Annuler
                        </button>
                        <button type="submit" className="btn btn-primary">
                            Enregistrer
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default PlanningEventModal;
