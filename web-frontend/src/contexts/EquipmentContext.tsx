import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import apiService from '../services/api.service';
import logger from '../services/logger.service';
import { Equipment } from '../types';

interface EquipmentStats {
  total: number;
  available: number;
  inUse: number;
  maintenance: number;
}

interface EquipmentContextType {
  equipment: Equipment[];
  stats: EquipmentStats;
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
  initialized: boolean;
}

const EquipmentContext = createContext<EquipmentContextType | undefined>(undefined);

export const EquipmentProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [equipment, setEquipment] = useState<Equipment[]>([]);
  const [stats, setStats] = useState<EquipmentStats>({
    total: 0,
    available: 0,
    inUse: 0,
    maintenance: 0
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [initialized, setInitialized] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [equipmentData, statsData] = await Promise.all([
        apiService.getEquipment(),
        apiService.getEquipmentStats()
      ]);

      setEquipment(equipmentData);
      setStats(statsData);
      setInitialized(true);
    } catch (err) {
      logger.error('Erreur chargement équipements:', err);
      setError('Impossible de charger les équipements');
    } finally {
      setLoading(false);
    }
  }, []);

  // Chargement initial au démarrage de l'application
  useEffect(() => {
    loadData();
  }, [loadData]);

  const refresh = useCallback(async () => {
    await loadData();
  }, [loadData]);

  return (
    <EquipmentContext.Provider value={{ equipment, stats, loading, error, refresh, initialized }}>
      {children}
    </EquipmentContext.Provider>
  );
};

export const useEquipment = () => {
  const context = useContext(EquipmentContext);
  if (context === undefined) {
    throw new Error('useEquipment must be used within an EquipmentProvider');
  }
  return context;
};
