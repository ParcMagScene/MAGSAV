/**
 * Traductions centralisées pour l'interface MAGSAV
 * Permet d'avoir une interface 100% française
 */

// ==================== STATUTS ====================

export const vehicleStatusTranslations: { [key: string]: string } = {
  'AVAILABLE': 'Disponible',
  'IN_USE': 'En utilisation',
  'MAINTENANCE': 'En maintenance',
  'OUT_OF_ORDER': 'Hors service',
  'RENTED_OUT': 'Loué externe',
  'RESERVED': 'Réservé'
};

export const equipmentStatusTranslations: { [key: string]: string } = {
  'AVAILABLE': 'Disponible',
  'IN_USE': 'En utilisation',
  'MAINTENANCE': 'Maintenance',
  'RESERVED': 'Réservé',
  'OUT_OF_ORDER': 'Hors service',
  'RETIRED': 'Retiré'
};

export const serviceRequestStatusTranslations: { [key: string]: string } = {
  // Nouveaux statuts (spec officielle)
  'PENDING': 'En attente',
  'VALIDATED': 'Validée',

  // Anciens statuts (compatibilité - à supprimer après migration des données)

  'IN_PROGRESS': 'En cours',
  'WAITING_PARTS': 'En attente de pièces',
  'RESOLVED': 'Résolue',
  'CLOSED': 'Fermée',
  'CANCELLED': 'Annulée',
  'EXTERNAL': 'Externe'
};

export const repairStatusTranslations: { [key: string]: string } = {
  'IN_PROGRESS': 'En cours',
  'WAITING_PARTS': 'En attente de pièces',
  'COMPLETED': 'Effectuée',
  'CANCELLED': 'Annulée'
};

export const rmaStatusTranslations: { [key: string]: string } = {
  'REQUEST_PENDING': 'Demande en cours',
  'REQUEST_VALIDATED': 'Demande validée',
  'SHIPPED': 'Expédiée',
  'RETURNED': 'Retournée',
  'REJECTED': 'Refusée'
};

export const reservationStatusTranslations: { [key: string]: string } = {
  'PENDING': 'En attente',
  'CONFIRMED': 'Confirmé',
  'IN_PROGRESS': 'En cours',
  'COMPLETED': 'Terminé',
  'CANCELLED': 'Annulé'
};

export const projectStatusTranslations: { [key: string]: string } = {
  'DRAFT': 'Brouillon',
  'PENDING': 'En attente',
  'IN_PROGRESS': 'En cours',
  'COMPLETED': 'Terminé',
  'CANCELLED': 'Annulé'
};

// ==================== PRIORITÉS ====================

export const priorityTranslations: { [key: string]: string } = {
  'LOW': 'Basse',
  'MEDIUM': 'Moyenne',
  'HIGH': 'Haute',
  'URGENT': 'Urgente'
};

// ==================== TYPES ====================

export const vehicleTypeTranslations: { [key: string]: string } = {
  'VAN': 'Fourgon',
  'VL': 'VL',
  'VL_17M3': 'VL 17m³',
  'VL_20M3': 'VL 20m³',
  'TRUCK': 'Camion',
  'PORTEUR': 'Porteur',
  'TRACTEUR': 'Tracteur',
  'SEMI_REMORQUE': 'Semi-remorque',
  'SCENE_MOBILE': 'Scène Mobile',
  'TRAILER': 'Remorque',
  'CAR': 'Voiture',
  'MOTORCYCLE': 'Moto',
  'OTHER': 'Autre'
};

export const serviceRequestTypeTranslations: { [key: string]: string } = {
  'REPAIR': 'Réparation',
  'MAINTENANCE': 'Maintenance',
  'INSTALLATION': 'Installation',
  'RMA': 'RMA',
  'TRAINING': 'Formation',
  'WARRANTY': 'Garantie'
};

export const fuelTypeTranslations: { [key: string]: string } = {
  'GASOLINE': 'Essence',
  'DIESEL': 'Diesel',
  'ELECTRIC': 'Électrique',
  'HYBRID': 'Hybride',
  'GPL': 'GPL',
  'OTHER': 'Autre'
};

// ==================== FONCTIONS UTILITAIRES ====================

/**
 * Traduit un statut en français
 */
export function translateStatus(status: string | undefined, type: 'vehicle' | 'equipment' | 'serviceRequest' | 'repair' | 'rma' | 'reservation' | 'project' = 'equipment'): string {
  if (!status) return '-';

  const translations = {
    vehicle: vehicleStatusTranslations,
    equipment: equipmentStatusTranslations,
    serviceRequest: serviceRequestStatusTranslations,
    repair: repairStatusTranslations,
    rma: rmaStatusTranslations,
    reservation: reservationStatusTranslations,
    project: projectStatusTranslations
  };

  return translations[type][status] || status;
}

/**
 * Traduit une priorité en français
 */
export function translatePriority(priority: string | undefined): string {
  if (!priority) return '-';
  return priorityTranslations[priority] || priority;
}

/**
 * Traduit un type de véhicule en français
 */
export function translateVehicleType(type: string | undefined): string {
  if (!type) return '-';
  return vehicleTypeTranslations[type] || type;
}

/**
 * Traduit un type de carburant en français
 */
export function translateFuelType(fuelType: string | undefined): string {
  if (!fuelType) return '-';
  return fuelTypeTranslations[fuelType] || fuelType;
}

/**
 * Traduit un type de demande SAV en français
 */
export function translateServiceRequestType(type: string | undefined): string {
  if (!type) return '-';
  return serviceRequestTypeTranslations[type] || type;
}

/**
 * Formate une date au format français
 */
export function formatFrenchDate(date: string | Date | undefined): string {
  if (!date) return '-';
  try {
    return new Date(date).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  } catch {
    return '-';
  }
}

/**
 * Formate une date et heure au format français
 */
export function formatFrenchDateTime(date: string | Date | undefined): string {
  if (!date) return '-';
  try {
    return new Date(date).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch {
    return '-';
  }
}
