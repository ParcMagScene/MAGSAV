package com.magsav.service;

import com.magsav.db.DB;
import com.magsav.util.AppLogger;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service pour gérer le workflow de validation des demandes vers les commandes fournisseurs
 */
public class RequestToOrderWorkflowService {
    
    private static RequestToOrderWorkflowService instance;
    
    public static synchronized RequestToOrderWorkflowService getInstance() {
        if (instance == null) {
            instance = new RequestToOrderWorkflowService();
        }
        return instance;
    }
    
    /**
     * Valide une demande de pièces/matériel et crée/met à jour les commandes fournisseurs
     * @param requestId ID de la demande
     * @param validatorUserId ID de l'administrateur qui valide
     * @return true si validation réussie
     */
    public boolean validateRequestAndCreateOrders(Long requestId, String validatorUserId) {
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // 1. Valider la demande
                if (!updateRequestStatus(conn, requestId, "VALIDEE", validatorUserId)) {
                    conn.rollback();
                    return false;
                }
                
                // 2. Récupérer les items de la demande groupés par fournisseur
                Map<Long, List<RequestItem>> itemsBySupplier = getRequestItemsGroupedBySupplier(conn, requestId);
                
                // 3. Pour chaque fournisseur, créer ou mettre à jour une commande
                for (Map.Entry<Long, List<RequestItem>> entry : itemsBySupplier.entrySet()) {
                    Long supplierId = entry.getKey();
                    List<RequestItem> items = entry.getValue();
                    
                    if (supplierId != null) {
                        processSupplierOrder(conn, supplierId, items, requestId);
                    } else {
                        AppLogger.warn("Items sans fournisseur dans la demande " + requestId);
                    }
                }
                
                conn.commit();
                AppLogger.info("Demande " + requestId + " validée et commandes créées/mises à jour");
                return true;
                
            } catch (Exception e) {
                conn.rollback();
                AppLogger.error("Erreur lors de la validation de la demande " + requestId, e);
                return false;
            }
            
        } catch (SQLException e) {
            AppLogger.error("Erreur de connexion lors de la validation de la demande " + requestId, e);
            return false;
        }
    }
    
    /**
     * Met à jour le statut d'une demande
     */
    private boolean updateRequestStatus(Connection conn, Long requestId, String status, String validatorUserId) {
        String sql = "UPDATE requests SET status = ?, validated_at = ?, assigned_to = ?, updated_at = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            ps.setString(3, validatorUserId);
            ps.setString(4, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            ps.setLong(5, requestId);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            AppLogger.error("Erreur lors de la mise à jour du statut de la demande " + requestId, e);
            return false;
        }
    }
    
    /**
     * Récupère les items d'une demande groupés par fournisseur
     */
    private Map<Long, List<RequestItem>> getRequestItemsGroupedBySupplier(Connection conn, Long requestId) {
        String sql = "SELECT id, item_type, reference, name, description, quantity, unit_price, " +
                    "total_price, supplier_id, status, notes FROM request_items WHERE request_id = ?";
        
        Map<Long, List<RequestItem>> result = new HashMap<>();
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, requestId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RequestItem item = new RequestItem(
                        rs.getLong("id"),
                        requestId,
                        rs.getString("item_type"),
                        rs.getString("reference"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("total_price"),
                        rs.getLong("supplier_id") == 0 ? null : rs.getLong("supplier_id"),
                        rs.getString("status"),
                        rs.getString("notes")
                    );
                    
                    Long supplierId = item.supplierId();
                    result.computeIfAbsent(supplierId, k -> new ArrayList<>()).add(item);
                }
            }
            
        } catch (SQLException e) {
            AppLogger.error("Erreur lors de la récupération des items de la demande " + requestId, e);
        }
        
        return result;
    }
    
    /**
     * Traite une commande pour un fournisseur spécifique
     */
    private void processSupplierOrder(Connection conn, Long supplierId, List<RequestItem> items, Long requestId) {
        try {
            // Chercher une commande existante en brouillon pour ce fournisseur
            Long orderId = findOrCreateDraftOrder(conn, supplierId);
            
            if (orderId != null) {
                // Ajouter les items à la commande
                addItemsToOrder(conn, orderId, items);
                
                // Mettre à jour le montant total de la commande
                updateOrderTotal(conn, orderId);
                
                // Marquer les items comme commandés
                markItemsAsOrdered(conn, items);
                
                AppLogger.info("Items ajoutés à la commande " + orderId + " pour le fournisseur " + supplierId);
            }
            
        } catch (SQLException e) {
            AppLogger.error("Erreur lors du traitement de la commande pour le fournisseur " + supplierId, e);
        }
    }
    
    /**
     * Trouve une commande en brouillon existante ou en crée une nouvelle
     */
    private Long findOrCreateDraftOrder(Connection conn, Long supplierId) throws SQLException {
        // Chercher une commande existante en brouillon
        String findSql = "SELECT id FROM commandes WHERE fournisseur_id = ? AND statut = 'BROUILLON' LIMIT 1";
        
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setLong(1, supplierId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        
        // Créer une nouvelle commande
        return createNewDraftOrder(conn, supplierId);
    }
    
    /**
     * Crée une nouvelle commande en brouillon
     */
    private Long createNewDraftOrder(Connection conn, Long supplierId) throws SQLException {
        String orderNumber = generateOrderNumber();
        String sql = "INSERT INTO commandes (numero_commande, fournisseur_id, statut, date_commande) VALUES (?, ?, 'BROUILLON', ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, orderNumber);
            ps.setLong(2, supplierId);
            ps.setString(3, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Long orderId = keys.getLong(1);
                    AppLogger.info("Nouvelle commande créée: " + orderNumber + " (ID: " + orderId + ")");
                    return orderId;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Génère un numéro de commande unique
     */
    private String generateOrderNumber() {
        return "CMD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }
    
    /**
     * Ajoute des items à une commande (via lignes_commandes)
     */
    private void addItemsToOrder(Connection conn, Long orderId, List<RequestItem> items) throws SQLException {
        String sql = "INSERT INTO lignes_commandes (commande_id, reference_produit, nom_produit, description, " +
                    "quantite, prix_unitaire, prix_total, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (RequestItem item : items) {
                ps.setLong(1, orderId);
                ps.setString(2, item.reference());
                ps.setString(3, item.name());
                ps.setString(4, item.description());
                ps.setInt(5, item.quantity());
                ps.setDouble(6, item.unitPrice());
                ps.setDouble(7, item.totalPrice());
                ps.setString(8, item.notes());
                
                ps.addBatch();
            }
            
            ps.executeBatch();
        }
    }
    
    /**
     * Met à jour le montant total d'une commande
     */
    private void updateOrderTotal(Connection conn, Long orderId) throws SQLException {
        String sql = "UPDATE commandes SET montant_ht = (SELECT SUM(prix_total) FROM lignes_commandes WHERE commande_id = ?), " +
                    "montant_ttc = montant_ht * 1.20, montant_tva = montant_ttc - montant_ht WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setLong(2, orderId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Marque les items comme commandés
     */
    private void markItemsAsOrdered(Connection conn, List<RequestItem> items) throws SQLException {
        String sql = "UPDATE request_items SET status = 'COMMANDE', updated_at = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            for (RequestItem item : items) {
                ps.setString(1, now);
                ps.setLong(2, item.id());
                ps.addBatch();
            }
            
            ps.executeBatch();
        }
    }
    
    /**
     * Récupère les demandes en attente de validation
     */
    public List<PendingRequest> getPendingRequests() {
        List<PendingRequest> requests = new ArrayList<>();
        String sql = "SELECT r.id, r.type, r.title, r.description, r.status, r.priority, r.requester_name, " +
                    "r.estimated_cost, r.created_at, COUNT(ri.id) as item_count " +
                    "FROM requests r LEFT JOIN request_items ri ON r.id = ri.request_id " +
                    "WHERE r.status = 'EN_ATTENTE' AND r.type IN ('PIECES', 'MATERIEL') " +
                    "GROUP BY r.id ORDER BY r.created_at DESC";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                requests.add(new PendingRequest(
                    rs.getLong("id"),
                    rs.getString("type"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("priority"),
                    rs.getString("requester_name"),
                    rs.getDouble("estimated_cost"),
                    rs.getString("created_at"),
                    rs.getInt("item_count")
                ));
            }
            
        } catch (SQLException e) {
            AppLogger.error("Erreur lors de la récupération des demandes en attente", e);
        }
        
        return requests;
    }
    
    /**
     * Record pour représenter un item de demande
     */
    public record RequestItem(
        Long id,
        Long requestId,
        String itemType,
        String reference,
        String name,
        String description,
        int quantity,
        double unitPrice,
        double totalPrice,
        Long supplierId,
        String status,
        String notes
    ) {}
    
    /**
     * Record pour représenter une demande en attente
     */
    public record PendingRequest(
        Long id,
        String type,
        String title,
        String description,
        String status,
        String priority,
        String requesterName,
        double estimatedCost,
        String createdAt,
        int itemCount
    ) {}
}