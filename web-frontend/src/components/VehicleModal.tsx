import React, { useState, useEffect } from 'react';
import { Vehicle } from '../types/entities';
import './Modal.css';

interface VehicleModalProps {
    vehicle: Vehicle;
    isOpen: boolean;
    onClose: () => void;
    onSave: (vehicle: Vehicle) => void;
}

const VehicleModal: React.FC<VehicleModalProps> = ({ vehicle, isOpen, onClose, onSave }) => {
    const [formData, setFormData] = useState<Vehicle>(vehicle);

    useEffect(() => {
        setFormData(vehicle);
    }, [vehicle]);

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
                    <h2>✏️ Modifier le véhicule</h2>
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
                                <label>Immatriculation *</label>
                                <input
                                    type="text"
                                    name="licensePlate"
                                    value={formData.licensePlate || ''}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Marque *</label>
                                <input
                                    type="text"
                                    name="brand"
                                    value={formData.brand || ''}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Modèle *</label>
                                <input
                                    type="text"
                                    name="model"
                                    value={formData.model || ''}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Type</label>
                                <select
                                    name="type"
                                    value={formData.type}
                                    onChange={handleChange}
                                >
                                    <option value="VAN">Van</option>
                                    <option value="VL">VL</option>
                                    <option value="VL_17M3">VL 17m³</option>
                                    <option value="VL_20M3">VL 20m³</option>
                                    <option value="TRUCK">Camion</option>
                                    <option value="PORTEUR">Porteur</option>
                                    <option value="TRACTEUR">Tracteur</option>
                                    <option value="SEMI_REMORQUE">Semi-remorque</option>
                                    <option value="SCENE_MOBILE">Scène mobile</option>
                                    <option value="TRAILER">Remorque</option>
                                    <option value="CAR">Voiture</option>
                                    <option value="MOTORCYCLE">Moto</option>
                                    <option value="OTHER">Autre</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Statut</label>
                                <select
                                    name="status"
                                    value={formData.status}
                                    onChange={handleChange}
                                >
                                    <option value="AVAILABLE">Disponible</option>
                                    <option value="IN_USE">En utilisation</option>
                                    <option value="MAINTENANCE">En maintenance</option>
                                    <option value="OUT_OF_ORDER">Hors service</option>
                                    <option value="RENTED_OUT">Loué</option>
                                    <option value="RESERVED">Réservé</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Couleur</label>
                                <input
                                    type="text"
                                    name="color"
                                    value={formData.color || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Propriétaire</label>
                                <input
                                    type="text"
                                    name="owner"
                                    value={formData.owner || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Année</label>
                                <input
                                    type="number"
                                    name="year"
                                    value={formData.year || ''}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Carburant</label>
                                <select
                                    name="fuelType"
                                    value={formData.fuelType || ''}
                                    onChange={handleChange}
                                >
                                    <option value="">-</option>
                                    <option value="GASOLINE">Essence</option>
                                    <option value="DIESEL">Diesel</option>
                                    <option value="ELECTRIC">Électrique</option>
                                    <option value="HYBRID">Hybride</option>
                                    <option value="GPL">GPL</option>
                                    <option value="OTHER">Autre</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Kilométrage</label>
                                <input
                                    type="number"
                                    name="mileage"
                                    value={formData.mileage || ''}
                                    onChange={handleChange}
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

export default VehicleModal;
