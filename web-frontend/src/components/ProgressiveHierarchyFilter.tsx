import React, { useState, useEffect } from 'react';
import './ProgressiveHierarchyFilter.css';

interface ProgressiveHierarchyFilterProps {
  equipment: any[];
  onFilterChange: (famille: string, categorie: string, type: string) => void;
}

const ProgressiveHierarchyFilter: React.FC<ProgressiveHierarchyFilterProps> = ({ 
  equipment, 
  onFilterChange 
}) => {
  const [selectedFamille, setSelectedFamille] = useState<string>('Tous');
  const [selectedCategorie, setSelectedCategorie] = useState<string>('Tous');
  const [selectedType, setSelectedType] = useState<string>('Tous');
  
  const [categories, setCategories] = useState<string[]>([]);
  const [types, setTypes] = useState<string[]>([]);

  // Extraire les familles uniques (category = Famille dans le CSV)
  const familles = React.useMemo(() => {
    return Array.from(
      new Set(equipment.map(item => item.category).filter(Boolean))
    ).sort();
  }, [equipment]);

  // Extraire les catégories en fonction de la famille sélectionnée
  useEffect(() => {
    if (selectedFamille === 'Tous') {
      setCategories([]);
    } else {
      const filtered = equipment.filter(item => item.category === selectedFamille);
      const uniqueCategories = Array.from(
        new Set(filtered.map(item => item.subCategory).filter(Boolean))
      ).sort();
      setCategories(uniqueCategories);
    }
  }, [selectedFamille, equipment]);

  // Extraire les types en fonction de la famille et catégorie sélectionnées
  useEffect(() => {
    if (selectedFamille === 'Tous' || selectedCategorie === 'Tous') {
      setTypes([]);
    } else {
      const filtered = equipment.filter(
        item => item.category === selectedFamille && item.subCategory === selectedCategorie
      );
      const uniqueTypes = Array.from(
        new Set(filtered.map(item => item.specificCategory).filter(Boolean))
      ).sort();
      setTypes(uniqueTypes);
    }
  }, [selectedFamille, selectedCategorie, equipment]);

  const handleFamilleChange = (value: string) => {
    setSelectedFamille(value);
    setSelectedCategorie('Tous');
    setSelectedType('Tous');
    setCategories([]);
    setTypes([]);
    
    if (value !== 'Tous') {
      const filtered = equipment.filter(item => item.category === value);
      const uniqueCategories = Array.from(
        new Set(filtered.map(item => item.subCategory).filter(Boolean))
      ).sort();
      setCategories(uniqueCategories);
    }
    
    onFilterChange(value, 'Tous', 'Tous');
  };

  const handleCategorieChange = (value: string) => {
    setSelectedCategorie(value);
    setSelectedType('Tous');
    setTypes([]);
    
    if (value !== 'Tous') {
      const filtered = equipment.filter(
        item => item.category === selectedFamille && item.subCategory === value
      );
      const uniqueTypes = Array.from(
        new Set(filtered.map(item => item.specificCategory).filter(Boolean))
      ).sort();
      setTypes(uniqueTypes);
    }
    
    onFilterChange(selectedFamille, value, 'Tous');
  };

  const handleTypeChange = (value: string) => {
    setSelectedType(value);
    onFilterChange(selectedFamille, selectedCategorie, value);
  };

  return (
    <div className="progressive-hierarchy-filter">
      <div className="filter-step">
        <label className="filter-label">📁 Famille</label>
        <select 
          value={selectedFamille} 
          onChange={(e) => handleFamilleChange(e.target.value)}
          className="filter-select"
        >
          <option value="Tous">Tous</option>
          {familles.map(famille => (
            <option key={famille} value={famille}>{famille}</option>
          ))}
        </select>
      </div>

      {selectedFamille !== 'Tous' && (
        <div className="filter-step">
          <label className="filter-label">📂 Catégorie</label>
          <select 
            value={selectedCategorie} 
            onChange={(e) => handleCategorieChange(e.target.value)}
            className="filter-select"
          >
            <option value="Tous">Tous</option>
            {categories.map(categorie => (
              <option key={categorie} value={categorie}>{categorie}</option>
            ))}
          </select>
        </div>
      )}

      {selectedFamille !== 'Tous' && selectedCategorie !== 'Tous' && (
        <div className="filter-step">
          <label className="filter-label">📝 Type</label>
          <select 
            value={selectedType} 
            onChange={(e) => handleTypeChange(e.target.value)}
            className="filter-select"
          >
            <option value="Tous">Tous</option>
            {types.map(type => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
        </div>
      )}
    </div>
  );
};

export default ProgressiveHierarchyFilter;
