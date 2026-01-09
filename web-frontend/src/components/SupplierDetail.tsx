import React from 'react';
import DetailDrawer from './DetailDrawer';

interface Supplier {
    id: number;
    name: string;
    code?: string;
    email?: string;
    phone?: string;
    address?: string;
    city?: string;
    postalCode?: string;
    country?: string;
    contactPerson?: string;
    website?: string;
    notes?: string;
    active: boolean;
}

interface SupplierDetailProps {
    supplier: Supplier | null;
    isOpen: boolean;
    onClose: () => void;
    onEdit?: () => void;
}

const SupplierDetail: React.FC<SupplierDetailProps> = ({ supplier, isOpen, onClose, onEdit }) => {
    return (
        <DetailDrawer
            isOpen={isOpen}
            onClose={onClose}
            title={supplier?.name || 'Fournisseur'}
            width="700px"
            itemId={supplier?.id}
            onEdit={onEdit}
        >
            {!supplier ? null : (
                <>
                    <div className="detail-section">
                        <h3>📋 Informations générales</h3>
                        <div className="detail-grid">
                            <div className="detail-item">
                                <span className="detail-label">Nom</span>
                                <span className="detail-value">{supplier.name}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Code</span>
                                <span className="detail-value">{supplier.code || '-'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Statut</span>
                                <span className={`status-badge ${supplier.active ? 'status-active' : 'status-inactive'}`}>
                                    {supplier.active ? 'Actif' : 'Inactif'}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="detail-section">
                        <h3>📞 Contact</h3>
                        <div className="detail-grid">
                            <div className="detail-item">
                                <span className="detail-label">Email</span>
                                <span className="detail-value">{supplier.email || '-'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Téléphone</span>
                                <span className="detail-value">{supplier.phone || '-'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Personne contact</span>
                                <span className="detail-value">{supplier.contactPerson || '-'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Site web</span>
                                <span className="detail-value">{supplier.website || '-'}</span>
                            </div>
                        </div>
                    </div>

                    <div className="detail-section">
                        <h3>📍 Adresse</h3>
                        <div className="detail-grid">
                            <div className="detail-item">
                                <span className="detail-label">Adresse</span>
                                <span className="detail-value">{supplier.address || '-'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Ville</span>
                                <span className="detail-value">{supplier.city || '-'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Code postal</span>
                                <span className="detail-value">{supplier.postalCode || '-'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Pays</span>
                                <span className="detail-value">{supplier.country || '-'}</span>
                            </div>
                        </div>
                    </div>

                    {supplier.notes && (
                        <div className="detail-section">
                            <h3>📝 Notes</h3>
                            <p className="detail-notes">{supplier.notes}</p>
                        </div>
                    )}
                </>
            )}
        </DetailDrawer>
    );
};

export default SupplierDetail;
