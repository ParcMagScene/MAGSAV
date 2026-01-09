import React, { useState, useEffect } from 'react';
import logger from '../services/logger.service';
import apiService from '../services/api.service';
import { Personnel as PersonnelType } from '../types/entities';
import DataTable from '../components/DataTable';
import LoadingState from '../components/LoadingState';
import PersonnelDetail from '../components/PersonnelDetail';
import PersonnelModal from '../components/PersonnelModal';
import { useApiData } from '../hooks/useApiData';
import { usePageContext } from '../contexts/PageContext';
import './Personnel.css';

const Personnel: React.FC = () => {
  const { setPageTitle } = usePageContext();

  // âœ¨ Refactorisation : utilisation du hook useApiData
  const { data: personnel, loading, error, reload } = useApiData<PersonnelType[]>(
    () => apiService.getPersonnel()
  );

  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('Tous');
  const [statusFilter, setStatusFilter] = useState('Tous');
  const [selectedPersonnel, setSelectedPersonnel] = useState<PersonnelType | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    setPageTitle('ðŸ‘¥ Personnel');
    return () => {
      setPageTitle('');
    };
  }, [setPageTitle]);

  if (loading) {
    return (
      <div className="page-container">
        <LoadingState message="Chargement du personnel..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-container">
        <div className="error-container">
          <h2>âŒ Erreur</h2>
          <p>{error.message}</p>
          <button onClick={reload} className="btn-retry">
            RÃ©essayer
          </button>
        </div>
      </div>
    );
  }

  const filteredPersonnel = (personnel || []).filter(person => {
    const matchesSearch = searchTerm === '' ||
      person.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      person.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      person.email?.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesType = typeFilter === 'Tous' || person.type === typeFilter;
    const matchesStatus = statusFilter === 'Tous' ||
      (statusFilter === 'Actifs' && person.active) ||
      (statusFilter === 'Inactifs' && !person.active);

    return matchesSearch && matchesType && matchesStatus;
  });

  const columns = [
    {
      key: 'name',
      label: 'Nom',
      render: (_value: any, person: PersonnelType) => {
        if (!person) return '-';
        return `${person.firstName || ''} ${person.lastName || ''}`.trim() || '-';
      }
    },
    {
      key: 'type',
      label: 'Type',
      render: (_value: any, person: PersonnelType) => {
        if (!person || !person.type) return '-';
        const typeMap: { [key: string]: string } = {
          'EMPLOYEE': 'ðŸ‘¥ EmployÃ©',
          'FREELANCE': 'ðŸ’¼ Freelance',
          'INTERN': 'ðŸŽ“ Stagiaire',
          'TEMP': 'â±ï¸ IntÃ©rimaire',
          'INTERMITTENT': 'ðŸŽ­ Intermittent'
        };
        return typeMap[person.type] || person.type;
      }
    },
    { key: 'email', label: 'Email' },
    { key: 'phone', label: 'TÃ©lÃ©phone' },
    {
      key: 'qualifications',
      label: 'Qualifications',
      render: (_value: any, person: PersonnelType) => {
        if (!person) return '-';
        const qualifications = person.qualifications || [];
        if (qualifications.length === 0) return '-';
        return (
          <div className="qualifications">
            {qualifications.slice(0, 3).map((qual, idx) => (
              <span key={idx} className="qualification-badge">{qual}</span>
            ))}
            {qualifications.length > 3 && (
              <span className="qualification-more">+{qualifications.length - 3}</span>
            )}
          </div>
        );
      }
    },
    {
      key: 'active',
      label: 'Statut',
      render: (_value: any, person: PersonnelType) => {
        if (!person) return '-';
        return (
          <span className={`status-badge ${person.active ? 'status-active' : 'status-inactive'}`}>
            {person.active ? 'Actif' : 'Inactif'}
          </span>
        );
      }
    }
  ];

  return (
    <div className="personnel-page">
      <div className="filters-bar">
        <div className="filter-group">
          <label>ðŸ”</label>
          <input
            type="text"
            placeholder="Nom, prÃ©nom, email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        <div className="filter-group">
          <label>ðŸ‘¤ Type</label>
          <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)} className="filter-select">
            <option value="Tous">Tous</option>
            <option value="EMPLOYEE">EmployÃ©</option>
            <option value="FREELANCE">Freelance</option>
            <option value="INTERN">Stagiaire</option>
            <option value="TEMP">IntÃ©rimaire</option>
            <option value="INTERMITTENT">Intermittent</option>
          </select>
        </div>

        <div className="filter-group">
          <label>ðŸ“Š Statut</label>
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="filter-select">
            <option value="Tous">Tous</option>
            <option value="Actifs">Actifs</option>
            <option value="Inactifs">Inactifs</option>
          </select>
        </div>

        <div className="header-actions">
          <button className="btn btn-primary" onClick={() => logger.debug('New employee')}>
            âž• Nouvel EmployÃ©
          </button>
        </div>
      </div>

      <div className="page-content">
        {error && <div className="error-message">âŒ {error}</div>}

        <DataTable
          columns={columns}
          data={filteredPersonnel}
          loading={loading}
          emptyMessage="Aucun personnel trouvÃ©"
          selectedItem={selectedPersonnel}
          onRowClick={(personnel) => {
            if (selectedPersonnel?.id === personnel.id) {
              setSelectedPersonnel(null);
              setIsDetailOpen(false);
            } else {
              setSelectedPersonnel(personnel);
              setIsDetailOpen(true);
            }
          }}
          onEdit={(personnel) => {
            setSelectedPersonnel(personnel);
            setIsModalOpen(true);
          }}
        />
      </div>

      <PersonnelDetail
        personnel={selectedPersonnel}
        isOpen={isDetailOpen}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedPersonnel(null);
        }}
        onEdit={() => {
          setIsModalOpen(true);
        }}
      />

      {selectedPersonnel && isModalOpen && (
        <PersonnelModal
          personnel={selectedPersonnel}
          isOpen={isModalOpen}
          onClose={() => {
            setIsModalOpen(false);
          }}
          onSave={async (updatedPersonnel) => {
            try {
              await apiService.put(`/api/personnel/${updatedPersonnel.id}`, updatedPersonnel);
              setIsModalOpen(false);
              reload();
            } catch (error) {
              logger.error('Erreur lors de la mise Ã  jour:', error);
              alert('Erreur lors de la mise Ã  jour du personnel');
            }
          }}
        />
      )}
    </div>
  );
};

export default Personnel;