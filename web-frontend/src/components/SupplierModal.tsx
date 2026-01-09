import React, { useState, useEffect } from 'react';
import { Supplier } from '../types/entities';
import './Modal.css';

interface SupplierModalProps {
    supplier: Supplier;
    isOpen: boolean;
    onClose: () => void;
    onSave: (supplier: Supplier) => void;
}

const SupplierModal: React.FC<SupplierModalProps> = ({ supplier, isOpen, onClose, onSave }) => {
    const [formData, setFormData] = useState<Supplier>(supplier);

    useEffect(() => {
        setFormData(supplier);
    }, [supplier]);

    if (!isOpen) return null;

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const { name, value, type } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
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
                    <h2>✏️ Modifier le fournisseur</h2>
                    <button className="modal-close" onClick={onClose}>✕</button>
                </div>

                <form onSubmit={handleSubmit} className="modal-body">
                    <div className="form-grid">
                        <div className="form-field">
                            <label>Nom *</label>
                            <input
                                type="text"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="form-field">
                            <label>Type</label>
                            <input
                                type="text"
                                name="type"
                                value={formData.type || ''}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Email</label>
                            <input
                                type="email"
                                name="email"
                                value={formData.email || ''}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Téléphone</label>
                            <input
                                type="tel"
                                name="phone"
                                value={formData.phone || ''}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field full-width">
                            <label>Adresse</label>
                            <input
                                type="text"
                                name="address"
                                value={formData.address || ''}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Ville</label>
                            <input
                                type="text"
                                name="city"
                                value={formData.city || ''}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Code postal</label>
                            <input
                                type="text"
                                name="postalCode"
                                value={formData.postalCode || ''}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Pays</label>
                            <input
                                type="text"
                                name="country"
                                value={formData.country || ''}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Site web</label>
                            <input
                                type="url"
                                name="website"
                                value={formData.website || ''}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field">
                            <label>Actif</label>
                            <input
                                type="checkbox"
                                name="active"
                                checked={formData.active}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-field full-width">
                            <label>Notes</label>
                            <textarea
                                name="notes"
                                value={formData.notes || ''}
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

export default SupplierModal;
