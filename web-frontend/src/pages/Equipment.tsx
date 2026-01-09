import React, { useEffect, useState } from 'react';
import apiService from '../services/api.service';
import configService from '../services/config.service';
import { Equipment as EquipmentType } from '../types';
import DataTable from '../components/DataTable';
import StatCard from '../components/StatCard';
import EquipmentDetail from '../components/EquipmentDetail';
import EquipmentModal from '../components/EquipmentModal';
import ProgressiveHierarchyFilter from '../components/ProgressiveHierarchyFilter';
import ContextMenu from '../components/ContextMenu';
import { usePageContext } from '../contexts/PageContext';
import { useEquipment } from '../contexts/EquipmentContext';
import { translateStatus, formatFrenchDate } from '../utils/translations';
import './Equipment.css';

const Equipment: React.FC = () => {
  const { setPageTitle } = usePageContext();
  const { equipment, stats, loading, error } = useEquipment();

  const [searchTerm, setSearchTerm] = useState('');
  const [familleFilter, setFamilleFilter] = useState('Tous');
  const [categorieFilter, setCategorieFilter] = useState('Tous');
  const [typeFilter, setTypeFilter] = useState('Tous');
  const [statusFilter, setStatusFilter] = useState('Tous');

  const [selectedEquipment, setSelectedEquipment] = useState<EquipmentType | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [contextMenu, setContextMenu] = useState<{ x: number; y: number; item: EquipmentType } | null>(null);

  // Configuration du header dynamique
  useEffect(() => {
    setPageTitle('📦 Equipements');
    return () => {
      setPageTitle('');
    };
  }, [setPageTitle]);

  const filteredEquipment = (equipment || []).filter(item => {
    const matchesSearch = searchTerm === '' ||
      item.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.internalReference?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.serialNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.brand?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.model?.toLowerCase().includes(searchTerm.toLowerCase());

    // Filtre hiérarchique progressif
    // category = Famille (CSV col 0)
    // subCategory = Catégorie (CSV col 1)
    // specificCategory = Type (CSV col 2)
    const matchesFamille = familleFilter === 'Tous' || item.category === familleFilter;
    const matchesCategorie = categorieFilter === 'Tous' || item.subCategory === categorieFilter;
    const matchesType = typeFilter === 'Tous' || item.specificCategory === typeFilter;

    const matchesStatus = statusFilter === 'Tous' || item.status === statusFilter;

    return matchesSearch && matchesFamille && matchesCategorie && matchesType && matchesStatus;
  });

  const getStatusBadge = (status: string) => {
    const label = translateStatus(status, 'equipment');
    const className = `status-badge status-${status.toLowerCase().replace('_', '-')}`;
    return <span className={className}>{label}</span>;
  };

  const generateUID = (item: EquipmentType): string => {
    // Extraire 3 lettres de la catégorie ou utiliser EQP par défaut
    let prefix = 'EQP';
    if (item.category) {
      // Prendre les 3 premières lettres de la catégorie en majuscules
      prefix = item.category.substring(0, 3).toUpperCase().padEnd(3, 'X');
    }

    // Formater l'ID sur 4 chiffres
    const idStr = String(item.id || 0).padStart(4, '0').slice(-4);

    return `${prefix}${idStr}`;
  };

  const columns = [
    {
      key: 'id',
      label: 'UID',
      render: (value: any, item: EquipmentType) => {
        if (!item) return '-';
        return <span className="equipment-uid">{generateUID(item)}</span>;
      }
    },
    {
      key: 'internalReference',
      label: 'Code Locmat',
      render: (value: any, item: EquipmentType) => {
        if (!item || !item.internalReference) return '-';
        return (
          <div className="equipment-code">
            <span className="code-text">{item.internalReference}</span>
          </div>
        );
      }
    },
    {
      key: 'name',
      label: 'Nom',
      render: (value: any, item: EquipmentType) => {
        if (!item) return '-';
        return <span className="equipment-name">{item.name || '-'}</span>;
      }
    },
    {
      key: 'category',
      label: 'Famille',
      render: (value: any, item: EquipmentType) => {
        if (!item) return '-';
        return <span className="category-badge">{item.category || '-'}</span>;
      }
    },
    {
      key: 'subCategory',
      label: 'Catégorie',
      render: (value: any, item: EquipmentType) => {
        if (!item) return '-';
        return <span className="subcategory-badge">{item.subCategory || '-'}</span>;
      }
    },
    {
      key: 'specificCategory',
      label: 'Type',
      render: (value: any, item: EquipmentType) => {
        if (!item) return '-';
        return <span className="type-badge">{item.specificCategory || '-'}</span>;
      }
    },
    {
      key: 'brand',
      label: 'Marque',
      render: (value: any, item: EquipmentType) => {
        if (!item) return '-';
        return <span className="equipment-brand">{item.brand || '-'}</span>;
      }
    },
    {
      key: 'model',
      label: 'Modèle',
      render: (value: any, item: EquipmentType) => {
        if (!item) return '-';
        return item.model || '-';
      }
    },
    {
      key: 'serialNumber',
      label: 'N° Série',
      render: (value: any, item: EquipmentType) => {
        if (!item) return '-';
        return item.serialNumber || '-';
      }
    },
    {
      key: 'quantityInStock',
      label: 'Qté',
      render: (value: any, item: EquipmentType) => {
        if (!item) return '-';
        return item.quantityInStock !== undefined ? item.quantityInStock : '-';
      }
    },
    {
      key: 'status',
      label: 'Statut',
      render: (value: any, item: EquipmentType) => {
        if (!item) return '-';
        return getStatusBadge(item.status);
      }
    },
    {
      key: 'location',
      label: 'Localisation',
      render: (value: any, item: EquipmentType) => {
        if (!item) return '-';
        return item.location || '-';
      }
    }
  ];

  const statuses = configService.getStatuses();

  return (
    <div className="equipment-page">
      <div className="filters-bar">
        <div className="filter-group">
          <label>🔍</label>
          <input
            type="text"
            placeholder="Code Locmat, nom, marque, modèle, N° série..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        <ProgressiveHierarchyFilter
          equipment={equipment}
          onFilterChange={(famille, categorie, type) => {
            setFamilleFilter(famille);
            setCategorieFilter(categorie);
            setTypeFilter(type);
          }}
        />

        <div className="filter-group">
          <label>📊 Statut</label>
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="filter-select">
            <option value="Tous">Tous</option>
            {statuses.map(status => (
              <option key={status.value} value={status.value}>
                {status.label}
              </option>
            ))}
          </select>
        </div>

        <div className="header-actions">
          <button className="btn btn-secondary" onClick={() => console.log('Export CSV')}>
            📄 Exporter CSV
          </button>
          <button className="btn btn-primary" onClick={() => console.log('New equipment')}>
            ➕ Nouvel Équipement
          </button>
        </div>
      </div>

      <div className="page-content">
        {error && <div className="error-message">❌ {error}</div>}

        <DataTable
          columns={columns}
          data={filteredEquipment}
          loading={loading}
          emptyMessage="Aucun équipement trouvé"
          selectedItem={selectedEquipment}
          onRowClick={(equipment) => {
            if (selectedEquipment?.id === equipment.id) {
              setSelectedEquipment(null);
              setIsDetailOpen(false);
            } else {
              setSelectedEquipment(equipment);
              setIsDetailOpen(true);
            }
            setContextMenu(null);
          }}
          onEdit={(equipment) => {
            setSelectedEquipment(equipment);
            setIsModalOpen(true);
          }}
        />
      </div>

      <EquipmentDetail
        equipment={selectedEquipment}
        isOpen={isDetailOpen}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedEquipment(null);
        }}
      />

      {selectedEquipment && isModalOpen && (
        <EquipmentModal
          equipment={selectedEquipment}
          isOpen={isModalOpen}
          onClose={() => {
            setIsModalOpen(false);
          }}
          onSave={async (updatedEquipment) => {
            try {
              await apiService.put(`/api/equipment/${updatedEquipment.id}`, updatedEquipment);
              setIsModalOpen(false);
              window.location.reload(); // Recharger pour mettre à jour les données
            } catch (error) {
              console.error('Erreur lors de la mise à jour:', error);
              alert('Erreur lors de la mise à jour de l\'équipement');
            }
          }}
        />
      )}
    </div>
  );
};

export default Equipment;