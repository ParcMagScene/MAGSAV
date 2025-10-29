import React from 'react';

const Dashboard: React.FC = () => {
  return (
    <div className="page-container">
      <h1 className="page-title">
        🏠 Dashboard
      </h1>
      
      <div className="welcome-message">
        <h2>Bienvenue dans MAGSAV-3.0</h2>
        <p>Système de Gestion SAV et Parc Matériel - Interface Web</p>
      </div>
      
      <div className="stats-grid">
        <div className="stat-card">
          <h3>📦</h3>
          <p>Parc Matériel</p>
        </div>
        <div className="stat-card">
          <h3>🔧</h3>
          <p>SAV & Interventions</p>
        </div>
        <div className="stat-card">
          <h3>👥</h3>
          <p>Clients</p>
        </div>
        <div className="stat-card">
          <h3>🚐</h3>
          <p>Véhicules</p>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;