// JS de la page Import Avancé
// Gère drag & drop, sélection de type, prévisualisation CSV et soumission du formulaire

(function() {
  // Etat
  let selectedFile = null;
  let selectedType = null;
  let csvData = null;

  // Helpers DOM
  const $ = (sel) => document.querySelector(sel);
  const $$ = (sel) => Array.from(document.querySelectorAll(sel));

  // Templates
  function templateHtml(id) {
    const tpl = document.getElementById(id);
    if (!tpl) return '';
    if (tpl.tagName.toLowerCase() === 'template') {
      return tpl.innerHTML.trim();
    }
    // fallback legacy
    return tpl.innerHTML.trim();
  }

  const formatTemplates = {
    'clients': templateHtml('clientsFormatTemplate'),
    'fournisseurs': templateHtml('fournisseursFormatTemplate'),
    'dossiers_sav': templateHtml('dossiersFormatTemplate'),
    'produits': templateHtml('produitsFormatTemplate')
  };

  document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
  });

  function setupEventListeners() {
    const dropZone = $('#dropZone');
    const fileInput = $('#fileInput');

    // Drag & drop
    ['dragover','dragleave','drop'].forEach(evt => {
      dropZone.addEventListener(evt, (e) => e.preventDefault());
      dropZone.addEventListener(evt, (e) => e.stopPropagation());
    });
    dropZone.addEventListener('dragover', () => dropZone.classList.add('dragover'));
    dropZone.addEventListener('dragleave', () => dropZone.classList.remove('dragover'));
    dropZone.addEventListener('drop', (e) => {
      dropZone.classList.remove('dragover');
      const files = e.dataTransfer.files;
      if (files && files.length > 0) handleFileSelect({ target: { files } });
    });

    // Browse button
    $$('.js-browse').forEach(btn => btn.addEventListener('click', () => fileInput.click()));

    // Clear file
    $$('.js-clear-file').forEach(btn => btn.addEventListener('click', clearFile));

    // File chooser
    fileInput.addEventListener('change', handleFileSelect);

    // Type selection
    $$('.type-option').forEach(opt => {
      opt.addEventListener('click', () => selectType(opt.dataset.type));
    });

    // Import button
    $('#importBtn').addEventListener('click', startImport);
    // Reset button
    $('#resetBtn').addEventListener('click', resetImport);
  }

  // File handling
  function handleFileSelect(e) {
    const file = e.target.files[0];
    if (!file) return;

    if (!file.name.toLowerCase().endsWith('.csv')) {
      return showError('Seuls les fichiers CSV sont acceptés');
    }
    if (file.size > 10 * 1024 * 1024) {
      return showError('Le fichier est trop volumineux (maximum 10MB)');
    }

    selectedFile = file;
    showFileInfo(file);
    parseCSVFile(file);
  }

  function showFileInfo(file) {
    const dropZone = $('#dropZone');
    const fileInfo = $('#fileInfo');
    dropZone.classList.add('has-file');
    fileInfo.classList.add('show');
    $('#fileName').textContent = file.name;
    $('#fileSize').textContent = formatFileSize(file.size);
  }

  function parseCSVFile(file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target.result;
      csvData = parseCSV(text);
      updatePreview(csvData);
      updateStats(csvData);
      validateData(csvData);
      updateImportButton();
    };
    reader.readAsText(file);
  }

  function parseCSV(text) {
    const lines = text.replace(/\r\n?/g, '\n').trim().split('\n');
    const headers = splitCsvLine(lines[0]);
    const rows = [];
    for (let i = 1; i < lines.length; i++) {
      const line = lines[i];
      if (!line || !line.trim()) continue;
      rows.push(splitCsvLine(line));
    }
    return { headers, rows };
  }

  // CSV splitter handling quotes
  function splitCsvLine(line) {
    const result = [];
    let current = '';
    let inQuotes = false;
    for (let i = 0; i < line.length; i++) {
      const ch = line[i];
      if (ch === '"') {
        if (inQuotes && line[i+1] === '"') { // escaped quote
          current += '"'; i++; continue;
        }
        inQuotes = !inQuotes; continue;
      }
      if (ch === ',' && !inQuotes) {
        result.push(current.trim()); current = ''; continue;
      }
      current += ch;
    }
    result.push(current.trim());
    return result;
  }

  function updatePreview(data) {
    const container = $('#previewContainer');
    const table = $('#previewTable');

    let html = '<thead><tr>' + data.headers.map(h => `<th>${escapeHtml(h)}</th>`).join('') + '</tr></thead><tbody>';
    data.rows.slice(0, 10).forEach(row => {
      html += '<tr>' + row.map(c => `<td>${escapeHtml(c || '-')}</td>`).join('') + '</tr>';
    });
    html += '</tbody>';

    table.innerHTML = html;
    container.classList.add('show');
  }

  function updateStats(data) {
    $('#totalRows').textContent = data.rows.length;
    $('#totalColumns').textContent = data.headers.length;
    $('#fileRows').textContent = data.rows.length;
  }

  function validateData(data) {
    const resultDiv = $('#validationResult');
    if (!selectedType) {
      resultDiv.innerHTML = '<small>⚠️ Sélectionnez d\'abord le type de données</small>';
      resultDiv.className = 'alert alert-warning';
      return;
    }
    let isValid = false;
    switch (selectedType) {
      case 'clients': isValid = hasColumns(data, ['nom','prenom','telephone','email','adresse']); break;
      case 'fournisseurs': isValid = hasColumns(data, ['nom','contact','telephone','email','adresse']); break;
      case 'dossiers_sav': isValid = hasColumns(data, ['client_nom','appareil_marque','appareil_modele','symptome','statut']); break;
      case 'produits': isValid = hasColumns(data, ['nom','marque','modele','prix','stock']); break;
    }
    if (isValid) {
      resultDiv.className = 'alert alert-success';
      resultDiv.innerHTML = '<small>✅ Format valide, prêt pour l\'import</small>';
    } else {
      resultDiv.className = 'alert alert-danger';
      resultDiv.innerHTML = '<small>❌ Format invalide, vérifiez les colonnes</small>';
    }
  }

  function hasColumns(data, required) {
    const set = new Set(data.headers.map(h => h.toLowerCase()));
    return required.every(c => set.has(c.toLowerCase()));
  }

  function selectType(type) {
    selectedType = type;
    $$('.type-option').forEach(o => o.classList.remove('selected'));
    const sel = document.querySelector(`[data-type="${type}"]`);
    if (sel) sel.classList.add('selected');

    $('#formatInfo').innerHTML = formatTemplates[type] || '';
    if (csvData) validateData(csvData);
    updateImportButton();
  }

  function updateImportButton() {
    const btn = $('#importBtn');
    const canImport = !!(selectedFile && selectedType && csvData);
    btn.disabled = !canImport;
    btn.innerHTML = canImport ? `⬆️ Importer ${csvData.rows.length} ligne(s)` : `⬆️ Lancer l\'import`;
  }

  function startImport() {
    if (!selectedFile || !selectedType) {
      return showError('Veuillez sélectionner un fichier et un type de données');
    }
    $('#importProgress').classList.add('show');
    // Réinitialiser la barre de progression (pour l'UX uniquement)
    const bar = document.querySelector('#importProgress .progress-bar');
    if (bar) {
      bar.style.width = '0%';
      bar.setAttribute('aria-valuenow', '0');
      // Animation fictive jusqu'à soumission (optionnel)
      let pct = 0;
      const intId = setInterval(() => {
        pct = Math.min(pct + 10, 90);
        bar.style.width = pct + '%';
        bar.setAttribute('aria-valuenow', String(pct));
        if (pct >= 90) clearInterval(intId);
      }, 150);
    }
    const form = $('#importForm');
    const hiddenInput = $('#hiddenFileInput');
    const typeInput = $('#selectedType');
    const dt = new DataTransfer();
    dt.items.add(selectedFile);
    hiddenInput.files = dt.files;
    typeInput.value = selectedType;
    form.submit();
  }

  function resetImport() {
    selectedFile = null; selectedType = null; csvData = null;
    $('#dropZone').classList.remove('has-file','error');
    $('#fileInfo').classList.remove('show');
    $('#previewContainer').classList.remove('show');
    $('#importProgress').classList.remove('show');
    const bar = document.querySelector('#importProgress .progress-bar');
    if (bar) {
      bar.style.width = '0%';
      bar.setAttribute('aria-valuenow', '0');
    }
    $('#fileInput').value = '';
    $$('.type-option').forEach(o => o.classList.remove('selected'));
    $('#formatInfo').innerHTML = '<p class="text-muted">Sélectionnez un type de données pour voir le format attendu.</p>';
    $('#totalRows').textContent = '0';
    $('#totalColumns').textContent = '0';
    const vr = $('#validationResult');
    vr.innerHTML = '<small>⏳ Sélectionnez un fichier pour voir les statistiques</small>';
    vr.className = 'alert alert-info';
    updateImportButton();
  }

  function clearFile() { resetImport(); }

  function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024; const sizes = ['Bytes','KB','MB','GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  function escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function showError(message) {
    const dz = $('#dropZone');
    dz.classList.add('error');
    setTimeout(() => dz.classList.remove('error'), 3000);
    const alert = document.createElement('div');
    alert.className = 'alert alert-danger alert-dismissible fade show mt-3';
    alert.innerHTML = `
      <strong>❌ Erreur :</strong> ${escapeHtml(message)}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    const container = document.querySelector('.container .row .col-12');
    if (container) container.appendChild(alert);
  }
})();
