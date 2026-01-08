import { useState, useEffect, useCallback } from 'react';

/**
 * Hook personnalisé pour charger des données depuis l'API
 * Gère automatiquement l'état de chargement, les erreurs et le rechargement
 * 
 * @example
 * const { data, loading, error, reload } = useApiData(() => apiService.getClients());
 * 
 * if (loading) return <LoadingState />;
 * if (error) return <ErrorMessage error={error} />;
 * return <ClientList clients={data} onReload={reload} />;
 */
export function useApiData<T>(
  apiCall: () => Promise<T>,
  dependencies: any[] = []
) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const reload = useCallback(async () => {
    try {
      console.log('🔄 [useApiData] Début du chargement...');
      setLoading(true);
      setError(null);
      const result = await apiCall();
      console.log('✅ [useApiData] Données chargées avec succès:', result);
      setData(result);
    } catch (err) {
      console.error('❌ [useApiData] Erreur lors du chargement:', err);
      setError(err instanceof Error ? err : new Error('Une erreur est survenue'));
    } finally {
      setLoading(false);
      console.log('🏁 [useApiData] Chargement terminé');
    }
  }, dependencies);

  useEffect(() => {
    reload();
  }, [reload]);

  return { data, loading, error, reload };
}
