// JS page erreur - gère le bouton retour sans inline handler
(function(){
  document.addEventListener('DOMContentLoaded', function(){
    const btn = document.querySelector('.js-back');
    if (btn) btn.addEventListener('click', function(e){ e.preventDefault(); window.history.back(); });
  });
})();
