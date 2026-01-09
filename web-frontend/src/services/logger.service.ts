/**
 * Service de Logging CentralisÃ©
 * GÃ¨re les logs de maniÃ¨re intelligente selon l'environnement
 */

type LogLevel = 'debug' | 'info' | 'warn' | 'error';

interface LogConfig {
  enabled: boolean;
  levels: {
    debug: boolean;
    info: boolean;
    warn: boolean;
    error: boolean;
  };
  includeTimestamp: boolean;
  includeStackTrace: boolean;
}

class LoggerService {
  private config: LogConfig;

  constructor() {
    // Configuration basÃ©e sur l'environnement
    const isDevelopment = process.env.NODE_ENV === 'development';
    
    this.config = {
      enabled: isDevelopment,
      levels: {
        debug: isDevelopment,
        info: isDevelopment,
        warn: true,  // Toujours actif
        error: true, // Toujours actif
      },
      includeTimestamp: true,
      includeStackTrace: isDevelopment,
    };
  }

  /**
   * Configure le logger (optionnel)
   */
  configure(config: Partial<LogConfig>): void {
    this.config = { ...this.config, ...config };
  }

  /**
   * Formatte le message avec timestamp et emoji
   */
  private formatMessage(level: LogLevel, emoji: string, ...args: any[]): any[] {
    const timestamp = this.config.includeTimestamp 
      ? `[${new Date().toLocaleTimeString('fr-FR')}]` 
      : '';
    
    return [
      `${timestamp} ${emoji} [${level.toUpperCase()}]`,
      ...args
    ];
  }

  /**
   * Log niveau DEBUG
   * UtilisÃ© pour: DÃ©tails de dÃ©veloppement, debug dÃ©taillÃ©
   */
  debug(...args: any[]): void {
    if (this.config.enabled && this.config.levels.debug) {
      console.log(...this.formatMessage('debug', 'ðŸ”', ...args));
    }
  }

  /**
   * Log niveau INFO
   * UtilisÃ© pour: OpÃ©rations normales, confirmations
   */
  info(...args: any[]): void {
    if (this.config.enabled && this.config.levels.info) {
      console.log(...this.formatMessage('info', 'â„¹ï¸', ...args));
    }
  }

  /**
   * Log niveau WARN
   * UtilisÃ© pour: Avertissements, comportements inattendus non bloquants
   */
  warn(...args: any[]): void {
    if (this.config.enabled && this.config.levels.warn) {
      console.warn(...this.formatMessage('warn', 'âš ï¸', ...args));
    }
  }

  /**
   * Log niveau ERROR
   * UtilisÃ© pour: Erreurs, exceptions, problÃ¨mes critiques
   */
  error(...args: any[]): void {
    if (this.config.enabled && this.config.levels.error) {
      console.error(...this.formatMessage('error', 'âŒ', ...args));
      
      // Stack trace en dÃ©veloppement
      if (this.config.includeStackTrace) {
        console.trace();
      }
    }
  }

  /**
   * Log de requÃªte API
   */
  apiRequest(method: string, url: string, data?: any): void {
    this.debug('ðŸŒ API REQUEST', { method, url, data });
  }

  /**
   * Log de rÃ©ponse API
   */
  apiResponse(status: number, url: string, data?: any, dataLength?: number): void {
    this.debug('âœ… API RESPONSE', { method, url, status, data });
  }

  /**
   * Log d'erreur API
   */
  apiError(url: string, error: any, status?: number): void {
    this.error('âŒ API ERROR', { method, url, error: error.message || error });
  }

  /**
   * Log de navigation
   */
  navigation(from: string, to: string): void {
    this.info('ðŸ§­ NAVIGATION', { from, to });
  }

  /**
   * Log d'authentification
   */
  auth(action: string, details?: any): void {
    this.info('ðŸ” AUTH', action, details);
  }

  /**
   * Log de performance
   */
  performance(label: string, duration: number): void {
    const emoji = duration < 100 ? 'âš¡' : duration < 500 ? 'ðŸ¢' : 'ðŸŒ';
    this.debug(`${emoji} PERFORMANCE`, `${label}: ${duration}ms`);
  }

  /**
   * Grouper des logs (utile pour tracer une opÃ©ration complexe)
   */
  group(label: string, collapsed: boolean = false): void {
    if (this.config.enabled) {
      collapsed ? console.groupCollapsed(label) : console.group(label);
    }
  }

  /**
   * Terminer un groupe de logs
   */
  groupEnd(): void {
    if (this.config.enabled) {
      console.groupEnd();
    }
  }

  /**
   * Afficher un tableau (utile pour les donnÃ©es structurÃ©es)
   */
  table(data: any): void {
    if (this.config.enabled && this.config.levels.debug) {
      console.table(data);
    }
  }
}

// Instance singleton
const logger = new LoggerService();

// Export par dÃ©faut
export default logger;

// Exports nommÃ©s pour utilisation directe
export const { debug, info, warn, error, apiRequest, apiResponse, apiError } = {
  debug: logger.debug.bind(logger),
  info: logger.info.bind(logger),
  warn: logger.warn.bind(logger),
  error: logger.error.bind(logger),
  apiRequest: logger.apiRequest.bind(logger),
  apiResponse: logger.apiResponse.bind(logger),
  apiError: logger.apiError.bind(logger),
};

