import React, { useEffect, useState } from 'react';
import apiService from '../services/api.service';
import { Equipment as EquipmentType } from '../types';
import DataTable from '../components/DataTable';
import StatCard from '../components/StatCard';
import EquipmentDetail from '../components/EquipmentDetail';
import { usePageContext } from '../contexts/PageContext';
import './Equipment.css';

const Equipment: React.FC = () => {
  const { setPageTitle } = usePageContext();

  const [equipment, setEquipment] = useState<EquipmentType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [searchTerm, setSearchTerm] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('Tous');
  const [statusFilter, setStatusFilter] = useState('Tous');
  const [ownerFilter, setOwnerFilter] = useState('Tous');

  const [selectedEquipment, setSelectedEquipment] = useState<EquipmentType | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);

  const [stats, setStats] = useState({
    total: 0,
    available: 0,
    inUse: 0,
    maintenance: 0
  });

  // Configuration du header dynamique
  useEffect(() => {
    setPageTitle('📦 Parc Matériel');
    return () => {
      setPageTitle('');
    };
  }, [setPageTitle]);

  useEffect(() => {
    loadEquipmentData();
  }, []);

  const loadEquipmentData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [equipmentData, statsData] = await Promise.all([
        apiService.getEquipment(),
        apiService.getEquipmentStats()
      ]);

      setEquipment(equipmentData);
      setStats(statsData);
    } catch (err) {
      console.error('Erreur chargement équipements:', err);
      setError('Impossible de charger les équipements');
    } finally {
      setLoading(false);
    }
  };

  const filteredEquipment = (equipment || []).filter(item => {
    const matchesSearch = searchTerm === '' ||
      item.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.internalReference?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.serialNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.brand?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.model?.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesCategory = categoryFilter === 'Tous' || item.category === categoryFilter;
    const matchesStatus = statusFilter === 'Tous' || item.status === statusFilter;
    const matchesOwner = ownerFilter === 'Tous';

    return matchesSearch && matchesCategory && matchesStatus && matchesOwner;
  });

  const getStatusBadge = (status: string) => {
    const statusMap: { [key: string]: { label: string; className: string } } = {
      'AVAILABLE': { label: 'Disponible', className: 'status-badge status-available' },
      'IN_USE': { label: 'En utilisation', className: 'status-badge status-in-use' },
      'MAINTENANCE': { label: 'Maintenance', className: 'status-badge status-maintenance' },
      'RESERVED': { label: 'Réservé', className: 'status-badge status-reserved' },
      'OUT_OF_ORDER': { label: 'Hors service', className: 'status-badge status-out-of-order' },
      'RETIRED': { label: 'Retiré', className: 'status-badge status-retired' }
    };

    const statusInfo = statusMap[status] || { label: status, className: 'status-badge' };
    return <span className={statusInfo.className}>{statusInfo.label}</span>;
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
      key: 'purchasePrice',
      label: 'Prix',
      render: (value: any, item: EquipmentType) => {
        if (!item || !item.purchasePrice) return '-';
        return `${item.purchasePrice.toFixed(2)}€`;
      }
    },
    {
      key: 'insuranceValue',
      label: 'Valeur',
      render: (value: any, item: EquipmentType) => {
        if (!item || !item.insuranceValue) return '-';
        return `${item.insuranceValue.toFixed(2)}€`;
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

  const categories = ['Tous', ...Array.from(new Set(equipment.map(e => e.category).filter(Boolean)))];
  const owners = ['Tous'];

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

        <div className="filter-group">
          <label>📂 Catégorie</label>
          <select value={categoryFilter} onChange={(e) => setCategoryFilter(e.target.value)} className="filter-select">
            {categories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
          </select>
        </div>

        <div className="filter-group">
          <label>📊 Statut</label>
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="filter-select">
            <option value="Tous">Tous</option>
            <option value="AVAILABLE">Disponible</option>
            <option value="IN_USE">En utilisation</option>
            <option value="MAINTENANCE">Maintenance</option>
            <option value="RESERVED">Réservé</option>
            <option value="OUT_OF_ORDER">Hors service</option>
            <option value="RETIRED">Retiré</option>
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
            } else {
              setSelectedEquipment(equipment);
              setIsDetailOpen(true);
            }
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
    </div>
  );
};

export default Equipment;