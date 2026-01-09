import React, { useEffect } from 'react';
import apiService from '../services/api.service';
import StatCard from '../components/StatCard';
import LoadingState from '../components/LoadingState';
import { useApiData } from '../hooks/useApiData';
import { usePageContext } from '../contexts/PageContext';
import { DashboardStats } from '../types/entities';
import './Dashboard.css';

const Dashboard: React.FC = () => {
  console.log('🏠 [DASHBOARD] Composant monté');
  const { setPageTitle } = usePageContext();

  // ✨ Refactorisation : utilisation du hook useApiData
  const { data: stats, loading, error, reload } = useApiData<DashboardStats>(
    () => apiService.getDashboardStats()
  );

  useEffect(() => {
    setPageTitle('🏠 Dashboard');
  }, [setPageTitle]);

  console.log('🏠 [DASHBOARD] État actuel:', { stats, loading, error });

  if (loading) {
    return (
      <div className="page-container">
        <LoadingState message="Chargement du dashboard..." />
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

  if (!stats) {
    console.warn('⚠️ [DASHBOARD] Aucune donnée stats disponible');
    return null;
  }

  console.log('🎨 [DASHBOARD] Rendu avec stats:', stats);
  return (
    <div className="dashboard-page">
      <div className="page-content">
        {/* Section Parc Matériel */}
        <div className="dashboard-section">
          <h3 className="section-title">📦 Equipements</h3>
          <div className="stats-grid">
            <StatCard
              icon="📦"
              title="Total Équipements"
              value={stats?.totalEquipment || 0}
              color="primary"
            />
            <StatCard
              icon="✅"
              title="Disponibles"
              value={stats?.availableEquipment || 0}
              color="success"
            />
            <StatCard
              icon="🔨"
              title="En Utilisation"
              value={stats?.inUseEquipment || 0}
              color="info"
            />
            <StatCard
              icon="🔧"
              title="En Maintenance"
              value={stats?.maintenanceEquipment || 0}
              color="warning"
            />
          </div>
        </div>

        {/* Section SAV */}
        <div className="dashboard-section">
          <h3 className="section-title">🔧 SAV</h3>
          <div className="stats-grid">
            <StatCard
              icon="📋"
              title="Demandes Ouvertes"
              value={stats?.openServiceRequests || 0}
              subtitle="Demandes d'intervention"
              color="primary"
            />
            <StatCard
              icon="🔧"
              title="Réparations En Cours"
              value={stats?.pendingRepairs || 0}
              subtitle="À traiter"
              color="warning"
            />
            <StatCard
              icon="↩️"
              title="RMA Actifs"
              value={stats?.activeRMAs || 0}
              subtitle="Retours fournisseurs"
              color="info"
            />
          </div>
        </div>

        {/* Section Projets & Contrats */}
        <div className="dashboard-section">
          <h3 className="section-title">💼 Projets & Contrats</h3>
          <div className="stats-grid">
            <StatCard
              icon="🎯"
              title="Projets Actifs"
              value={stats?.activeProjects || 0}
              color="primary"
            />
            <StatCard
              icon="📄"
              title="Contrats Actifs"
              value={stats?.activeContracts || 0}
              color="success"
            />
            <StatCard
              icon="📝"
              title="Demandes Matériel"
              value={stats?.pendingMaterialRequests || 0}
              subtitle="En attente"
              color="warning"
            />
          </div>
        </div>

        {/* Section Ressources */}
        <div className="dashboard-section">
          <h3 className="section-title">🚀 Ressources</h3>
          <div className="stats-grid">
            <StatCard
              icon="🚐"
              title="Total Véhicules"
              value={stats?.totalVehicles || 0}
              subtitle={`${stats?.availableVehicles || 0} disponibles`}
              color="primary"
            />
            <StatCard
              icon="👥"
              title="Total Personnel"
              value={stats?.totalPersonnel || 0}
              subtitle={`${stats?.activePersonnel || 0} actifs`}
              color="success"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;