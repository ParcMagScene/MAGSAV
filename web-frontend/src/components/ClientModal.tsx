import React, { useState, useEffect } from 'react';
import { Client } from '../types/entities';
import './Modal.css';

interface ClientModalProps {
    client: Client;
    isOpen: boolean;
    onClose: () => void;
    onSave: (client: Client) => void;
}

const ClientModal: React.FC<ClientModalProps> = ({ client, isOpen, onClose, onSave }) => {
    const [formData, setFormData] = useState<Client>(client);

    useEffect(() => {
        setFormData(client);
    }, [client]);

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
                    <h2>✏️ Modifier le client</h2>
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
                                <label>Type *</label>
                                <select
                                    name="type"
                                    value={formData.type}
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="PARTICULIER">Particulier</option>
                                    <option value="PROFESSIONNEL">Professionnel</option>
                                    <option value="ENTREPRISE">Entreprise</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Email</label>
                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Téléphone</label>
                                <input
                                    type="tel"
                                    name="phone"
                                    value={formData.phone || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Adresse</label>
                                <input
                                    type="text"
                                    name="address"
                                    value={formData.address || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Ville</label>
                                <input
                                    type="text"
                                    name="city"
                                    value={formData.city || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Code postal</label>
                                <input
                                    type="text"
                                    name="postalCode"
                                    value={formData.postalCode || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Pays</label>
                                <input
                                    type="text"
                                    name="country"
                                    value={formData.country || ''}
                                    onChange={handleChange}
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
                                    {' '}Client actif
                                </label>
                            </div>

                            <div className="form-group full-width">
                                <label>Notes</label>
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

export default ClientModal;
