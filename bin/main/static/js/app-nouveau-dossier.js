// JS de la page Nouveau Dossier SAV
(function() {
  document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('form');
    if (form) {
      form.addEventListener('submit', onSubmitValidate);
    }
    setupTextareaCounters(['symptome','commentaire']);
  });

  function onSubmitValidate(e) {
    const requiredFields = ['nomClient', 'emailClient', 'marqueAppareil', 'modeleAppareil', 'symptome'];
    let isValid = true;

    requiredFields.forEach(id => {
      const field = document.getElementById(id);
      if (field && !field.value.trim()) {
        field.classList.add('is-invalid');
        isValid = false;
      } else if (field) {
        field.classList.remove('is-invalid');
      }
    });

    const emailField = document.getElementById('emailClient');
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (emailField && emailField.value && !emailRegex.test(emailField.value)) {
      emailField.classList.add('is-invalid');
      isValid = false;
    }

    if (!isValid) {
      e.preventDefault();
      // Utiliser une alerte Bootstrap si possible
      showAlert('danger', "Veuillez remplir tous les champs obligatoires correctement.");
    }
  }

  function setupTextareaCounters(ids) {
    ids.forEach(fieldId => {
      const field = document.getElementById(fieldId);
      if (!field) return;
      const maxLength = parseInt(field.getAttribute('maxlength') || '0', 10);
      field.addEventListener('input', function() {
        const remaining = maxLength - this.value.length;
        const formText = this.nextElementSibling;
        if (formText && formText.classList.contains('form-text')) {
          formText.textContent = `${remaining} caractères restants`;
          if (remaining < 50) {
            formText.classList.add('text-warning');
          } else {
            formText.classList.remove('text-warning');
          }
        }
      });
    });
  }

  function showAlert(type, message) {
    const container = document.querySelector('.container');
    if (!container) return;
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
      <strong>${type === 'success' ? '✅ Succès' : '❌ Erreur'} :</strong> ${escapeHtml(message)}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    container.insertBefore(alert, container.firstChild);
  }

  function escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }
})();
