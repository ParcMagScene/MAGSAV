import React, { useState, useEffect } from 'react';
import { usePageContext } from '../contexts/PageContext';
import './Settings.css';

type TabType = 'appearance' | 'language' | 'display' | 'referentials' | 'security' | 'about';

const Settings: React.FC = () => {
    const { setPageTitle } = usePageContext();
    const [activeTab, setActiveTab] = useState<TabType>('appearance');
    const [theme, setTheme] = useState(localStorage.getItem('theme') || 'light');
    const [language, setLanguage] = useState('fr');

    useEffect(() => {
        setPageTitle('⚙ Paramètres');
    }, [setPageTitle]);

    const handleThemeChange = (newTheme: string) => {
        setTheme(newTheme);
        localStorage.setItem('theme', newTheme);
        document.documentElement.setAttribute('data-theme', newTheme);
    };

    return (
        <div className="settings-page">
            <div className="tabs-header tabs-header-only">
                <button
                    className={`tab-button ${activeTab === 'appearance' ? 'active' : ''}`}
                    onClick={() => setActiveTab('appearance')}
                >
                    🎨 Apparence
                </button>
                <button
                    className={`tab-button ${activeTab === 'language' ? 'active' : ''}`}
                    onClick={() => setActiveTab('language')}
                >
                    🌍 Langue
                </button>
                <button
                    className={`tab-button ${activeTab === 'display' ? 'active' : ''}`}
                    onClick={() => setActiveTab('display')}
                >
                    📊 Affichage
                </button>
                <button
                    className={`tab-button ${activeTab === 'referentials' ? 'active' : ''}`}
                    onClick={() => setActiveTab('referentials')}
                >
                    📋 Référentiels
                </button>
                <button
                    className={`tab-button ${activeTab === 'security' ? 'active' : ''}`}
                    onClick={() => setActiveTab('security')}
                >
                    🔐 Sécurité
                </button>
                <button
                    className={`tab-button ${activeTab === 'about' ? 'active' : ''}`}
                    onClick={() => setActiveTab('about')}
                >
                    ℹ️ À propos
                </button>
            </div>

            <div className="tab-content">
                {activeTab === 'appearance' && (
                    <div className="settings-section">
                        <div className="setting-item">
                            <label>Thème</label>
                            <div className="theme-selector">
                                <button
                                    className={`theme-btn theme-light ${theme === 'light' ? 'active' : ''}`}
                                    onClick={() => handleThemeChange('light')}
                                >
                                    ☀️ Clair
                                </button>
                                <button
                                    className={`theme-btn theme-dark ${theme === 'dark' ? 'active' : ''}`}
                                    onClick={() => handleThemeChange('dark')}
                                >
                                    🌙 Sombre
                                </button>
                                <button
                                    className={`theme-btn theme-blue ${theme === 'blue' ? 'active' : ''}`}
                                    onClick={() => handleThemeChange('blue')}
                                >
                                    💙 Bleu
                                </button>
                                <button
                                    className={`theme-btn theme-green ${theme === 'green' ? 'active' : ''}`}
                                    onClick={() => handleThemeChange('green')}
                                >
                                    💚 Vert
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {activeTab === 'language' && (
                    <div className="settings-section">
                        <div className="setting-item">
                            <label>Langue de l'interface</label>
                            <select
                                className="settings-select"
                                value={language}
                                onChange={(e) => setLanguage(e.target.value)}
                            >
                                <option value="fr">Français</option>
                                <option value="en">English</option>
                                <option value="es">Español</option>
                            </select>
                        </div>
                    </div>
                )}

                {activeTab === 'display' && (
                    <div className="settings-section">
                        <div className="setting-item">
                            <label>
                                <input type="checkbox" defaultChecked />
                                Afficher les statistiques sur le dashboard
                            </label>
                        </div>
                        <div className="setting-item">
                            <label>
                                <input type="checkbox" defaultChecked />
                                Notifications en temps réel
                            </label>
                        </div>
                        <div className="setting-item">
                            <label>
                                <input type="checkbox" />
                                Mode compact pour les tables
                            </label>
                        </div>
                    </div>
                )}

                {activeTab === 'referentials' && (
                    <div className="settings-section">
                        <h3>📦 Équipements - Hiérarchie Famille / Catégorie / Type</h3>
                        <p className="section-description">
                            Gérez la classification hiérarchique de votre parc matériel.
                        </p>
                        <div className="referentials-equipment">
                            <button className="btn btn-primary">
                                ➕ Ajouter une Famille
                            </button>
                            <div className="hierarchy-list">
                                <p className="placeholder-text">
                                    Interface de gestion des hiérarchies (à venir)
                                </p>
                            </div>
                        </div>

                        <h3 style={{marginTop: '32px'}}>🏷️ Équipements - Statuts</h3>
                        <p className="section-description">
                            Définissez les statuts disponibles pour les équipements.
                        </p>
                        <div className="referentials-statuses">
                            <button className="btn btn-primary">
                                ➕ Ajouter un Statut
                            </button>
                            <div className="status-list">
                                <p className="placeholder-text">
                                    Interface de gestion des statuts (à venir)
                                </p>
                            </div>
                        </div>
                    </div>
                )}

                {activeTab === 'security' && (
                    <div className="settings-section">
                        <div className="setting-item">
                            <button className="btn-secondary">Changer le mot de passe</button>
                        </div>
                        <div className="setting-item">
                            <button className="btn-secondary">Gérer les sessions actives</button>
                        </div>
                    </div>
                )}

                {activeTab === 'about' && (
                    <div className="settings-section">
                        <div className="about-info">
                            <p><strong>MAGSAV-3.0</strong></p>
                            <p>Version: 3.0.0</p>
                            <p>© 2026 Mag Scène - Tous droits réservés</p>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Settings;
