import React, { useState, useEffect } from 'react';
import apiService from '../services/api.service';
import { Client } from '../types/entities';
import DataTable from '../components/DataTable';
import LoadingState from '../components/LoadingState';
import ClientDetail from '../components/ClientDetail';
import ClientModal from '../components/ClientModal';
import { useApiData } from '../hooks/useApiData';
import { usePageContext } from '../contexts/PageContext';
import './Clients.css';

const Clients: React.FC = () => {
  const { setPageTitle } = usePageContext();

  // ✨ Refactorisation : utilisation du hook useApiData
  const { data: clients, loading, error, reload } = useApiData<Client[]>(
    () => apiService.getClients()
  );

  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('Tous');
  const [statusFilter, setStatusFilter] = useState('Tous');
  const [selectedClient, setSelectedClient] = useState<Client | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    setPageTitle('👥 Clients');
    return () => {
      setPageTitle('');
    };
  }, [setPageTitle]);

  if (loading) {
    return (
      <div className="page-container">
        <LoadingState message="Chargement des clients..." />
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

  const filteredClients = (clients || []).filter(client => {
    const matchesSearch = searchTerm === '' ||
      client.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      client.email?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'Tous' ||
      (statusFilter === 'Actifs' && client.active) ||
      (statusFilter === 'Inactifs' && !client.active);

    return matchesSearch && matchesStatus;
  });

  const columns = [
    { key: 'name', label: 'Nom' },
    {
      key: 'type',
      label: 'Type',
      render: (client: Client) => {
        if (!client || !client.type) return '-'; const typeMap: { [key: string]: string } = {
          'COMPANY': '🏢 Entreprise',
          'ADMINISTRATION': '🏛️ Administration',
          'ASSOCIATION': '🤝 Association',
          'INDIVIDUAL': '👤 Particulier'
        };
        return typeMap[client.type] || client.type;
      }
    },
    { key: 'email', label: 'Email' },
    { key: 'phone', label: 'Téléphone' },
    { key: 'city', label: 'Ville' },
    {
      key: 'active',
      label: 'Statut',
      render: (client: Client) => {
        if (!client) return '-';
        return (
          <span className={`status-badge ${client.active ? 'status-active' : 'status-inactive'}`}>
            {client.active ? 'Actif' : 'Inactif'}
          </span>
        );
      }
    }
  ];

  return (
    <div className="clients-page">
      <div className="filters-bar">
        <div className="filter-group">
          <label>🔍</label>
          <input
            type="text"
            placeholder="Nom, email, N° TVA..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        <div className="filter-group">
          <label>🏢 Type</label>
          <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)} className="filter-select">
            <option value="Tous">Tous</option>
            <option value="COMPANY">Entreprise</option>
            <option value="ADMINISTRATION">Administration</option>
            <option value="ASSOCIATION">Association</option>
            <option value="INDIVIDUAL">Particulier</option>
          </select>
        </div>

        <div className="filter-group">
          <label>📊 Statut</label>
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="filter-select">
            <option value="Tous">Tous</option>
            <option value="Actifs">Actifs</option>
            <option value="Inactifs">Inactifs</option>
          </select>
        </div>

        <div className="header-actions">
          <button className="btn btn-secondary" onClick={() => console.log('Export')}>
            📄 Exporter
          </button>
          <button className="btn btn-primary" onClick={() => console.log('New client')}>
            ➕ Nouveau Client
          </button>
        </div>
      </div>

      <div className="page-content">
        {error && <div className="error-message">❌ {error}</div>}

        <DataTable
          columns={columns}
          data={filteredClients}
          loading={loading}
          emptyMessage="Aucun client trouvé"
          selectedItem={selectedClient}
          onRowClick={(client) => {
            if (selectedClient?.id === client.id) {
              setSelectedClient(null);
              setIsDetailOpen(false);
            } else {
              setSelectedClient(client);
              setIsDetailOpen(true);
            }
          }}
          onEdit={(client) => {
            setSelectedClient(client);
            setIsModalOpen(true);
          }}
        />
      </div>

      <ClientDetail
        client={selectedClient}
        isOpen={isDetailOpen}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedClient(null);
        }}
        onEdit={() => {
          setIsModalOpen(true);
        }}
      />

      {selectedClient && isModalOpen && (
        <ClientModal
          client={selectedClient}
          isOpen={isModalOpen}
          onClose={() => {
            setIsModalOpen(false);
          }}
          onSave={async (updatedClient) => {
            try {
              await apiService.put(`/api/clients/${updatedClient.id}`, updatedClient);
              setIsModalOpen(false);
              reload();
            } catch (error) {
              console.error('Erreur lors de la mise à jour:', error);
              alert('Erreur lors de la mise à jour du client');
            }
          }}
        />
      )}
    </div>
  );
};

export default Clients;