// JS pour dossier-liste: tri et taille de page sans inline handlers
(function(){
  function updateUrlParam(updates) {
    const urlParams = new URLSearchParams(window.location.search);
    Object.entries(updates).forEach(([k,v]) => urlParams.set(k, v));
    window.location.href = '?' + urlParams.toString();
  }

  function sortBy(column) {
    const urlParams = new URLSearchParams(window.location.search);
    const currentSort = urlParams.get('sortBy');
    const currentDir = urlParams.get('sortDir');
    let newDir = 'asc';
    if (currentSort === column && currentDir === 'asc') newDir = 'desc';
    updateUrlParam({ sortBy: column, sortDir: newDir, page: '0' });
  }

  function changePageSize(newSize) {
    updateUrlParam({ size: String(newSize), page: '0' });
  }

  document.addEventListener('DOMContentLoaded', function(){
    document.querySelectorAll('.js-sort').forEach(th => {
      th.addEventListener('click', () => sortBy(th.dataset.col));
    });
    const sizeSel = document.querySelector('.js-page-size');
    if (sizeSel) sizeSel.addEventListener('change', () => changePageSize(sizeSel.value));
  });
})();
