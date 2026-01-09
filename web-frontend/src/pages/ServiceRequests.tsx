import React, { useState, useEffect, useRef } from 'react';
import apiService from '../services/api.service';
import logger from '../services/logger.service';
import DataTable from '../components/DataTable';
import StatCard from '../components/StatCard';
import LoadingState from '../components/LoadingState';
import ServiceRequestDetail from '../components/ServiceRequestDetail';
import RepairDetail from '../components/RepairDetail';
import RMADetail from '../components/RMADetail';
import ValidationModal from '../components/ValidationModal';
import ContextMenu from '../components/ContextMenu';
import { useApiData } from '../hooks/useApiData';
import { usePageContext } from '../contexts/PageContext';
import { ServiceRequest, Repair, RMA, ServiceRequestStats } from '../types/entities';
import { translateStatus, translatePriority, formatFrenchDate } from '../utils/translations';
import './ServiceRequests.css';

type TabType = 'requests' | 'repairs' | 'rma' | 'scrap';

const ServiceRequests: React.FC = () => {
  const { setPageTitle } = usePageContext();
  const [activeTab, setActiveTab] = useState<TabType>('requests');
  const [selectedRequest, setSelectedRequest] = useState<ServiceRequest | null>(null);
  const [selectedRepair, setSelectedRepair] = useState<Repair | null>(null);
  const [selectedRMA, setSelectedRMA] = useState<RMA | null>(null);
  const [selectedEquipment, setSelectedEquipment] = useState<any | null>(null);
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [contextMenu, setContextMenu] = useState<{ x: number; y: number; request: ServiceRequest } | null>(null);
  const [isValidationModalOpen, setIsValidationModalOpen] = useState(false);
  const [requestToValidate, setRequestToValidate] = useState<ServiceRequest | null>(null);
  const [highlightedRowId, setHighlightedRowId] = useState<number | null>(null);

  useEffect(() => {
    setPageTitle('Ã°Å¸â€Â§ SAV');
  }, [setPageTitle]);

  const { data: serviceRequests, loading: loadingRequests, reload: reloadRequests } = useApiData<ServiceRequest[]>(
    () => apiService.getServiceRequests()
  );
  const { data: repairs, loading: loadingRepairs, reload: reloadRepairs } = useApiData<Repair[]>(
    () => apiService.getRepairs()
  );
  const { data: rmas, loading: loadingRMAs, reload: reloadRMAs } = useApiData<RMA[]>(
    () => apiService.getRMAs()
  );
  const { data: stats, loading: loadingStats } = useApiData<ServiceRequestStats>(
    () => apiService.getServiceRequestStats()
  );
  const { data: scrapEquipment, loading: loadingScrap, reload: reloadScrap } = useApiData<any[]>(
    () => apiService.getEquipmentByStatus('OUT_OF_ORDER')
  );

  const loading = loadingRequests || loadingRepairs || loadingRMAs || loadingStats || loadingScrap;

  const handleDeleteRequest = async (request: ServiceRequest) => {
    if (window.confirm(`ÃƒÅ tes-vous sÃƒÂ»r de vouloir supprimer la demande ${request.requestNumber} ?`)) {
      try {
        await apiService.deleteServiceRequest(request.id);
        reloadRequests();
        setContextMenu(null);
      } catch (error) {
        logger.error('Erreur lors de la suppression:', error);
        alert('Erreur lors de la suppression de la demande');
      }
    }
  };

  const handleValidateRequest = async (actionType: 'DIAGNOSTIC' | 'INTERNAL_REPAIR' | 'RMA' | 'SCRAP') => {
    if (!requestToValidate) return;

    try {
      logger.debug('Ã°Å¸â€â€ž Validation en cours:', actionType, requestToValidate);

      let relatedRepairId: number | undefined;
      let relatedRmaId: number | undefined;

      if (actionType === 'DIAGNOSTIC' || actionType === 'INTERNAL_REPAIR') {
        const priorityMap: { [key: string]: string } = { 'LOW': 'LOW', 'MEDIUM': 'NORMAL', 'HIGH': 'HIGH', 'URGENT': 'URGENT' };
        const repairNumber = 'REP-' + Math.random().toString(36).substring(2, 10).toUpperCase();
        const repairData: any = {
          repairNumber,
          equipmentName: requestToValidate.equipmentInternalReference || requestToValidate.equipmentQrCode || 'MatÃƒÂ©riel non identifiÃƒÂ©',
          problemDescription: requestToValidate.description || requestToValidate.title,
          status: 'IN_PROGRESS',
          priority: priorityMap[requestToValidate.priority] || 'NORMAL',
          serviceRequestId: requestToValidate.id
        };
        if (requestToValidate.equipmentQrCode) {
          repairData.equipmentSerialNumber = requestToValidate.equipmentQrCode;
        }
        logger.debug('Ã°Å¸â€Â§ CrÃƒÂ©ation rÃƒÂ©paration:', repairData);
        const createdRepair = await apiService.createRepair(repairData);
        relatedRepairId = createdRepair.id;
        logger.debug('Ã¢Å“â€¦ RÃƒÂ©paration crÃƒÂ©ÃƒÂ©e avec ID:', relatedRepairId);
        reloadRepairs();
      } else if (actionType === 'RMA') {
        const priorityMap: { [key: string]: string } = { 'LOW': 'LOW', 'MEDIUM': 'NORMAL', 'HIGH': 'HIGH', 'URGENT': 'URGENT' };
        const rmaNumber = 'RMA-' + Math.random().toString(36).substring(2, 10).toUpperCase();
        const rmaData: any = {
          rmaNumber,
          equipmentName: requestToValidate.equipmentInternalReference || requestToValidate.equipmentQrCode || 'MatÃƒÂ©riel non identifiÃƒÂ©',
          description: requestToValidate.description || requestToValidate.title,
          status: 'REQUEST_PENDING',
          reason: 'MANUFACTURING_DEFECT',
          priority: priorityMap[requestToValidate.priority] || 'NORMAL',
          serviceRequestId: requestToValidate.id
        };
        if (requestToValidate.equipmentQrCode) {
          rmaData.equipmentSerialNumber = requestToValidate.equipmentQrCode;
        }
        logger.debug('Ã°Å¸â€œÂ¦ CrÃƒÂ©ation RMA:', rmaData);
        const createdRMA = await apiService.createRMA(rmaData);
        relatedRmaId = createdRMA.id;
        logger.debug('Ã¢Å“â€¦ RMA crÃƒÂ©ÃƒÂ© avec ID:', relatedRmaId);
        reloadRMAs();
      } else if (actionType === 'SCRAP') {
        if (requestToValidate.equipmentId) {
          logger.debug('🔸 Mise au rebut équipement:', requestToValidate.equipmentId);
          await apiService.updateEquipment(requestToValidate.equipmentId, {
            status: 'OUT_OF_ORDER'
          });
          logger.debug('✅ Équipement mis au rebut');
          reloadScrap();
        }
      }

      const updateData: any = {
        title: requestToValidate.title,
        description: requestToValidate.description,
        priority: requestToValidate.priority,
        status: 'VALIDATED',
        validationAction: actionType
      };
      if (relatedRepairId) updateData.relatedRepairId = relatedRepairId;
      if (relatedRmaId) updateData.relatedRmaId = relatedRmaId;

      await apiService.updateServiceRequest(requestToValidate.id, updateData);
      logger.debug('✅ Demande passée en "Validée"');

      reloadRequests();
      setIsValidationModalOpen(false);
      setRequestToValidate(null);
      setActiveTab(actionType === 'DIAGNOSTIC' || actionType === 'INTERNAL_REPAIR' ? 'repairs' : actionType === 'RMA' ? 'rma' : actionType === 'SCRAP' ? 'scrap' : 'requests');
      logger.debug('✅ Validation terminée avec succès');
    } catch (error: any) {
      logger.error('Ã¢ÂÅ’ Erreur lors de la validation:', error);
      alert(`Erreur lors de la validation:\n${error.response?.data?.message || error.message}`);
    }
  };

  const navigateToRelated = (request: ServiceRequest) => {
    if (request.validationAction === 'DIAGNOSTIC' || request.validationAction === 'INTERNAL_REPAIR') {
      if (request.relatedRepairId) {
        setActiveTab('repairs');
        setHighlightedRowId(request.relatedRepairId);
        setTimeout(() => setHighlightedRowId(null), 2000);
      }
    } else if (request.validationAction === 'RMA') {
      if (request.relatedRmaId) {
        setActiveTab('rma');
        setHighlightedRowId(request.relatedRmaId);
        setTimeout(() => setHighlightedRowId(null), 2000);
      }
    } else if (request.validationAction === 'SCRAP') {
      setActiveTab('scrap');
      if (request.equipmentId) {
        setHighlightedRowId(request.equipmentId);
        setTimeout(() => setHighlightedRowId(null), 2000);
      }
    }
    setContextMenu(null);
  };

  const getActionLabel = (action?: string) => {
    switch (action) {
      case 'DIAGNOSTIC': return 'Ã°Å¸â€Â Diagnostique Interne';
      case 'INTERNAL_REPAIR': return 'Ã°Å¸â€Â§ RÃƒÂ©paration Interne';
      case 'RMA': return 'Ã¢â€ Â©Ã¯Â¸Â Retour Fournisseur (RMA)';
      case 'SCRAP': return 'Ã°Å¸â€”â€˜Ã¯Â¸Â Mis au Rebut';
      default: return 'Ã¢Ââ€œ Action inconnue';
    }
  };

  const handleContextMenu = (e: React.MouseEvent, request: ServiceRequest) => {
    e.preventDefault();
    setContextMenu({
      x: e.clientX,
      y: e.clientY,
      request
    });
  };

  if (loading) {
    return (
      <div className="page-container">
        <LoadingState message="Chargement des donnÃƒÂ©es SAV..." />
      </div>
    );
  }

  const requestColumns = [
    { key: 'requestNumber', label: 'NÃ‚Â° Demande', width: '120px' },
    { key: 'title', label: 'Titre' },
    { key: 'equipmentInternalReference', label: 'Code LOCMAT', width: '120px' },
    { key: 'equipmentQrCode', label: 'UID', width: '100px' },
    {
      key: 'status',
      label: 'Statut',
      render: (value: string) => {
        const label = translateStatus(value, 'serviceRequest');
        return <span className={`status-badge status-${value.toLowerCase().replace('_', '-')}`}>{label}</span>;
      },
    },
    {
      key: 'priority',
      label: 'PrioritÃƒÂ©',
      render: (value: string) => {
        const label = translatePriority(value);
        return <span className={`priority-badge priority-${value.toLowerCase()}`}>{label}</span>;
      },
    },
    {
      key: 'requestDate',
      label: 'Date',
      render: (value: string) => formatFrenchDate(value),
    }
  ];

  const repairColumns = [
    { key: 'repairNumber', label: 'NÃ‚Â° RÃƒÂ©paration', width: '120px' },
    { key: 'equipmentName', label: 'MatÃƒÂ©riel' },
    { key: 'problemDescription', label: 'Description' },
    { key: 'equipmentSerialNumber', label: 'SÃƒÂ©rie', width: '120px' },
    {
      key: 'status',
      label: 'Statut',
      render: (value: string) => {
        const label = translateStatus(value, 'repair');
        return <span className={`status-badge status-${value.toLowerCase().replace('_', '-')}`}>{label}</span>;
      },
    },
    {
      key: 'priority',
      label: 'PrioritÃƒÂ©',
      render: (value: string) => {
        const label = translatePriority(value);
        return <span className={`priority-badge priority-${value.toLowerCase()}`}>{label}</span>;
      },
    },
    {
      key: 'startDate',
      label: 'DÃƒÂ©but',
      render: (value: string) => formatFrenchDate(value),
    },
  ];

  const rmaColumns = [
    { key: 'rmaNumber', label: 'NÃ‚Â° RMA', width: '120px' },
    { key: 'equipmentName', label: 'MatÃƒÂ©riel' },
    { key: 'description', label: 'Description' },
    { key: 'equipmentSerialNumber', label: 'SÃƒÂ©rie', width: '120px' },
    {
      key: 'status',
      label: 'Statut',
      render: (value: string) => {
        const label = translateStatus(value, 'rma');
        return <span className={`status-badge status-${value.toLowerCase().replace('_', '-')}`}>{label}</span>;
      },
    },
    {
      key: 'requestDate',
      label: 'Date Demande',
      render: (value: string) => formatFrenchDate(value),
    },
  ];

  const scrapColumns = [
    { key: 'qrCode', label: 'UID', width: '120px' },
    { key: 'name', label: 'Nom' },
    { key: 'internalReference', label: 'Code LOCMAT', width: '120px' },
    { key: 'category', label: 'CatÃƒÂ©gorie' },
    { key: 'brand', label: 'Marque' },
    { key: 'model', label: 'ModÃƒÂ¨le' },
    {
      key: 'updatedAt',
      label: 'Date Mise au Rebut',
      render: (value: string) => formatFrenchDate(value),
    },
  ];

  // GÃƒÂ©nÃƒÂ©ration des items du menu contextuel
  const getContextMenuItems = (request: ServiceRequest) => {
    const items = [];

    if (request.status === 'VALIDATED' && request.validationAction) {
      items.push({
        label: `Voir ${getActionLabel(request.validationAction)}`,
        icon: 'Ã°Å¸â€˜ÂÃ¯Â¸Â',
        onClick: () => navigateToRelated(request)
      });
      items.push({
        label: 'Modifier la dÃƒÂ©marche',
        icon: 'Ã¢Å“ÂÃ¯Â¸Â',
        onClick: () => {
          setRequestToValidate(request);
          setIsValidationModalOpen(true);
          setContextMenu(null);
        }
      });
    } else {
      items.push({
        label: 'Valider',
        icon: 'Ã¢Å“â€¦',
        onClick: () => {
          setRequestToValidate(request);
          setIsValidationModalOpen(true);
          setContextMenu(null);
        }
      });
    }

    items.push({
      label: 'Supprimer',
      icon: 'Ã°Å¸â€”â€˜Ã¯Â¸Â',
      onClick: () => handleDeleteRequest(request)
    });

    return items;
  };

  return (
    <div className="service-requests-page">
      <div className="filters-bar">
        <div className="filter-group">
          <label>Ã°Å¸â€Â</label>
          <input type="text" className="filter-input" placeholder="NÃ‚Â° demande, titre..." />
        </div>
        <div className="filter-group">
          <label>Statut</label>
          <select className="filter-select">
            <option value="">Tous</option>
            <option value="PENDING">En attente</option>
            <option value="VALIDATED">ValidÃƒÂ©e</option>
          </select>
        </div>
        <div className="filter-group">
          <label>PrioritÃƒÂ©</label>
          <select className="filter-select">
            <option value="">Toutes</option>
            <option value="LOW">Basse</option>
            <option value="MEDIUM">Moyenne</option>
            <option value="HIGH">Haute</option>
            <option value="URGENT">Urgente</option>
          </select>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary">Ã°Å¸â€œâ€ž Exporter</button>
          <button className="btn btn-primary">Ã¢Å¾â€¢ Nouvelle Demande</button>
        </div>
      </div>

      <div className="tabs-header">
        <button
          className={`tab-button ${activeTab === 'requests' ? 'active' : ''}`}
          onClick={() => setActiveTab('requests')}
        >
          Ã°Å¸â€œâ€¹ Demandes ({serviceRequests?.length || 0})
        </button>
        <button
          className={`tab-button ${activeTab === 'repairs' ? 'active' : ''}`}
          onClick={() => setActiveTab('repairs')}
        >
          Ã°Å¸â€Â§ RÃƒÂ©parations ({repairs?.length || 0})
        </button>
        <button
          className={`tab-button ${activeTab === 'rma' ? 'active' : ''}`}
          onClick={() => setActiveTab('rma')}
        >
          Ã¢â€ Â©Ã¯Â¸Â RMA ({rmas?.length || 0})
        </button>
        <button
          className={`tab-button ${activeTab === 'scrap' ? 'active' : ''}`}
          onClick={() => setActiveTab('scrap')}
        >
          Ã°Å¸â€”â€˜Ã¯Â¸Â Rebut ({scrapEquipment?.length || 0})
        </button>
      </div>

      <div className="tabs-content">
        {activeTab === 'requests' && (
          <>
            <DataTable
              data={serviceRequests || []}
              columns={requestColumns}
              loading={loading}
              emptyMessage="Aucune demande d'intervention"
              selectedItem={selectedRequest}
              onRowClick={(request: ServiceRequest) => {
                if (selectedRequest?.id === request.id) {
                  setSelectedRequest(null);
                  setIsDetailOpen(false);
                } else {
                  setSelectedRequest(request);
                  setIsDetailOpen(true);
                }
              }}
              onRowContextMenu={(request: ServiceRequest, e: React.MouseEvent) => {
                handleContextMenu(e, request);
              }}
            />
            {contextMenu && (
              <ContextMenu
                items={getContextMenuItems(contextMenu.request)}
                position={{ x: contextMenu.x, y: contextMenu.y }}
                onClose={() => setContextMenu(null)}
              />
            )}
          </>
        )}
        {activeTab === 'repairs' && (
          <DataTable
            data={repairs || []}
            columns={repairColumns}
            loading={loading}
            emptyMessage="Aucune rÃƒÂ©paration"
            selectedItem={selectedRepair}
            highlightedRowId={highlightedRowId}
            onRowClick={(repair: Repair) => {
              if (selectedRepair?.id === repair.id) {
                setSelectedRepair(null);
                setIsDetailOpen(false);
              } else {
                setSelectedRepair(repair);
                setIsDetailOpen(true);
              }
            }}
          />
        )}
        {activeTab === 'rma' && (
          <DataTable
            data={rmas || []}
            columns={rmaColumns}
            loading={loading}
            emptyMessage="Aucun RMA"
            selectedItem={selectedRMA}
            highlightedRowId={highlightedRowId}
            onRowClick={(rma: RMA) => {
              if (selectedRMA?.id === rma.id) {
                setSelectedRMA(null);
                setIsDetailOpen(false);
              } else {
                setSelectedRMA(rma);
                setIsDetailOpen(true);
              }
            }}
          />
        )}
        {activeTab === 'scrap' && (
          <DataTable
            data={scrapEquipment || []}
            columns={scrapColumns}
            loading={loading}
            emptyMessage="Aucun ÃƒÂ©quipement au rebut"
            selectedItem={selectedEquipment}
            highlightedRowId={highlightedRowId}
            onRowClick={(equipment: any) => {
              if (selectedEquipment?.id === equipment.id) {
                setSelectedEquipment(null);
                setIsDetailOpen(false);
              } else {
                setSelectedEquipment(equipment);
                setIsDetailOpen(true);
              }
            }}
          />
        )}
      </div>

      <ServiceRequestDetail
        serviceRequest={selectedRequest}
        isOpen={isDetailOpen && activeTab === 'requests'}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedRequest(null);
        }}
      />

      <RepairDetail
        repair={selectedRepair}
        isOpen={isDetailOpen && activeTab === 'repairs'}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedRepair(null);
        }}
      />

      <RMADetail
        rma={selectedRMA}
        isOpen={isDetailOpen && activeTab === 'rma'}
        onClose={() => {
          setIsDetailOpen(false);
          setSelectedRMA(null);
        }}
      />

      <ValidationModal
        isOpen={isValidationModalOpen}
        onClose={() => {
          setIsValidationModalOpen(false);
          setRequestToValidate(null);
        }}
        onValidate={handleValidateRequest}
        request={requestToValidate}
      />
    </div>
  );
};

export default ServiceRequests;
