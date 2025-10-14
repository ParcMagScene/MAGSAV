package com.magsav.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Wrapper pour une connexion poolée qui la retourne automatiquement au pool
 */
public class PooledConnection implements Connection {
    private final Connection delegate;
    private final ConnectionPool pool;
    private boolean closed = false;
    
    public PooledConnection(Connection delegate, ConnectionPool pool) {
        this.delegate = delegate;
        this.pool = pool;
    }
    
    @Override
    public void close() throws SQLException {
        if (!closed) {
            closed = true;
            pool.returnConnection(delegate);
        }
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return closed || delegate.isClosed();
    }
    
    // Déléguer toutes les autres méthodes à la connexion réelle
    @Override
    public java.sql.Statement createStatement() throws SQLException {
        checkClosed();
        return delegate.createStatement();
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(String sql) throws SQLException {
        checkClosed();
        return delegate.prepareStatement(sql);
    }
    
    @Override
    public java.sql.CallableStatement prepareCall(String sql) throws SQLException {
        checkClosed();
        return delegate.prepareCall(sql);
    }
    
    @Override
    public String nativeSQL(String sql) throws SQLException {
        checkClosed();
        return delegate.nativeSQL(sql);
    }
    
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkClosed();
        delegate.setAutoCommit(autoCommit);
    }
    
    @Override
    public boolean getAutoCommit() throws SQLException {
        checkClosed();
        return delegate.getAutoCommit();
    }
    
    @Override
    public void commit() throws SQLException {
        checkClosed();
        delegate.commit();
    }
    
    @Override
    public void rollback() throws SQLException {
        checkClosed();
        delegate.rollback();
    }
    
    @Override
    public java.sql.DatabaseMetaData getMetaData() throws SQLException {
        checkClosed();
        return delegate.getMetaData();
    }
    
    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        checkClosed();
        delegate.setReadOnly(readOnly);
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        checkClosed();
        return delegate.isReadOnly();
    }
    
    @Override
    public void setCatalog(String catalog) throws SQLException {
        checkClosed();
        delegate.setCatalog(catalog);
    }
    
    @Override
    public String getCatalog() throws SQLException {
        checkClosed();
        return delegate.getCatalog();
    }
    
    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        checkClosed();
        delegate.setTransactionIsolation(level);
    }
    
    @Override
    public int getTransactionIsolation() throws SQLException {
        checkClosed();
        return delegate.getTransactionIsolation();
    }
    
    @Override
    public java.sql.SQLWarning getWarnings() throws SQLException {
        checkClosed();
        return delegate.getWarnings();
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        checkClosed();
        delegate.clearWarnings();
    }
    
    @Override
    public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        checkClosed();
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        checkClosed();
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        checkClosed();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public java.util.Map<String, Class<?>> getTypeMap() throws SQLException {
        checkClosed();
        return delegate.getTypeMap();
    }
    
    @Override
    public void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException {
        checkClosed();
        delegate.setTypeMap(map);
    }
    
    @Override
    public void setHoldability(int holdability) throws SQLException {
        checkClosed();
        delegate.setHoldability(holdability);
    }
    
    @Override
    public int getHoldability() throws SQLException {
        checkClosed();
        return delegate.getHoldability();
    }
    
    @Override
    public java.sql.Savepoint setSavepoint() throws SQLException {
        checkClosed();
        return delegate.setSavepoint();
    }
    
    @Override
    public java.sql.Savepoint setSavepoint(String name) throws SQLException {
        checkClosed();
        return delegate.setSavepoint(name);
    }
    
    @Override
    public void rollback(java.sql.Savepoint savepoint) throws SQLException {
        checkClosed();
        delegate.rollback(savepoint);
    }
    
    @Override
    public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException {
        checkClosed();
        delegate.releaseSavepoint(savepoint);
    }
    
    @Override
    public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        checkClosed();
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        checkClosed();
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        checkClosed();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        checkClosed();
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        checkClosed();
        return delegate.prepareStatement(sql, columnIndexes);
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        checkClosed();
        return delegate.prepareStatement(sql, columnNames);
    }
    
    @Override
    public java.sql.Clob createClob() throws SQLException {
        checkClosed();
        return delegate.createClob();
    }
    
    @Override
    public java.sql.Blob createBlob() throws SQLException {
        checkClosed();
        return delegate.createBlob();
    }
    
    @Override
    public java.sql.NClob createNClob() throws SQLException {
        checkClosed();
        return delegate.createNClob();
    }
    
    @Override
    public java.sql.SQLXML createSQLXML() throws SQLException {
        checkClosed();
        return delegate.createSQLXML();
    }
    
    @Override
    public boolean isValid(int timeout) throws SQLException {
        return !closed && delegate.isValid(timeout);
    }
    
    @Override
    public void setClientInfo(String name, String value) throws java.sql.SQLClientInfoException {
        try {
            checkClosed();
            delegate.setClientInfo(name, value);
        } catch (SQLException e) {
            throw new java.sql.SQLClientInfoException();
        }
    }
    
    @Override
    public void setClientInfo(java.util.Properties properties) throws java.sql.SQLClientInfoException {
        try {
            checkClosed();
            delegate.setClientInfo(properties);
        } catch (SQLException e) {
            throw new java.sql.SQLClientInfoException();
        }
    }
    
    @Override
    public String getClientInfo(String name) throws SQLException {
        checkClosed();
        return delegate.getClientInfo(name);
    }
    
    @Override
    public java.util.Properties getClientInfo() throws SQLException {
        checkClosed();
        return delegate.getClientInfo();
    }
    
    @Override
    public java.sql.Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        checkClosed();
        return delegate.createArrayOf(typeName, elements);
    }
    
    @Override
    public java.sql.Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        checkClosed();
        return delegate.createStruct(typeName, attributes);
    }
    
    @Override
    public void setSchema(String schema) throws SQLException {
        checkClosed();
        delegate.setSchema(schema);
    }
    
    @Override
    public String getSchema() throws SQLException {
        checkClosed();
        return delegate.getSchema();
    }
    
    @Override
    public void abort(java.util.concurrent.Executor executor) throws SQLException {
        checkClosed();
        delegate.abort(executor);
    }
    
    @Override
    public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) throws SQLException {
        checkClosed();
        delegate.setNetworkTimeout(executor, milliseconds);
    }
    
    @Override
    public int getNetworkTimeout() throws SQLException {
        checkClosed();
        return delegate.getNetworkTimeout();
    }
    
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        checkClosed();
        return delegate.unwrap(iface);
    }
    
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        checkClosed();
        return delegate.isWrapperFor(iface);
    }
    
    private void checkClosed() throws SQLException {
        if (closed) {
            throw new SQLException("Connexion fermée");
        }
    }
}