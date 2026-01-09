import React, { ReactNode, useEffect, useLayoutEffect } from 'react';
import './DetailDrawer.css';

interface DetailDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  width?: string;
  itemId?: string | number | null;
  onEdit?: () => void;
}

const DetailDrawer: React.FC<DetailDrawerProps> = ({
  isOpen,
  onClose,
  title,
  children,
  width = '600px',
  itemId,
  onEdit
}) => {
  const [isClosing, setIsClosing] = React.useState(false);
  const [shouldRender, setShouldRender] = React.useState(false);
  const [frozenTitle, setFrozenTitle] = React.useState(title);
  const [frozenChildren, setFrozenChildren] = React.useState(children);
  const wasOpen = React.useRef(false);

  // Détecter les changements d'état isOpen
  useEffect(() => {
    if (isOpen && !wasOpen.current) {
      // Ouverture
      setShouldRender(true);
      setIsClosing(false);
      setFrozenTitle(title);
      setFrozenChildren(children);
      wasOpen.current = true;
    } else if (!isOpen && wasOpen.current) {
      // Fermeture demandée - garder le contenu figé
      setIsClosing(true);
      setTimeout(() => {
        setIsClosing(false);
        setShouldRender(false);
        wasOpen.current = false;
      }, 200);
    } else if (isOpen) {
      // Mise à jour du contenu si ouvert
      setFrozenTitle(title);
      setFrozenChildren(children);
    }
  }, [isOpen, title, children]);

  const handleClose = () => {
    onClose();
  };

  if (!shouldRender) return null;

  return (
    <>
      <div
        className={`drawer-overlay ${isClosing ? 'closing' : ''}`}
        onClick={handleClose}
      />
      <div
        className={`drawer-container ${isClosing ? 'closing' : ''}`}
        style={{ width }}
      >
        <div className="drawer-header">
          <h2 className="drawer-title">{frozenTitle}</h2>
          <div className="drawer-header-actions">
            {onEdit && (
              <button className="btn btn-secondary" onClick={onEdit}>
                ✏️ Modifier
              </button>
            )}
            <button className="drawer-close" onClick={handleClose} aria-label="Fermer">
              ✕
            </button>
          </div>
        </div>
        <div className="drawer-content">
          {frozenChildren}
        </div>
      </div>
    </>
  );
};

export default DetailDrawer;
