import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import LoadingState from '../components/LoadingState';
import PlanningEventDetail from '../components/PlanningEventDetail';
import PlanningEventModal from '../components/PlanningEventModal';
import { useApiData } from '../hooks/useApiData';
import { usePageContext } from '../contexts/PageContext';
import apiService from '../services/api.service';
import './Planning.css';

interface PlanningEvent {
    id: string;
    type: 'PERSONNEL' | 'VEHICLE';
    resourceId: number;
    resourceName: string;
    projectId?: number;
    projectName?: string;
    startDate: string;
    endDate: string;
    description?: string;
    status: string;
}

const Planning: React.FC = () => {
    const { setPageTitle } = usePageContext();
    const [selectedEvent, setSelectedEvent] = useState<PlanningEvent | null>(null);
    const [isDetailOpen, setIsDetailOpen] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);

    useEffect(() => {
        setPageTitle('📅 Planning');
    }, [setPageTitle]);

    // ✨ Refactorisation : utilisation du hook useApiData
    const { data: events, loading, error, reload: reloadEvents } =
        useApiData<PlanningEvent[]>(() => apiService.getCompleteSchedule());

    if (loading) {
        return (
            <div className="page-container">
                <LoadingState message="Chargement du planning..." />
            </div>
        );
    }

    if (error) {
        return (
            <div className="page-container">
                <div className="error-container">
                    <h2>❌ Erreur</h2>
                    <p>{error.message}</p>
                    <button onClick={reloadEvents} className="btn-retry">
                        Réessayer
                    </button>
                </div>
            </div>
        );
    }

    const columns = [
        {
            key: 'type',
            label: 'Type',
            render: (value: string) => (value === 'PERSONNEL' ? '👤' : '🚐'),
            width: '60px',
        },
        { key: 'resourceName', label: 'Ressource' },
        { key: 'projectName', label: 'Projet' },
        {
            key: 'startDate',
            label: 'Début',
            render: (value: string) => new Date(value).toLocaleDateString('fr-FR'),
        },
        {
            key: 'endDate',
            label: 'Fin',
            render: (value: string) => new Date(value).toLocaleDateString('fr-FR'),
        },
        { key: 'description', label: 'Description' },
        {
            key: 'status',
            label: 'Statut',
            render: (value: string) => (
                <span className={`status-badge status-${value.toLowerCase()}`}>{value}</span>
            ),
        },
    ];

    return (
        <div className="planning-page">
            <div className="filters-bar">
                <div className="filter-group">
                    <label>Type</label>
                    <select className="filter-select">
                        <option value="">Tous</option>
                        <option value="PERSONNEL">Personnel</option>
                        <option value="VEHICLE">Véhicules</option>
                    </select>
                </div>
                <div className="filter-group">
                    <label>Statut</label>
                    <select className="filter-select">
                        <option value="">Tous</option>
                        <option value="pending">En attente</option>
                        <option value="confirmed">Confirmé</option>
                        <option value="in_progress">En cours</option>
                    </select>
                </div>
                <div className="filter-group">
                    <label>Période</label>
                    <input type="date" className="filter-input" />
                </div>
                <div className="header-actions">
                    <button className="btn btn-secondary">📄 Exporter</button>
                    <button className="btn btn-primary">➕ Nouvel Événement</button>
                </div>
            </div>

            <div className="page-content">
                <DataTable
                    columns={columns}
                    data={events || []}
                    selectedItem={selectedEvent}
                    onRowClick={(event) => {
                        if (selectedEvent?.id === event.id) {
                            setSelectedEvent(null);
                            setIsDetailOpen(false);
                        } else {
                            setSelectedEvent(event);
                            setIsDetailOpen(true);
                        }
                    }}
                    onEdit={(event) => {
                        setSelectedEvent(event);
                        setIsModalOpen(true);
                    }}
                />
            </div>

            <PlanningEventDetail
                event={selectedEvent}
                isOpen={isDetailOpen}
                onClose={() => {
                    setIsDetailOpen(false);
                    setSelectedEvent(null);
                }}
                onEdit={() => {
                    setIsDetailOpen(false);
                    setIsModalOpen(true);
                }}
            />

            {isModalOpen && (
                <PlanningEventModal
                    event={selectedEvent!}
                    isOpen={true}
                    onClose={() => {
                        setIsModalOpen(false);
                        setSelectedEvent(null);
                    }}
                    onSave={async (updatedEvent) => {
                        try {
                            if (selectedEvent?.id) {
                                await apiService.put(`/api/schedule/${selectedEvent.id}`, updatedEvent);
                                reloadEvents();
                            }
                            setIsModalOpen(false);
                            setSelectedEvent(null);
                        } catch (error) {
                            console.error('Erreur lors de la mise à jour de l\'événement:', error);
                            alert('Erreur lors de la mise à jour');
                        }
                    }}
                />
            )}
        </div>
    );
};

export default Planning;
