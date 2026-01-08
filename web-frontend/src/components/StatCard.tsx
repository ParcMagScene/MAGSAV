import React from 'react';
import './StatCard.css';

interface StatCardProps {
    icon: string;
    title: string;
    value: number | string;
    subtitle?: string;
    color?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
    onClick?: () => void;
}

const StatCard: React.FC<StatCardProps> = ({
    icon,
    title,
    value,
    subtitle,
    color = 'primary',
    onClick,
}) => {
    return (
        <div
            className={`stat-card stat-card-${color} ${onClick ? 'clickable' : ''}`}
            onClick={onClick}
        >
            <div className="stat-card-icon">{icon}</div>
            <div className="stat-card-content">
                <h3 className="stat-card-title">{title}</h3>
                <div className="stat-card-value">{value}</div>
                {subtitle && <div className="stat-card-subtitle">{subtitle}</div>}
            </div>
        </div>
    );
};

export default StatCard;
