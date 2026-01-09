import React, { useState, useEffect } from 'react';
import logger from '../services/logger.service';
import apiService from '../services/api.service';
import { Project } from '../types';
import DataTable from '../components/DataTable';
import LoadingState from '../components/LoadingState';
import SalesInstallationDetail from '../components/SalesInstallationDetail';
import SalesInstallationModal from '../components/SalesInstallationModal';
import { useApiData } from '../hooks/useApiData';
import { usePageContext } from '../contexts/PageContext';
import './SalesInstallations.css';

const SalesInstallations: React.FC = () => {
  const { setPageTitle } = usePageContext();

  // âœ¨ Refactorisation : utilisation du hook useApiData
  const { data: projects, loading: loadingProj, error: errorProj, reload: reloadProj } =
    useApiData<Project[]>(() => apiService.getProjects());
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    setPageTitle('ðŸ“¦ Ventes & Installations');
  }, [setPageTitle]);

  const loading = loadingProj;
  const error = errorProj;

  if (loading) {
    return (
      <div className="page-container">
        <LoadingState message="Chargement des ventes & installations..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-container">
        <div className="error-container">
          <h2>âŒ Erreur</h2>
          <p>{error.message}</p>
          <button onClick={reloadProj} className="btn-retry">
            RÃ©essayer
          </button>
        </div>
      </div>
    );
  }

  const getStatusBadge = (status: string) => {
    const statusMap: { [key: string]: { label: string; className: string } } = {
      'DRAFT': { label: 'Brouillon', className: 'status-badge status-draft' },
      'ACTIVE': { label: 'Actif', className: 'status-badge status-active' },
      'IN_PROGRESS': { label: 'En cours', className: 'status-badge status-in-progress' },
      'COMPLETED': { label: 'TerminÃ©', className: 'status-badge status-completed' },
      'CANCELLED': { label: 'AnnulÃ©', className: 'status-badge status-cancelled' }
    };
    return statusMap[status] || { label: status, className: 'status-badge' };
  };

  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('fr-FR');
  };

  const projectColumns = [
    { key: 'projectNumber', label: 'NÂ° Projet' },
    {
      key: 'name',
      label: 'Titre',
      render: (project: Project) => {
        if (!project) return '-';
        return project.title || project.name || '-';
      }
    },
    { key: 'clientName', label: 'Client' },
    {
      key: 'startDate',
      label: 'DÃ©but',
      render: (project: Project) => {
        if (!project) return '-';
        return formatDate(project.startDate);
      }
    },
    {
      key: 'endDate',
      label: 'Fin',
      render: (project: Project) => {
        if (!project) return '-';
        return formatDate(project.endDate);
      }
    },
    {
      key: 'budget',
      label: 'Budget',
      render: (project: Project) => {
        if (!project) return '-';
        return project.budget ? `${project.budget.toLocaleString()} â‚¬` : '-';
      }
    },
    {
      key: 'status',
      label: 'Statut',
      render: (project: Project) => {
        if (!project) return '-';
        const info = getStatusBadge(project.status);
        return <span className={info.className}>{info.label}</span>;
      }
    }
  ];

  return (
    <div className="sales-installations-page">
      <div className="filters-bar">
        <div className="filter-group">
          <label>ðŸ”</label>
          <input
            type="text"
            className="filter-input"
            placeholder="NÂ° projet, client..."
          />
        </div>
        <div className="filter-group">
          <label>Statut</label>
          <select className="filter-select">
            <option value="">Tous</option>
            <option value="draft">Brouillon</option>
            <option value="active">Actif</option>
            <option value="completed">TerminÃ©</option>
          </select>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary">ðŸ“„ Importer PDF</button>
          <button className="btn btn-primary">âž• Nouvelle Affaire</button>
        </div>
      </div>

      <div className="page-content">
        <DataTable
          columns={projectColumns}
          data={projects || []}
          loading={loading}
          emptyMessage="Aucun projet trouvÃ©"
          selectedItem={selectedProject}
          onRowClick={(project) => {
            if (selectedProject?.id === project.id) {
              setSelectedProject(null);
              setIsDetailOpen(false);
            } else {
              setSelectedProject(project);
              setIsDetailOpen(true);
            }
          }}
          onEdit={(project) => {
            setSelectedProject(project);
            setIsModalOpen(true);
          }}
        />
      </div>

      <SalesInstallationDetail
        project={selectedProject}
        isOpen={isDetailOpen}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedProject(null);
        }}
        onEdit={() => {
          setIsDetailOpen(false);
          setIsModalOpen(true);
        }}
      />

      {isModalOpen && (
        <SalesInstallationModal
          project={selectedProject}
          isOpen={true}
          onClose={() => {
            setIsModalOpen(false);
            setSelectedProject(null);
          }}
          onSave={async (updatedProject) => {
            try {
              if (selectedProject?.id) {
                await apiService.put(`/api/projects/${selectedProject.id}`, updatedProject);
                reloadProj();
              }
              setIsModalOpen(false);
              setSelectedProject(null);
            } catch (error) {
              logger.error('Erreur lors de la mise Ã  jour du projet:', error);
              alert('Erreur lors de la mise Ã  jour');
            }
          }}
        />
      )}
    </div>
  );
};

export default SalesInstallations;