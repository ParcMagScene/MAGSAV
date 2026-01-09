import React from 'react';
import { BrowserRouter as Router, Routes, Route, NavLink } from 'react-router-dom';
import './App.css';

// Contexte
import { PageProvider, usePageContext } from './contexts/PageContext';
import { EquipmentProvider } from './contexts/EquipmentContext';

// Composants
import GlobalSearch from './components/GlobalSearch';

// Composants de pages
import Dashboard from './pages/Dashboard';
import Equipment from './pages/Equipment';
import ServiceRequests from './pages/ServiceRequests';
import Clients from './pages/Clients';
import Contracts from './pages/Contracts';
import SalesInstallations from './pages/SalesInstallations';
import Vehicles from './pages/Vehicles';
import Personnel from './pages/Personnel';
import Planning from './pages/Planning';
import Suppliers from './pages/Suppliers';
import Settings from './pages/Settings';

const TopBar: React.FC = () => {
  const { pageTitle } = usePageContext();

  return (
    <div className="top-bar">
      {pageTitle && <h1 className="top-bar-title">{pageTitle}</h1>}
      <GlobalSearch />
    </div>
  );
};

function App() {
  return (
    <PageProvider>
      <EquipmentProvider>
        <Router
          future={{
            v7_startTransition: true,
            v7_relativeSplatPath: true
          }}
        >
          <div className="App">
            <nav className="sidebar">
              <div className="sidebar-logo">
                <h1>📋 MAGSAV-3.0</h1>
                <p>Gestion SAV & Parc Matériel</p>
              </div>
              <ul className="sidebar-menu">
                <li><NavLink to="/" end>🏠 Dashboard</NavLink></li>
                <li><NavLink to="/equipment">📦 Equipements</NavLink></li>
                <li><NavLink to="/sav">🔧 SAV</NavLink></li>
                <li><NavLink to="/clients">👥 Clients</NavLink></li>
                <li><NavLink to="/contracts">📋 Contrats</NavLink></li>
                <li><NavLink to="/sales">💼 Ventes & Installations</NavLink></li>
                <li><NavLink to="/vehicles">🚐 Véhicules</NavLink></li>
                <li><NavLink to="/personnel">👤 Personnel</NavLink></li>
                <li><NavLink to="/planning">📅 Planning</NavLink></li>
                <li><NavLink to="/suppliers">🏪 Fournisseurs</NavLink></li>
                <li><NavLink to="/settings">⚙️ Paramètres</NavLink></li>
              </ul>
            </nav>
            <div className="main-wrapper">
              <TopBar />
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
                  <Route path="/planning" element={<Planning />} />
                  <Route path="/suppliers" element={<Suppliers />} />
                  <Route path="/settings" element={<Settings />} />
                </Routes>
              </main>
            </div>
          </div>
        </Router>
      </EquipmentProvider>
    </PageProvider>
  );
}

export default App;