import React, { ReactNode, useEffect, useLayoutEffect } from 'react';
import { flushSync } from 'react-dom';
import './DetailDrawer.css';

interface DetailDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  width?: string;
  itemId?: string | number | null;
}

const DetailDrawer: React.FC<DetailDrawerProps> = ({
  isOpen,
  onClose,
  title,
  children,
  width = '600px',
  itemId
}) => {
  const [isClosing, setIsClosing] = React.useState(false);
  const [lastItemId, setLastItemId] = React.useState<string | number | null>(null);
  const [displayedChildren, setDisplayedChildren] = React.useState<ReactNode>(children);
  const [wasOpen, setWasOpen] = React.useState(false);
  const [isAnimating, setIsAnimating] = React.useState(false);

  useLayoutEffect(() => {
    if (isOpen && itemId !== lastItemId) {
      if (itemId === null || itemId === undefined) {
        // Item désélectionné : fermer avec animation
        if (lastItemId !== null && lastItemId !== undefined) {
          setIsAnimating(true);
          // Forcer un rendu synchrone avec isClosing=true
          flushSync(() => {
            setIsClosing(true);
          });
          // Maintenant l'animation peut commencer
          setTimeout(() => {
            setIsClosing(false);
            setLastItemId(null);
            onClose();
            setTimeout(() => setIsAnimating(false), 10);
          }, 200);
        }
      } else if (lastItemId !== null && lastItemId !== undefined) {
        // Changement d'élément : fermer puis rouvrir
        setIsClosing(true);
        setTimeout(() => {
          setDisplayedChildren(children);
          setLastItemId(itemId ?? null);
          setIsClosing(false);
        }, 200);
      } else {
        // Première ouverture
        setDisplayedChildren(children);
        setLastItemId(itemId ?? null);
      }
    } else if (isOpen) {
      // Mise à jour du contenu si même item
      setDisplayedChildren(children);
    } else if (!isOpen && wasOpen && !isAnimating) {
      // Fermeture depuis l'extérieur : déclencher l'animation seulement si pas déjà en animation
      setIsClosing(true);
      setTimeout(() => {
        setIsClosing(false);
        setLastItemId(null);
      }, 200);
    }
    setWasOpen(isOpen);
  }, [isOpen, itemId, lastItemId, children, wasOpen, isAnimating, onClose]);

  const handleClose = () => {
    setIsClosing(true);
    setTimeout(() => {
      setIsClosing(false);
      setLastItemId(null);
      onClose();
    }, 200);
  };

  if (!isOpen && !isClosing) return null;

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
          <h2 className="drawer-title">{title}</h2>
          <button className="drawer-close" onClick={handleClose} aria-label="Fermer">
            ✕
          </button>
        </div>
        <div className="drawer-content">
          {displayedChildren}
        </div>
      </div>
    </>
  );
};

export default DetailDrawer;
