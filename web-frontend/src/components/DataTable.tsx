import React, { useState } from 'react';
import ContextMenu from './ContextMenu';
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
    onRowDoubleClick?: (item: T) => void;
    onEdit?: (item: T) => void;
    onRowContextMenu?: (item: T, event: React.MouseEvent) => void;
    selectedItem?: T | null;
    highlightedRowId?: number | null;
    loading?: boolean;
    emptyMessage?: string;
    keyField?: keyof T;
}

function DataTable<T extends { id?: number | string }>({
    data,
    columns,
    onRowClick,
    onRowDoubleClick,
    onEdit,
    onRowContextMenu,
    selectedItem,
    highlightedRowId,
    loading = false,
    emptyMessage = 'Aucune donnée disponible',
    keyField = 'id' as keyof T
}: DataTableProps<T>) {
    const [contextMenu, setContextMenu] = useState<{ x: number; y: number; item: T } | null>(null);

    const getNestedValue = (obj: any, path: string): any => {
        return path.split('.').reduce((acc, part) => acc?.[part], obj);
    };

    const handleClick = (item: T) => {
        onRowClick?.(item);
    };

    const handleDoubleClick = (item: T) => {
        onRowDoubleClick?.(item);
    };

    const handleContextMenu = (item: T, event: React.MouseEvent) => {
        if (onRowContextMenu) {
            // Si onRowContextMenu est fourni, l'utiliser directement (logique personnalisée)
            onRowContextMenu(item, event);
        } else if (onEdit) {
            // Sinon, utiliser le comportement par défaut avec le menu contextuel standard
            event.preventDefault();
            setContextMenu({ x: event.clientX, y: event.clientY, item });
        }
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
        <>
            <div className="data-table-wrapper">
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
                            const isHighlighted = highlightedRowId && item.id === highlightedRowId;
                            return (
                                <tr
                                    key={String(item[keyField])}
                                    onClick={() => handleClick(item)}
                                    onDoubleClick={() => handleDoubleClick(item)}
                                    onContextMenu={(e) => handleContextMenu(item, e)}
                                    className={`${onRowClick || onRowDoubleClick || onEdit ? 'clickable' : ''} ${isSelected ? 'selected' : ''} ${isHighlighted ? 'highlighted' : ''}`}
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

            {contextMenu && (
                <ContextMenu
                    items={[
                        {
                            label: 'Modifier',
                            icon: '✏️',
                            onClick: () => onEdit!(contextMenu.item)
                        }
                    ]}
                    position={{ x: contextMenu.x, y: contextMenu.y }}
                    onClose={() => setContextMenu(null)}
                />
            )}
        </>
    );
}

export default DataTable;