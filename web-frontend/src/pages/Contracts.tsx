import React, { useState, useEffect } from 'react';
import apiService from '../services/api.service';
import { Contract } from '../types/entities';
import DataTable from '../components/DataTable';
import LoadingState from '../components/LoadingState';
import ContractDetail from '../components/ContractDetail';
import { useApiData } from '../hooks/useApiData';
import { usePageContext } from '../contexts/PageContext';
import './Contracts.css';

const Contracts: React.FC = () => {
  const { setPageTitle } = usePageContext();

  // ✨ Refactorisation : utilisation du hook useApiData
  const { data: contracts, loading, error, reload } = useApiData<Contract[]>(
    () => apiService.getContracts()
  );

  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('Tous');
  const [statusFilter, setStatusFilter] = useState('Tous');
  const [selectedContract, setSelectedContract] = useState<Contract | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);

  useEffect(() => {
    setPageTitle('📜 Contrats');
    return () => {
      setPageTitle('');
    };
  }, [setPageTitle]);

  if (loading) {
    return (
      <div className="page-container">
        <LoadingState message="Chargement des contrats..." />
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

  const filteredContracts = (contracts || []).filter(contract => {
    const matchesSearch = searchTerm === '' ||
      contract.contractNumber?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'Tous' || contract.status === statusFilter;
    const matchesType = typeFilter === 'Tous' || contract.type === typeFilter;

    return matchesSearch && matchesStatus && matchesType;
  });

  const getStatusBadge = (status: string) => {
    const statusMap: { [key: string]: { label: string; className: string } } = {
      'DRAFT': { label: 'Brouillon', className: 'status-badge status-draft' },
      'ACTIVE': { label: 'Actif', className: 'status-badge status-active' },
      'SUSPENDED': { label: 'Suspendu', className: 'status-badge status-suspended' },
      'EXPIRED': { label: 'Expiré', className: 'status-badge status-expired' },
      'TERMINATED': { label: 'Résilié', className: 'status-badge status-terminated' }
    };
    return statusMap[status] || { label: status, className: 'status-badge' };
  };

  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('fr-FR');
  };

  const columns = [
    { key: 'contractNumber', label: 'N° Contrat' },
    { key: 'title', label: 'Titre' },
    { key: 'clientName', label: 'Client' },
    {
      key: 'type',
      label: 'Type',
      render: (contract: Contract) => {
        if (!contract || !contract.type) return '-';
        const typeMap: { [key: string]: string } = {
          'MAINTENANCE': 'Maintenance',
          'RENTAL': 'Location',
          'SERVICE': 'Prestation',
          'SUPPORT': 'Support',
          'SUPPLY': 'Fourniture',
          'MIXED': 'Mixte'
        };
        return typeMap[contract.type] || contract.type;
      }
    },
    {
      key: 'startDate',
      label: 'Début',
      render: (contract: Contract) => contract ? formatDate(contract.startDate) : '-'
    },
    {
      key: 'endDate',
      label: 'Fin',
      render: (contract: Contract) => contract ? formatDate(contract.endDate) : '-'
    },
    {
      key: 'status',
      label: 'Statut',
      render: (contract: Contract) => {
        if (!contract) return '-';
        const info = getStatusBadge(contract.status);
        return <span className={info.className}>{info.label}</span>;
      }
    }
  ];

  return (
    <div className="contracts-page">
      <div className="filters-bar">
        <div className="filter-group">
          <label>🔍</label>
          <input
            type="text"
            placeholder="Numéro, titre, client..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        <div className="filter-group">
          <label>📋 Type</label>
          <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)} className="filter-select">
            <option value="Tous">Tous</option>
            <option value="MAINTENANCE">Maintenance</option>
            <option value="RENTAL">Location</option>
            <option value="SERVICE">Prestation</option>
            <option value="SUPPORT">Support</option>
            <option value="SUPPLY">Fourniture</option>
            <option value="MIXED">Mixte</option>
          </select>
        </div>

        <div className="filter-group">
          <label>📊 Statut</label>
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="filter-select">
            <option value="Tous">Tous</option>
            <option value="DRAFT">Brouillon</option>
            <option value="ACTIVE">Actif</option>
            <option value="SUSPENDED">Suspendu</option>
            <option value="EXPIRED">Expiré</option>
            <option value="TERMINATED">Résilié</option>
          </select>
        </div>

        <div className="header-actions">
          <button className="btn btn-primary" onClick={() => console.log('New contract')}>
            ➕ Nouveau Contrat
          </button>
        </div>
      </div>

      <div className="page-content">
        {error && <div className="error-message">❌ {error}</div>}

        <DataTable
          columns={columns}
          data={filteredContracts}
          loading={loading}
          emptyMessage="Aucun contrat trouvé"
          selectedItem={selectedContract}
          onRowClick={(contract) => {
            if (selectedContract?.id === contract.id) {
              setSelectedContract(null);
            } else {
              setSelectedContract(contract);
              setIsDetailOpen(true);
            }
          }}
        />
      </div>

      <ContractDetail
        contract={selectedContract}
        isOpen={isDetailOpen}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedContract(null);
        }}
      />
    </div>
  );
};

export default Contracts;