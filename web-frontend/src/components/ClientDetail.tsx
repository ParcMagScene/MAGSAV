import React from 'react';
import { Client } from '../types/entities';
import DetailDrawer from './DetailDrawer';

interface ClientDetailProps {
  client: Client | null;
  isOpen: boolean;
  onClose: () => void;
}

const ClientDetail: React.FC<ClientDetailProps> = ({ client, isOpen, onClose }) => {
  return (
    <DetailDrawer
      isOpen={isOpen}
      onClose={onClose}
      title={client?.name || 'Client'}
      width="700px"
      itemId={client?.id}
    >
      {!client ? null : (
        <>
          <div className="detail-section">
            <h3 className="detail-section-title">Informations générales</h3>
            <div className="detail-row">
              <span className="detail-label">Nom</span>
              <span className="detail-value">{client.name || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Type</span>
              <span className="detail-value">
                {client.type === 'ENTREPRISE' ? 'Entreprise' :
                  client.type === 'PROFESSIONNEL' ? 'Professionnel' : 'Particulier'}
              </span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Contact</h3>
            <div className="detail-row">
              <span className="detail-label">Email</span>
              <span className="detail-value">{client.email || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Téléphone</span>
              <span className="detail-value">{client.phone || '-'}</span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Adresse</h3>
            <div className="detail-row">
              <span className="detail-label">Adresse</span>
              <span className="detail-value">{client.address || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Code postal</span>
              <span className="detail-value">{client.postalCode || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Ville</span>
              <span className="detail-value">{client.city || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Pays</span>
              <span className="detail-value">{client.country || '-'}</span>
            </div>
          </div>

          {client.notes && (
            <div className="detail-section">
              <h3 className="detail-section-title">Notes</h3>
              <div className="detail-value" style={{ whiteSpace: 'pre-wrap' }}>
                {client.notes}
              </div>
            </div>
          )}
        </>
      )}
    </DetailDrawer>
  );
};

export default ClientDetail;
