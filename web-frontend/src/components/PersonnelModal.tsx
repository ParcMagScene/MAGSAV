import React, { useState, useEffect } from 'react';
import { Personnel } from '../types/entities';
import './Modal.css';

interface PersonnelModalProps {
    personnel: Personnel;
    isOpen: boolean;
    onClose: () => void;
    onSave: (personnel: Personnel) => void;
}

const PersonnelModal: React.FC<PersonnelModalProps> = ({ personnel, isOpen, onClose, onSave }) => {
    const [formData, setFormData] = useState<Personnel>(personnel);

    useEffect(() => {
        setFormData(personnel);
    }, [personnel]);

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
                    <h2>✏️ Modifier le personnel</h2>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="modal-body">
                        <div className="form-grid">
                            <div className="form-group">
                                <label>Prénom *</label>
                                <input
                                    type="text"
                                    name="firstName"
                                    value={formData.firstName || ''}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Nom *</label>
                                <input
                                    type="text"
                                    name="lastName"
                                    value={formData.lastName || ''}
                                    onChange={handleChange}
                                    required
                                />
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
                                <label>Type *</label>
                                <select
                                    name="type"
                                    value={formData.type}
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="PERMANENT">Permanent</option>
                                    <option value="INTERMITTENT">Intermittent</option>
                                    <option value="FREELANCE">Freelance</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Poste</label>
                                <input
                                    type="text"
                                    name="position"
                                    value={formData.position || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Date d'embauche</label>
                                <input
                                    type="date"
                                    name="hireDate"
                                    value={formData.hireDate || ''}
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
                                    {' '}Actif
                                </label>
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

export default PersonnelModal;
