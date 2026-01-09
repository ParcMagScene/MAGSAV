import React, { useState, useEffect } from 'react';
import { Contract } from '../types/entities';
import './Modal.css';

interface ContractModalProps {
    contract: Contract;
    isOpen: boolean;
    onClose: () => void;
    onSave: (contract: Contract) => void;
}

const ContractModal: React.FC<ContractModalProps> = ({ contract, isOpen, onClose, onSave }) => {
    const [formData, setFormData] = useState<Contract>(contract);

    useEffect(() => {
        setFormData(contract);
    }, [contract]);

    if (!isOpen) return null;

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
        const { name, value, type } = e.target;

        if (type === 'checkbox') {
            const checked = (e.target as HTMLInputElement).checked;
            setFormData(prev => ({ ...prev, [name]: checked }));
        } else {
            setFormData(prev => ({ ...prev, [name]: value }));
        }
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSave(formData);
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content large" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>✏️ Modifier le contrat</h2>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="modal-body">
                        <div className="form-grid">
                            <div className="form-group">
                                <label>Numéro de contrat *</label>
                                <input
                                    type="text"
                                    name="contractNumber"
                                    value={formData.contractNumber || ''}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Type *</label>
                                <select
                                    name="type"
                                    value={formData.type}
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="MAINTENANCE">Maintenance</option>
                                    <option value="LOCATION">Location</option>
                                    <option value="SERVICE">Service</option>
                                    <option value="SUPPORT">Support</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Statut *</label>
                                <select
                                    name="status"
                                    value={formData.status}
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="DRAFT">Brouillon</option>
                                    <option value="ACTIVE">Actif</option>
                                    <option value="SUSPENDED">Suspendu</option>
                                    <option value="EXPIRED">Expiré</option>
                                    <option value="TERMINATED">Terminé</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Date de début *</label>
                                <input
                                    type="date"
                                    name="startDate"
                                    value={formData.startDate || ''}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Date de fin</label>
                                <input
                                    type="date"
                                    name="endDate"
                                    value={formData.endDate || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Montant</label>
                                <input
                                    type="number"
                                    name="amount"
                                    value={formData.amount || ''}
                                    onChange={handleChange}
                                    step="0.01"
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    <input
                                        type="checkbox"
                                        name="active"
                                        checked={formData.active}
                                        onChange={handleChange}
                                    />
                                    {' '}Contrat actif
                                </label>
                            </div>

                            <div className="form-group full-width">
                                <label>Description</label>
                                <textarea
                                    name="description"
                                    value={formData.description || ''}
                                    onChange={handleChange}
                                    rows={3}
                                />
                            </div>
                        </div>
                    </div>

                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" onClick={onClose}>
                            Annuler
                        </button>
                        <button type="submit" className="btn btn-primary">
                            💾 Enregistrer
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ContractModal;
