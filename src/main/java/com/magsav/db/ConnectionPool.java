package com.magsav.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pool de connexions SQLite optimisé pour MAGSAV
 * Améliore les performances en réutilisant les connexions
 */
public class ConnectionPool {
    private static final int DEFAULT_POOL_SIZE = 10;
    private static final int MAX_WAIT_SECONDS = 30;
    
    private final BlockingQueue<Connection> availableConnections;
    private final AtomicInteger totalConnections;
    private final AtomicBoolean isShutdown;
    private final String databaseUrl;
    private final int maxPoolSize;
    
    private static volatile ConnectionPool instance;
    
    private ConnectionPool(String databaseUrl, int poolSize) {
        this.databaseUrl = databaseUrl;
        this.maxPoolSize = poolSize;
        this.availableConnections = new LinkedBlockingQueue<>(poolSize);
        this.totalConnections = new AtomicInteger(0);
        this.isShutdown = new AtomicBoolean(false);
        
        // Pré-créer quelques connexions
        initializePool();
    }
    
    public static ConnectionPool getInstance(String databaseUrl) {
        if (instance == null) {
            synchronized (ConnectionPool.class) {
                if (instance == null) {
                    instance = new ConnectionPool(databaseUrl, DEFAULT_POOL_SIZE);
                }
            }
        }
        return instance;
    }
    
    private void initializePool() {
        // Créer 3 connexions initiales
        for (int i = 0; i < Math.min(3, maxPoolSize); i++) {
            try {
                Connection conn = createNewConnection();
                availableConnections.offer(conn);
                totalConnections.incrementAndGet();
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'initialisation du pool: " + e.getMessage());
            }
        }
    }
    
    private Connection createNewConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(databaseUrl);
        // Optimisations SQLite
        try (var stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA synchronous=NORMAL");
            stmt.execute("PRAGMA cache_size=10000");
            stmt.execute("PRAGMA temp_store=MEMORY");
            stmt.execute("PRAGMA mmap_size=268435456"); // 256MB
        }
        return conn;
    }
    
    public Connection getConnection() throws SQLException {
        if (isShutdown.get()) {
            throw new SQLException("Le pool de connexions est fermé");
        }
        
        // Essayer de récupérer une connexion disponible
        Connection conn = availableConnections.poll();
        
        if (conn != null && isConnectionValid(conn)) {
            return new PooledConnection(conn, this);
        }
        
        // Si pas de connexion disponible, créer une nouvelle si possible
        if (totalConnections.get() < maxPoolSize) {
            try {
                conn = createNewConnection();
                totalConnections.incrementAndGet();
                return new PooledConnection(conn, this);
            } catch (SQLException e) {
                System.err.println("Erreur lors de la création d'une nouvelle connexion: " + e.getMessage());
            }
        }
        
        // Attendre qu'une connexion se libère
        try {
            conn = availableConnections.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            if (conn != null && isConnectionValid(conn)) {
                return new PooledConnection(conn, this);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interruption lors de l'attente d'une connexion", e);
        }
        
        throw new SQLException("Impossible d'obtenir une connexion dans les " + MAX_WAIT_SECONDS + " secondes");
    }
    
    private boolean isConnectionValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }
    
    public void returnConnection(Connection conn) {
        if (conn != null && !isShutdown.get() && isConnectionValid(conn)) {
            availableConnections.offer(conn);
        } else {
            // Connexion invalide, la remplacer
            totalConnections.decrementAndGet();
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                // Ignorer les erreurs de fermeture
            }
        }
    }
    
    public void shutdown() {
        isShutdown.set(true);
        
        // Fermer toutes les connexions
        Connection conn;
        while ((conn = availableConnections.poll()) != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Ignorer les erreurs de fermeture
            }
        }
        
        totalConnections.set(0);
    }
    
    public int getActiveConnections() {
        return totalConnections.get() - availableConnections.size();
    }
    
    public int getAvailableConnections() {
        return availableConnections.size();
    }
    
    public int getTotalConnections() {
        return totalConnections.get();
    }
    
    public static class Stats {
        private final int active;
        private final int available;
        private final int total;
        
        public Stats(int active, int available, int total) {
            this.active = active;
            this.available = available;
            this.total = total;
        }
        
        public int getActive() { return active; }
        public int getAvailable() { return available; }
        public int getTotal() { return total; }
        
        @Override
        public String toString() {
            return String.format("Connexions: %d actives, %d disponibles, %d total", 
                active, available, total);
        }
    }
}