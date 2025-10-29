import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './App.css';

// Composants de pages
import Dashboard from './pages/Dashboard';
import Equipment from './pages/Equipment';
import ServiceRequests from './pages/ServiceRequests';
import Clients from './pages/Clients';
import Contracts from './pages/Contracts';
import SalesInstallations from './pages/SalesInstallations';
import Vehicles from './pages/Vehicles';
import Personnel from './pages/Personnel';

function App() {
  return (
    <Router
      future={{
        v7_startTransition: true,
        v7_relativeSplatPath: true
      }}
    >
      <div className="App">
        <header className="App-header">
          <nav className="sidebar">
            <div className="sidebar-logo">
              <h1>📋 MAGSAV-3.0</h1>
              <p>Gestion SAV & Parc Matériel</p>
            </div>
            <ul className="sidebar-menu">
              <li><Link to="/">🏠 Dashboard</Link></li>
              <li><Link to="/equipment">📦 Parc Matériel</Link></li>
              <li><Link to="/sav">🔧 SAV & Interventions</Link></li>
              <li><Link to="/clients">👥 Clients</Link></li>
              <li><Link to="/contracts">📋 Contrats</Link></li>
              <li><Link to="/sales">💼 Ventes & Installations</Link></li>
              <li><Link to="/vehicles">🚐 Véhicules</Link></li>
              <li><Link to="/personnel">👤 Personnel</Link></li>
            </ul>
          </nav>
          <main className="main-content">
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/equipment" element={<Equipment />} />
              <Route path="/sav" element={<ServiceRequests />} />
              <Route path="/clients" element={<Clients />} />
              <Route path="/contracts" element={<Contracts />} />
              <Route path="/sales" element={<SalesInstallations />} />
              <Route path="/vehicles" element={<Vehicles />} />
              <Route path="/personnel" element={<Personnel />} />
            </Routes>
          </main>
        </header>
      </div>
    </Router>
  );
}

export default App;