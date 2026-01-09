import React, { useState, useEffect, useRef } from 'react';
import configService from '../services/config.service';
import './HierarchyFilter.css';

interface HierarchyFilterProps {
  value: string;
  onChange: (value: string) => void;
}

const HierarchyFilter: React.FC<HierarchyFilterProps> = ({ value, onChange }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [hierarchy, setHierarchy] = useState<{ label: string; value: string; level: number }[]>([]);
  const [dropdownPosition, setDropdownPosition] = useState({ top: 0, left: 0, width: 0 });
  const triggerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const flatHierarchy = configService.getFlatHierarchy();
    setHierarchy(flatHierarchy);
  }, []);

  useEffect(() => {
    if (isOpen && triggerRef.current) {
      const rect = triggerRef.current.getBoundingClientRect();
      setDropdownPosition({
        top: rect.bottom + 4,
        left: rect.left,
        width: rect.width
      });
    }
  }, [isOpen]);

  const getDisplayLabel = (value: string): string => {
    if (value === 'Tous') return '📁 Tous les équipements';
    const item = hierarchy.find(h => h.value === value);
    if (!item) return value;
    
    // Extraire juste le nom sans les caractères d'arbre
    const cleanLabel = item.label.replace(/[📁📂📝│├└─\s]+/g, '').trim();
    
    if (value.startsWith('famille:')) {
      return `📁 ${cleanLabel}`;
    } else if (value.startsWith('categorie:')) {
      const parts = value.split('/');
      const famille = parts[0].replace('categorie:', '');
      return `📂 ${famille} › ${cleanLabel}`;
    } else if (value.startsWith('type:')) {
      const parts = value.split('/');
      const famille = parts[0].replace('type:', '');
      return `📝 ${famille} › ${parts[1]} › ${cleanLabel}`;
    }
    
    return cleanLabel;
  };

  return (
    <div className="hierarchy-filter">
      <div
        ref={triggerRef}
        className="hierarchy-filter-trigger"
        onClick={() => setIsOpen(!isOpen)}
      >
        <span className="hierarchy-filter-value">
          {getDisplayLabel(value)}
        </span>
        <span className="hierarchy-filter-arrow">{isOpen ? '▲' : '▼'}</span>
      </div>

      {isOpen && (
        <>
          <div className="hierarchy-filter-backdrop" onClick={() => setIsOpen(false)} />
          <div 
            className="hierarchy-filter-dropdown"
            style={{
              top: `${dropdownPosition.top}px`,
              left: `${dropdownPosition.left}px`,
              width: `${dropdownPosition.width}px`
            }}
          >
            <div
              className={`hierarchy-filter-option ${value === 'Tous' ? 'selected' : ''}`}
              onClick={() => {
                onChange('Tous');
                setIsOpen(false);
              }}
            >
              Tous
            </div>
            {hierarchy.map((item, index) => (
              <div
                key={index}
                className={`hierarchy-filter-option level-${item.level} ${value === item.value ? 'selected' : ''}`}
                onClick={() => {
                  onChange(item.value);
                  setIsOpen(false);
                }}
              >
                {item.label}
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default HierarchyFilter;
