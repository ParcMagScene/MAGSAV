function refreshQR(){
  const img = document.getElementById('qrPreview');
  if(!img) return;
  const base = img.getAttribute('src').split('?')[0];
  img.src = base + '?_=' + Date.now();
}

// Attacher les handlers sans inline JS
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.js-refresh-qr').forEach(btn => {
    btn.addEventListener('click', refreshQR);
  });
});
