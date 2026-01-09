import React, { useState, useEffect } from 'react';
import './Modal.css';

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

interface SalesInstallationModalProps {
    project: Project | null;
    isOpen: boolean;
    onClose: () => void;
    onSave: (project: Partial<Project>) => void;
}

const SalesInstallationModal: React.FC<SalesInstallationModalProps> = ({ project, isOpen, onClose, onSave }) => {

    if (!isOpen) return null;

    const [formData, setFormData] = useState<Partial<Project>>({
        name: '',
        code: '',
        clientId: undefined,
        status: 'PLANNING',
        startDate: '',
        endDate: '',
        totalAmount: undefined,
        description: ''
    });

    useEffect(() => {
        if (project) {
            setFormData({
                name: project.name || '',
                code: project.code || '',
                clientId: project.clientId,
                status: project.status || 'PLANNING',
                startDate: project.startDate || '',
                endDate: project.endDate || '',
                totalAmount: project.totalAmount,
                description: project.description || ''
            });
        }
    }, [project]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: name === 'totalAmount' || name === 'clientId' ? (value ? Number(value) : undefined) : value
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
                    <h2>{project ? '✏️ Modifier le projet' : '➕ Nouveau projet'}</h2>
                    <button className="modal-close" onClick={onClose}>✕</button>
                </div>

                <form onSubmit={handleSubmit} className="modal-body">
                    <div className="form-grid">
                        <div className="form-field">
                            <label>Nom du projet *</label>
                            <input
                                type="text"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="form-field">
                            <label>Code</label>
                            <input
                                type="text"
                                name="code"
                                value={formData.code}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>ID Client</label>
                            <input
                                type="number"
                                name="clientId"
                                value={formData.clientId || ''}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Statut</label>
                            <select name="status" value={formData.status} onChange={handleChange}>
                                <option value="PLANNING">Planification</option>
                                <option value="IN_PROGRESS">En cours</option>
                                <option value="ON_HOLD">En attente</option>
                                <option value="COMPLETED">Terminé</option>
                                <option value="CANCELLED">Annulé</option>
                            </select>
                        </div>

                        <div className="form-field">
                            <label>Date de début</label>
                            <input
                                type="date"
                                name="startDate"
                                value={formData.startDate}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Date de fin</label>
                            <input
                                type="date"
                                name="endDate"
                                value={formData.endDate}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Montant total</label>
                            <input
                                type="number"
                                name="totalAmount"
                                value={formData.totalAmount || ''}
                                onChange={handleChange}
                                step="0.01"
                            />
                        </div>

                        <div className="form-field full-width">
                            <label>Description</label>
                            <textarea
                                name="description"
                                value={formData.description}
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

export default SalesInstallationModal;
