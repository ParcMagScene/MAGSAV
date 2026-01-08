import React from 'react';
import { Personnel } from '../types/entities';
import DetailDrawer from './DetailDrawer';

interface PersonnelDetailProps {
  personnel: Personnel | null;
  isOpen: boolean;
  onClose: () => void;
}

const PersonnelDetail: React.FC<PersonnelDetailProps> = ({ personnel, isOpen, onClose }) => {
  return (
    <DetailDrawer
      isOpen={isOpen}
      onClose={onClose}
      title={personnel ? `${personnel.firstName || ''} ${personnel.lastName || ''}`.trim() || 'Personnel' : 'Personnel'}
      width="700px"
      itemId={personnel?.id}
    >
      {!personnel ? null : (
        <>
          <div className="detail-section">
            <h3 className="detail-section-title">Informations générales</h3>
            <div className="detail-row">
              <span className="detail-label">Prénom</span>
              <span className="detail-value">{personnel.firstName || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Nom</span>
              <span className="detail-value">{personnel.lastName || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Statut</span>
              <span className="detail-value">
                {personnel.active ? 'Actif' : 'Inactif'}
              </span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Type</span>
              <span className="detail-value">
                {personnel.type === 'PERMANENT' ? 'Permanent' :
                  personnel.type === 'INTERMITTENT' ? 'Intermittent' :
                    personnel.type === 'FREELANCE' ? 'Freelance' : personnel.type}
              </span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Contact</h3>
            <div className="detail-row">
              <span className="detail-label">Email</span>
              <span className="detail-value">{personnel.email || '-'}</span>
            </div>
            <div className="detail-row">
              <span className="detail-label">Téléphone</span>
              <span className="detail-value">{personnel.phone || '-'}</span>
            </div>
          </div>

          <div className="detail-section">
            <h3 className="detail-section-title">Poste</h3>
            <div className="detail-row">
              <span className="detail-label">Fonction</span>
              <span className="detail-value">{personnel.position || '-'}</span>
            </div>
          </div>

          {personnel.hireDate && (
            <div className="detail-section">
              <h3 className="detail-section-title">Dates</h3>
              <div className="detail-row">
                <span className="detail-label">Date d'embauche</span>
                <span className="detail-value">
                  {new Date(personnel.hireDate).toLocaleDateString('fr-FR')}
                </span>
              </div>
            </div>
          )}

          {personnel.qualifications && personnel.qualifications.length > 0 && (
            <div className="detail-section">
              <h3 className="detail-section-title">Qualifications</h3>
              <div className="detail-value">
                {personnel.qualifications.join(', ')}
              </div>
            </div>
          )}
        </>
      )}
    </DetailDrawer>
  );
};

export default PersonnelDetail;
