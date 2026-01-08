import React from 'react';
import './DataTable.css';

interface Column<T> {
    key: keyof T | string;
    label: string;
    render?: (value: any, item: T) => React.ReactNode;
    sortable?: boolean;
    width?: string;
}

interface DataTableProps<T> {
    data: T[];
    columns: Column<T>[];
    onRowClick?: (item: T) => void;
    selectedItem?: T | null;
    loading?: boolean;
    emptyMessage?: string;
    keyField?: keyof T;
}

function DataTable<T extends { id?: number | string }>({
    data,
    columns,
    onRowClick,
    selectedItem,
    loading = false,
    emptyMessage = 'Aucune donnée disponible',
    keyField = 'id' as keyof T,
}: DataTableProps<T>) {
    const getNestedValue = (obj: any, path: string): any => {
        return path.split('.').reduce((acc, part) => acc?.[part], obj);
    };

    if (loading) {
        return (
            <div className="data-table-loading">
                <div className="spinner"></div>
                <p>Chargement...</p>
            </div>
        );
    }

    if (!data || data.length === 0) {
        return (
            <div className="data-table-empty">
                <p>{emptyMessage}</p>
            </div>
        );
    }

    return (
        <div className="data-table-container">
            <table className="data-table">
                <thead>
                    <tr>
                        {columns.map((column, index) => (
                            <th
                                key={index}
                                style={{ width: column.width }}
                                className={column.sortable ? 'sortable' : ''}
                            >
                                {column.label}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {data.map((item) => {
                        const isSelected = selectedItem && item[keyField] === selectedItem[keyField];
                        return (
                            <tr
                                key={String(item[keyField])}
                                onClick={() => onRowClick?.(item)}
                                className={`${onRowClick ? 'clickable' : ''} ${isSelected ? 'selected' : ''}`}
                            >
                                {columns.map((column, colIndex) => {
                                    const value = getNestedValue(item, column.key as string);
                                    return (
                                        <td key={colIndex}>
                                            {column.render ? column.render(value, item) : String(value ?? '')}
                                        </td>
                                    );
                                })}
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
}

export default DataTable;
