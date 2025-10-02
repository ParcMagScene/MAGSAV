// JS pour la page Recherche Avancée
(function() {
  let currentPage = 0;
  let currentSize = 20;
  let currentSort = 'id_desc';
  let currentView = 'card';
  let currentFilters = {};
  let searchData = [];
  let isAdvancedMode = false;

  const $ = (sel) => document.querySelector(sel);
  const $$ = (sel) => Array.from(document.querySelectorAll(sel));

  document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    loadInitialData();
  });

  function setupEventListeners() {
    // Modes
    $('#simpleMode').addEventListener('click', (e) => { e.preventDefault(); toggleMode(false); });
    $('#advancedMode').addEventListener('click', (e) => { e.preventDefault(); toggleMode(true); });

    // Recherche simple
    $('#searchInput').addEventListener('input', debounce(performSearch, 300));
    $('#searchBtn').addEventListener('click', (e) => { e.preventDefault(); performSearch(); });

    // Filtres rapides
    $$('.filter-chip').forEach(chip => chip.addEventListener('click', () => toggleQuickFilter(chip.dataset.filter)));

    // Recherche avancée
    $('#advancedSearchForm').addEventListener('submit', handleAdvancedSearch);
    $('#clearFilters').addEventListener('click', (e) => { e.preventDefault(); clearAllFilters(); });

    // Vues
    $('#cardView').addEventListener('click', (e) => { e.preventDefault(); setView('card'); });
    $('#tableView').addEventListener('click', (e) => { e.preventDefault(); setView('table'); });
    $('#sortSelect').addEventListener('change', handleSortChange);

    // Export
    $('#exportBtn').addEventListener('click', handleExport);
  }

  function toggleMode(advanced) {
    isAdvancedMode = advanced;
    $$('.search-mode-btn').forEach(btn => btn.classList.remove('active'));
    document.getElementById(advanced ? 'advancedMode' : 'simpleMode').classList.add('active');
    const adv = $('#advancedFilters');
    adv.classList.toggle('show', advanced);
  }

  function toggleQuickFilter(filter) {
    $$('.filter-chip').forEach(chip => chip.classList.remove('active'));
    document.querySelector(`[data-filter="${filter}"]`).classList.add('active');
    currentFilters = filter === 'all' ? {} : { statut: filter };
    currentPage = 0;
    performSearch();
  }

  function performSearch() {
    const searchTerm = $('#searchInput').value.trim();
    showLoading(true);

    const params = new URLSearchParams({
      page: currentPage,
      size: currentSize,
      sortBy: currentSort.split('_')[0],
      sortDir: currentSort.split('_')[1] || 'asc'
    });
    if (searchTerm) params.append('search', searchTerm);
    Object.keys(currentFilters).forEach(k => currentFilters[k] && params.append(k, currentFilters[k]));

    fetch(`/api/search/dossiers?${params.toString()}`)
      .then(r => r.json())
      .then(data => {
        searchData = data.content || [];
        displayResults(data);
        updatePagination(data);
        showLoading(false);
      })
      .catch(err => { console.error('Erreur de recherche:', err); showError('Erreur lors de la recherche'); showLoading(false); });
  }

  function handleAdvancedSearch(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    currentFilters = {};
    for (let [k, v] of formData.entries()) {
      if (typeof v === 'string' && v.trim()) currentFilters[k] = v.trim();
    }
    currentPage = 0;
    performSearch();
  }

  function displayResults(data) {
    const resultsHeader = $('#resultsHeader');
    const emptyState = $('#emptyState');

    if (data.content && data.content.length > 0) {
      resultsHeader.classList.remove('d-none');
      emptyState.classList.add('d-none');
      $('#resultsCount').textContent = data.totalElements;
      if (currentView === 'card') displayCardResults(data.content); else displayTableResults(data.content);
    } else {
      resultsHeader.classList.add('d-none');
      $('#cardResults').innerHTML = '';
      $('#tableBody').innerHTML = '';
      emptyState.classList.remove('d-none');
    }
  }

  function displayCardResults(results) {
    const container = $('#cardResults');
    container.innerHTML = '';
    results.forEach(d => container.appendChild(createResultCard(d)));
    $('#cardResults').style.display = 'block';
    $('#tableResults').classList.add('d-none');
  }

  function displayTableResults(results) {
    const tbody = $('#tableBody');
    tbody.innerHTML = '';
    results.forEach(d => tbody.appendChild(createTableRow(d)));
    $('#cardResults').style.display = 'none';
    $('#tableResults').classList.remove('d-none');
  }

  function createResultCard(dossier) {
    const card = document.createElement('div');
    card.className = 'result-card';
    card.addEventListener('click', () => { window.location.href = `/dossier/${dossier.id}`; });
    card.innerHTML = `
      <div class="result-header">
        <h5 class="result-title">Dossier #${escapeHtml(dossier.id)}</h5>
        <div class="result-status">
          <span class="status-badge ${getStatusClass(dossier.statut)}">${getStatusLabel(dossier.statut)}</span>
        </div>
      </div>
      <div class="result-meta">
        <div class="meta-item"><span>👤</span> ${escapeHtml(dossier.client?.nom || 'N/A')} ${escapeHtml(dossier.client?.prenom || '')}</div>
        <div class="meta-item"><span>📱</span> ${escapeHtml(dossier.appareil?.marque || 'N/A')} ${escapeHtml(dossier.appareil?.modele || '')}</div>
        <div class="meta-item"><span>📅</span> ${escapeHtml(formatDate(dossier.dateEntree))}</div>
        <div class="meta-item"><span>🔧</span> ${escapeHtml(dossier.symptome || 'N/A')}</div>
      </div>`;
    return card;
  }

  function createTableRow(dossier) {
    const row = document.createElement('tr');
    row.addEventListener('click', () => { window.location.href = `/dossier/${dossier.id}`; });
    row.innerHTML = `
      <td><strong>#${escapeHtml(dossier.id)}</strong></td>
      <td>${escapeHtml(dossier.client?.nom || 'N/A')} ${escapeHtml(dossier.client?.prenom || '')}</td>
      <td>${escapeHtml(dossier.appareil?.marque || 'N/A')} ${escapeHtml(dossier.appareil?.modele || '')}</td>
      <td>${escapeHtml(dossier.symptome || 'N/A')}</td>
      <td><span class="status-badge ${getStatusClass(dossier.statut)}">${getStatusLabel(dossier.statut)}</span></td>
      <td>${escapeHtml(formatDate(dossier.dateEntree))}</td>
      <td><a href="/dossier/${encodeURIComponent(dossier.id)}" class="btn btn-sm btn-outline-primary">👁️ Voir</a></td>`;
    return row;
  }

  function setView(view) {
    currentView = view;
    $$('.view-btn').forEach(btn => btn.classList.remove('active'));
    document.getElementById(view + 'View').classList.add('active');
    if (searchData.length > 0) {
      if (view === 'card') displayCardResults(searchData); else displayTableResults(searchData);
    }
  }

  function handleSortChange() {
    currentSort = $('#sortSelect').value;
    currentPage = 0; performSearch();
  }

  function updatePagination(data) {
    // TODO: Implémentation pagination si nécessaire (conservée telle quelle)
  }

  function clearAllFilters() {
    $('#advancedSearchForm').reset();
    $('#searchInput').value = '';
    currentFilters = {}; currentPage = 0;
    $$('.filter-chip').forEach(chip => chip.classList.remove('active'));
    document.querySelector('[data-filter="all"]').classList.add('active');
    performSearch();
  }

  function loadInitialData() {
    fetch('/api/search/stats')
      .then(r => r.json())
      .then(stats => {
        $('#countAll').textContent = stats.total || 0;
        $('#countRecu').textContent = stats.recu || 0;
        $('#countEnCours').textContent = stats.en_cours || 0;
        $('#countReparation').textContent = stats.reparation || 0;
        $('#countTermine').textContent = stats.termine || 0;
        $('#countUrgent').textContent = stats.urgent || 0;
      })
      .catch(err => console.error('Erreur chargement stats:', err));
    performSearch();
  }

  // Utils
  function debounce(func, wait) {
    let timeout; return function(...args) { clearTimeout(timeout); timeout = setTimeout(() => func(...args), wait); };
  }
  function showLoading(show) { $('#loadingSpinner').classList.toggle('show', show); }
  function showError(message) { console.error(message); }
  function getStatusClass(statut) {
    const classes = { 'recu':'bg-primary','diagnostic':'bg-info','en_cours':'bg-warning','attente_pieces':'bg-secondary','reparation':'bg-warning','teste':'bg-info','pret':'bg-success','termine':'bg-dark','annule':'bg-danger' };
    return classes[statut] || 'bg-secondary';
  }
  function getStatusLabel(statut) {
    const labels = { 'recu':'📥 Reçu','diagnostic':'🔍 Diagnostic','en_cours':'⚠️ En cours','attente_pieces':'⏱️ Attente pièces','reparation':'🔧 Réparation','teste':'✅ Testé','pret':'📦 Prêt','termine':'✨ Terminé','annule':'❌ Annulé' };
    return labels[statut] || statut;
  }
  function formatDate(dateString) { if (!dateString) return 'N/A'; const d = new Date(dateString); return d.toLocaleDateString('fr-FR'); }
  function escapeHtml(str) { return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#039;'); }
})();
