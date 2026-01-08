import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api.service';
import './GlobalSearch.css';

interface SearchResult {
  type: 'equipment' | 'sav' | 'client' | 'vehicle';
  id: number;
  title: string;
  subtitle: string;
  icon: string;
  route: string;
}

const GlobalSearch: React.FC = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const searchRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    const searchTimeout = setTimeout(() => {
      if (query.trim().length >= 2) {
        performSearch(query.trim());
      } else {
        setResults([]);
        setIsOpen(false);
      }
    }, 300);

    return () => clearTimeout(searchTimeout);
  }, [query]);

  const performSearch = async (searchQuery: string) => {
    setLoading(true);
    const searchResults: SearchResult[] = [];

    try {
      // Recherche dans les équipements
      const equipments = await apiService.getEquipment();
      const matchingEquipments = equipments.filter(eq =>
        eq.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        eq.internalReference?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        eq.serialNumber?.toLowerCase().includes(searchQuery.toLowerCase())
      ).slice(0, 5);

      matchingEquipments.forEach(eq => {
        searchResults.push({
          type: 'equipment',
          id: eq.id || 0,
          title: eq.name || 'Sans nom',
          subtitle: `Réf: ${eq.internalReference || 'N/A'} - ${eq.category || 'N/A'}`,
          icon: '📦',
          route: '/equipment'
        });
      });

      // Recherche dans les demandes SAV
      try {
        const savRequests = await apiService.getServiceRequests();
        const matchingSAV = savRequests.filter(sav =>
          sav.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
          sav.reference?.toLowerCase().includes(searchQuery.toLowerCase())
        ).slice(0, 5);

        matchingSAV.forEach(sav => {
          searchResults.push({
            type: 'sav',
            id: sav.id || 0,
            title: sav.title || 'Sans titre',
            subtitle: `Réf: ${sav.reference || 'N/A'} - ${sav.status || 'N/A'}`,
            icon: '🔧',
            route: '/sav'
          });
        });
      } catch (error) {
        console.error('Erreur recherche SAV:', error);
      }

      // Recherche dans les clients
      try {
        const clients = await apiService.getClients();
        const matchingClients = clients.filter(client =>
          client.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
          client.email?.toLowerCase().includes(searchQuery.toLowerCase())
        ).slice(0, 5);

        matchingClients.forEach(client => {
          searchResults.push({
            type: 'client',
            id: client.id || 0,
            title: client.name || 'Sans nom',
            subtitle: client.email || 'Pas d\'email',
            icon: '👥',
            route: '/clients'
          });
        });
      } catch (error) {
        console.error('Erreur recherche clients:', error);
      }

      // Recherche dans les véhicules
      try {
        const vehicles = await apiService.getVehicles();
        const matchingVehicles = vehicles.filter(vehicle =>
          vehicle.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
          vehicle.registrationNumber?.toLowerCase().includes(searchQuery.toLowerCase())
        ).slice(0, 5);

        matchingVehicles.forEach(vehicle => {
          searchResults.push({
            type: 'vehicle',
            id: vehicle.id || 0,
            title: vehicle.name || 'Sans nom',
            subtitle: `Immat: ${vehicle.registrationNumber || 'N/A'}`,
            icon: '🚐',
            route: '/vehicles'
          });
        });
      } catch (error) {
        console.error('Erreur recherche véhicules:', error);
      }

      setResults(searchResults);
      setIsOpen(searchResults.length > 0);
    } catch (error) {
      console.error('Erreur lors de la recherche:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleResultClick = (result: SearchResult) => {
    navigate(result.route);
    setQuery('');
    setResults([]);
    setIsOpen(false);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Escape') {
      setIsOpen(false);
      setQuery('');
    }
  };

  return (
    <div className="global-search" ref={searchRef}>
      <div className="search-input-wrapper">
        <span className="search-icon">🔍</span>
        <input
          type="text"
          className="search-input"
          placeholder="Rechercher équipement, SAV, client, véhicule..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => query.trim().length >= 2 && setIsOpen(true)}
          onKeyDown={handleKeyDown}
        />
        {loading && <span className="search-loading">⏳</span>}
        {query && (
          <button
            className="search-clear"
            onClick={() => {
              setQuery('');
              setResults([]);
              setIsOpen(false);
            }}
          >
            ✕
          </button>
        )}
      </div>

      {isOpen && results.length > 0 && (
        <div className="search-results">
          <div className="search-results-header">
            {results.length} résultat{results.length > 1 ? 's' : ''}
          </div>
          {results.map((result, index) => (
            <div
              key={`${result.type}-${result.id}-${index}`}
              className="search-result-item"
              onClick={() => handleResultClick(result)}
            >
              <span className="result-icon">{result.icon}</span>
              <div className="result-content">
                <div className="result-title">{result.title}</div>
                <div className="result-subtitle">{result.subtitle}</div>
              </div>
              <span className="result-type">{result.type}</span>
            </div>
          ))}
        </div>
      )}

      {isOpen && results.length === 0 && !loading && query.trim().length >= 2 && (
        <div className="search-results">
          <div className="search-no-results">
            Aucun résultat pour "{query}"
          </div>
        </div>
      )}
    </div>
  );
};

export default GlobalSearch;
