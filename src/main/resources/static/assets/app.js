// Toggle modo oscuro (persistente con localStorage)
(function(){
  const key = 'eventosadmin:dark';
  const btn = document.getElementById('toggleDarkMode');
  const apply = (v) => {
    document.documentElement.classList.toggle('dark', v);
    if(btn) btn.textContent = v ? 'Modo Claro' : 'Modo Oscuro';
  };
  apply(localStorage.getItem(key) === '1');
  if(btn){
    btn.addEventListener('click', () => {
      const cur = localStorage.getItem(key) === '1';
      localStorage.setItem(key, cur ? '0' : '1');
      apply(!cur);
    });
  }
})();
