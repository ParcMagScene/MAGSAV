// Gestion du drag & drop et de la sélection de fichier
(function(){
  const uploadArea = document.getElementById('uploadArea');
  const fileInput = document.getElementById('file');
  const uploadContent = document.getElementById('uploadContent');
  const fileInfo = document.getElementById('fileInfo');
  const fileName = document.getElementById('fileName');
  const fileSize = document.getElementById('fileSize');
  const submitBtn = document.getElementById('submitBtn');
  const typeSelect = document.getElementById('type');

  if(!uploadArea || !fileInput) return;

  uploadArea.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadArea.classList.add('dragover');
  });

  uploadArea.addEventListener('dragleave', (e) => {
    e.preventDefault();
    uploadArea.classList.remove('dragover');
  });

  uploadArea.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadArea.classList.remove('dragover');
    const files = e.dataTransfer.files;
    if (files.length > 0) {
      fileInput.files = files;
      handleFileSelect();
    }
  });

  uploadArea.addEventListener('click', (e) => {
    if (e.target === uploadArea || e.target.closest('#uploadContent')) {
      fileInput.click();
    }
  });

  document.querySelectorAll('.js-open-file').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      fileInput.click();
    });
  });

  fileInput.addEventListener('change', handleFileSelect);
  typeSelect && typeSelect.addEventListener('change', checkFormValid);

  function handleFileSelect() {
    const file = fileInput.files[0];
    if (file) {
      fileName.textContent = file.name;
      fileSize.textContent = formatFileSize(file.size);
      uploadContent.classList.add('d-none');
      fileInfo.classList.remove('d-none');
      checkFormValid();
    }
  }

  function checkFormValid() {
    const hasFile = fileInput.files.length > 0;
    const hasType = typeSelect && typeSelect.value !== '';
    submitBtn.disabled = !(hasFile && hasType);
  }

  function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
})();
