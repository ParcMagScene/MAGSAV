import React from 'react';
import './LoadingState.css';

interface LoadingStateProps {
  message?: string;
}

/**
 * Composant de chargement uniforme pour toutes les pages
 * Affiche un spinner avec un message personnalisable
 */
const LoadingState: React.FC<LoadingStateProps> = ({ message = 'Chargement...' }) => {
  return (
    <div className="loading-state">
      <div className="spinner"></div>
      <p className="loading-message">{message}</p>
    </div>
  );
};

export default LoadingState;
