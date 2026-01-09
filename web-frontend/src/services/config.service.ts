/**
 * Service de gestion des configurations et référentiels de l'application
 */

export interface CategoryHierarchy {
  famille: string;
  categories: {
    categorie: string;
    types: string[];
  }[];
}

export interface EquipmentConfig {
  hierarchies: CategoryHierarchy[];
  statuses: {
    value: string;
    label: string;
    color: string;
  }[];
}

class ConfigService {
  private equipmentConfig: EquipmentConfig = {
    hierarchies: [
      {
        famille: 'Son',
        categories: [
          {
            categorie: 'Microphones',
            types: ['Micro HF', 'Micro filaire', 'Micro studio', 'Accessoires micro']
          },
          {
            categorie: 'Console',
            types: ['Console numérique', 'Console analogique', 'Surface de contrôle']
          },
          {
            categorie: 'Diffusion',
            types: ['Enceinte active', 'Enceinte passive', 'Subwoofer', 'Retour de scène']
          },
          {
            categorie: 'Amplification',
            types: ['Ampli puissance', 'Préampli', 'Processeur']
          }
        ]
      },
      {
        famille: 'Lumière',
        categories: [
          {
            categorie: 'Projecteurs',
            types: ['PAR LED', 'Lyre', 'Découpe', 'Poursuite']
          },
          {
            categorie: 'Console',
            types: ['Console lumière', 'Interface DMX']
          },
          {
            categorie: 'Effets',
            types: ['Stroboscope', 'Machine à fumée', 'Laser']
          }
        ]
      },
      {
        famille: 'Vidéo',
        categories: [
          {
            categorie: 'Projecteurs',
            types: ['Vidéoprojecteur', 'Écran LED']
          },
          {
            categorie: 'Caméras',
            types: ['Caméra PTZ', 'Caméra broadcast']
          }
        ]
      },
      {
        famille: 'Structure',
        categories: [
          {
            categorie: 'Praticable',
            types: ['Praticable 2x1m', 'Praticable 1x1m', 'Escalier', 'Garde-corps']
          },
          {
            categorie: 'Structure alu',
            types: ['Pont', 'Pied', 'Accroche']
          }
        ]
      }
    ],
    statuses: [
      { value: 'AVAILABLE', label: 'Disponible', color: '#22c55e' },
      { value: 'IN_USE', label: 'En utilisation', color: '#3b82f6' },
      { value: 'MAINTENANCE', label: 'Maintenance', color: '#f59e0b' },
      { value: 'RESERVED', label: 'Réservé', color: '#8b5cf6' },
      { value: 'OUT_OF_ORDER', label: 'Hors service', color: '#ef4444' },
      { value: 'RETIRED', label: 'Retiré', color: '#6b7280' }
    ]
  };

  // Récupérer la configuration des équipements
  getEquipmentConfig(): EquipmentConfig {
    // TODO: Charger depuis le backend
    return this.equipmentConfig;
  }

  // Sauvegarder la configuration
  async saveEquipmentConfig(config: EquipmentConfig): Promise<void> {
    // TODO: Sauvegarder vers le backend
    this.equipmentConfig = config;
    console.log('Configuration sauvegardée:', config);
  }

  // Obtenir toutes les familles
  getFamilles(): string[] {
    return this.equipmentConfig.hierarchies.map(h => h.famille);
  }

  // Obtenir les catégories d'une famille
  getCategories(famille: string): string[] {
    const hierarchy = this.equipmentConfig.hierarchies.find(h => h.famille === famille);
    return hierarchy ? hierarchy.categories.map(c => c.categorie) : [];
  }

  // Obtenir les types d'une catégorie
  getTypes(famille: string, categorie: string): string[] {
    const hierarchy = this.equipmentConfig.hierarchies.find(h => h.famille === famille);
    if (!hierarchy) return [];
    const cat = hierarchy.categories.find(c => c.categorie === categorie);
    return cat ? cat.types : [];
  }

  // Obtenir la hiérarchie complète aplatie pour les filtres
  getFlatHierarchy(): { label: string; value: string; level: number }[] {
    const result: { label: string; value: string; level: number }[] = [];
    
    this.equipmentConfig.hierarchies.forEach(hierarchy => {
      result.push({
        label: `📁 ${hierarchy.famille}`,
        value: `famille:${hierarchy.famille}`,
        level: 0
      });
      
      hierarchy.categories.forEach(category => {
        result.push({
          label: `   📂 ${category.categorie}`,
          value: `categorie:${hierarchy.famille}/${category.categorie}`,
          level: 1
        });
        
        category.types.forEach((type, index, array) => {
          result.push({
            label: `      📝 ${type}`,
            value: `type:${hierarchy.famille}/${category.categorie}/${type}`,
            level: 2
          });
        });
      });
    });
    
    return result;
  }

  // Obtenir les statuts disponibles
  getStatuses() {
    return this.equipmentConfig.statuses;
  }
}

export default new ConfigService();
