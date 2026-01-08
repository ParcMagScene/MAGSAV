import React, { useState, useEffect } from 'react';
import apiService from '../services/api.service';
import DataTable from '../components/DataTable';
import LoadingState from '../components/LoadingState';
import SupplierDetail from '../components/SupplierDetail';
import { useApiData } from '../hooks/useApiData';
import { usePageContext } from '../contexts/PageContext';
import './Suppliers.css';

interface Supplier {
    id: number;
    name: string;
    code?: string;
    email?: string;
    phone?: string;
    city?: string;
    contactPerson?: string;
    active: boolean;
}

const Suppliers: React.FC = () => {
    const { setPageTitle } = usePageContext();

    // ✨ Refactorisation : utilisation du hook useApiData
    const [filterActive, setFilterActive] = useState<boolean | null>(null);
    const [selectedSupplier, setSelectedSupplier] = useState<Supplier | null>(null);
    const [isDetailOpen, setIsDetailOpen] = useState(false);

    const { data: suppliers, loading, error, reload } = useApiData<Supplier[]>(
        () => filterActive === true
            ? apiService.getActiveSuppliers()
            : apiService.getSuppliers(),
        [filterActive] // Re-charger quand le filtre change
    );

    useEffect(() => {
        setPageTitle('🏪 Fournisseurs');
        return () => {
            setPageTitle('');
        };
    }, [setPageTitle]);

    if (loading) {
        return (
            <div className="page-container">
                <LoadingState message="Chargement des fournisseurs..." />
            </div>
        );
    }

    if (error) {
        return (
            <div className="page-container">
                <div className="error-container">
                    <h2>❌ Erreur</h2>
                    <p>{error.message}</p>
                    <button onClick={reload} className="btn-retry">
                        Réessayer
                    </button>
                </div>
            </div>
        );
    }

    const columns = [
        { key: 'code', label: 'Code', width: '100px' },
        { key: 'name', label: 'Nom' },
        { key: 'contactPerson', label: 'Contact' },
        { key: 'phone', label: 'Téléphone' },
        { key: 'email', label: 'Email' },
        { key: 'city', label: 'Ville' },
        {
            key: 'active',
            label: 'Statut',
            render: (value: boolean) => (
                <span className={`status-badge ${value ? 'status-active' : 'status-inactive'}`}>
                    {value ? 'Actif' : 'Inactif'}
                </span>
            ),
            width: '100px',
        },
    ];

    return (
        <div className="suppliers-page">
            <div className="filters-bar">
                <div className="filter-group">
                    <label>Statut</label>
                    <select
                        className="filter-select"
                        value={filterActive === null ? 'all' : filterActive ? 'active' : 'inactive'}
                        onChange={(e) =>
                            setFilterActive(
                                e.target.value === 'all' ? null : e.target.value === 'active'
                            )
                        }
                    >
                        <option value="all">Tous</option>
                        <option value="active">Actifs</option>
                        <option value="inactive">Inactifs</option>
                    </select>
                </div>

                <div className="header-actions">
                    <button className="btn btn-primary" onClick={() => console.log('New supplier')}>
                        ➕ Nouveau Fournisseur
                    </button>
                </div>
            </div>

            <div className="page-content">
                <DataTable
                    data={suppliers || []}
                    columns={columns}
                    loading={loading}
                    emptyMessage="Aucun fournisseur trouvé"
                    selectedItem={selectedSupplier}
                    onRowClick={(supplier) => {
                        if (selectedSupplier?.id === supplier.id) {
                            setSelectedSupplier(null);
                        } else {
                            setSelectedSupplier(supplier);
                            setIsDetailOpen(true);
                        }
                    }}
                />
            </div>

            <SupplierDetail
                supplier={selectedSupplier}
                isOpen={isDetailOpen}
                onClose={() => {
                    setIsDetailOpen(false);
                    setSelectedSupplier(null);
                }}
            />
        </div>
    );
};

export default Suppliers;
