import React, { useState, useEffect } from 'react';
import { Equipment } from '../types';
import './Modal.css';

interface EquipmentModalProps {
    equipment: Equipment;
    isOpen: boolean;
    onClose: () => void;
    onSave: (equipment: Equipment) => void;
}

const EquipmentModal: React.FC<EquipmentModalProps> = ({ equipment, isOpen, onClose, onSave }) => {
    const [formData, setFormData] = useState<Equipment>(equipment);

    useEffect(() => {
        setFormData(equipment);
    }, [equipment]);

    if (!isOpen) return null;

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSave(formData);
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content large" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>✏️ Modifier l'équipement</h2>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="modal-body">
                        <div className="form-grid">
                            <div className="form-group">
                                <label>Nom *</label>
                                <input
                                    type="text"
                                    name="name"
                                    value={formData.name || ''}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Référence interne</label>
                                <input
                                    type="text"
                                    name="internalReference"
                                    value={formData.internalReference || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Marque</label>
                                <input
                                    type="text"
                                    name="brand"
                                    value={formData.brand || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Modèle</label>
                                <input
                                    type="text"
                                    name="model"
                                    value={formData.model || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Numéro de série</label>
                                <input
                                    type="text"
                                    name="serialNumber"
                                    value={formData.serialNumber || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Statut</label>
                                <select
                                    name="status"
                                    value={formData.status || 'AVAILABLE'}
                                    onChange={handleChange}
                                >
                                    <option value="AVAILABLE">Disponible</option>
                                    <option value="IN_USE">En utilisation</option>
                                    <option value="IN_MAINTENANCE">En maintenance</option>
                                    <option value="OUT_OF_SERVICE">Hors service</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Famille</label>
                                <input
                                    type="text"
                                    name="category"
                                    value={formData.category || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Catégorie</label>
                                <input
                                    type="text"
                                    name="subCategory"
                                    value={formData.subCategory || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Type</label>
                                <input
                                    type="text"
                                    name="specificCategory"
                                    value={formData.specificCategory || ''}
                                    onChange={handleChange}
                                />
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

                            <div className="form-group full-width">
                                <label>Remarques</label>
                                <textarea
                                    name="notes"
                                    value={formData.notes || ''}
                                    onChange={handleChange}
                                    rows={3}
                                />
                            </div>
                        </div>
                    </div>

                    <div className="modal-footer">
                        <button type="button" className="btn-secondary" onClick={onClose}>
                            Annuler
                        </button>
                        <button type="submit" className="btn-primary">
                            💾 Enregistrer
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EquipmentModal;
