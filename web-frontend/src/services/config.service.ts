/**
 * Service de gestion des configurations et rÃ©fÃ©rentiels de l'application
 */
import logger from './logger.service';

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
            types: ['Console numÃ©rique', 'Console analogique', 'Surface de contrÃ´le']
          },
          {
            categorie: 'Diffusion',
            types: ['Enceinte active', 'Enceinte passive', 'Subwoofer', 'Retour de scÃ¨ne']
          },
          {
            categorie: 'Amplification',
            types: ['Ampli puissance', 'PrÃ©ampli', 'Processeur']
          }
        ]
      },
      {
        famille: 'LumiÃ¨re',
        categories: [
          {
            categorie: 'Projecteurs',
            types: ['PAR LED', 'Lyre', 'DÃ©coupe', 'Poursuite']
          },
          {
            categorie: 'Console',
            types: ['Console lumiÃ¨re', 'Interface DMX']
          },
          {
            categorie: 'Effets',
            types: ['Stroboscope', 'Machine Ã  fumÃ©e', 'Laser']
          }
        ]
      },
      {
        famille: 'VidÃ©o',
        categories: [
          {
            categorie: 'Projecteurs',
            types: ['VidÃ©oprojecteur', 'Ã‰cran LED']
          },
          {
            categorie: 'CamÃ©ras',
            types: ['CamÃ©ra PTZ', 'CamÃ©ra broadcast']
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
      { value: 'RESERVED', label: 'RÃ©servÃ©', color: '#8b5cf6' },
      { value: 'OUT_OF_ORDER', label: 'Hors service', color: '#ef4444' },
      { value: 'RETIRED', label: 'RetirÃ©', color: '#6b7280' }
    ]
  };

  // RÃ©cupÃ©rer la configuration des Ã©quipements
  getEquipmentConfig(): EquipmentConfig {
    // TODO: Charger depuis le backend
    return this.equipmentConfig;
  }

  // Sauvegarder la configuration
  async saveEquipmentConfig(config: EquipmentConfig): Promise<void> {
    // TODO: Sauvegarder vers le backend
    this.equipmentConfig = config;
    logger.debug('Configuration sauvegardÃ©e:', config);
  }

  // Obtenir toutes les familles
  getFamilles(): string[] {
    return this.equipmentConfig.hierarchies.map(h => h.famille);
  }

  // Obtenir les catÃ©gories d'une famille
  getCategories(famille: string): string[] {
    const hierarchy = this.equipmentConfig.hierarchies.find(h => h.famille === famille);
    return hierarchy ? hierarchy.categories.map(c => c.categorie) : [];
  }

  // Obtenir les types d'une catÃ©gorie
  getTypes(famille: string, categorie: string): string[] {
    const hierarchy = this.equipmentConfig.hierarchies.find(h => h.famille === famille);
    if (!hierarchy) return [];
    const cat = hierarchy.categories.find(c => c.categorie === categorie);
    return cat ? cat.types : [];
  }

  // Obtenir la hiÃ©rarchie complÃ¨te aplatie pour les filtres
  getFlatHierarchy(): { label: string; value: string; level: number }[] {
    const result: { label: string; value: string; level: number }[] = [];
    
    this.equipmentConfig.hierarchies.forEach(hierarchy => {
      result.push({
        label: `ðŸ“ ${hierarchy.famille}`,
        value: `famille:${hierarchy.famille}`,
        level: 0
      });
      
      hierarchy.categories.forEach(category => {
        result.push({
          label: `   ðŸ“‚ ${category.categorie}`,
          value: `categorie:${hierarchy.famille}/${category.categorie}`,
          level: 1
        });
        
        category.types.forEach((type, index, array) => {
          result.push({
            label: `      ðŸ“ ${type}`,
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
