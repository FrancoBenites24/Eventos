(function(){
  const PEN = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN', minimumFractionDigits: 2 });
  const $ = (sel, root=document) => root.querySelector(sel);
  const $$ = (sel, root=document) => Array.from(root.querySelectorAll(sel));
  const toastC = () => $('#toasts');

  const nav = document.querySelector('.navbar');
  const onScroll = () => { if (window.scrollY > 8) nav.classList.add('scrolled'); else nav.classList.remove('scrolled'); };
  document.addEventListener('scroll', onScroll, { passive: true });

  function showToast(type = 'info', title = 'Info', message = ''){
    const icons = { success: 'check-circle', info: 'info-circle', error: 'exclamation-octagon' };
    const classes = { success: 'toast-success', info: 'toast-info', error: 'toast-error' };
    const el = document.createElement('div');
    el.className = `toast align-items-center ${classes[type] ?? ''}`;
    el.setAttribute('role','status');
    el.setAttribute('aria-live','polite');
    el.innerHTML = `
      <div class="toast-header">
        <i class="bi bi-${icons[type] ?? icons.info}"></i>
        <strong class="me-auto">${title}</strong>
        <small class="text-muted">ahora</small>
        <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Cerrar"></button>
      </div>
      <div class="toast-body">${message}</div>`;
    toastC().appendChild(el);
    new bootstrap.Toast(el, { delay: 3500 }).show();
  }

  let CURRENT_CAT = 'Todos';
  let CURRENT_QUERY = '';
  let APPLIED_COUPON = null;

  const routes = [
    { name: 'home',     pattern: /^#?(home)?$/,                         render: () => renderHome() },
    { name: 'event',    pattern: /^#event\/(\d+)$/,                    render: (m) => renderEvent(m[1]) },
    { name: 'checkout', pattern: /^#checkout$/,                         render: () => renderCheckout() },
    { name: 'confirm',  pattern: /^#confirm\/(\w[\w-]*)$/,            render: (m) => renderConfirm(m[1]) },
    { name: 'login',    pattern: /^#login$/,                            render: () => renderLogin() },
    { name: 'account',  pattern: /^#account$/,                          render: () => renderAccount() },
    { name: '404',      pattern: /.*/,                                  render: () => renderHome() },
  ];
  function setActiveView(id){
    document.querySelectorAll('main > section').forEach(s => s.classList.remove('active'));
    const el = document.getElementById(id); if (el) el.classList.add('active');
    window.scrollTo({ top: 0, behavior: 'instant' });
  }
  function router(){
    const hash = window.location.hash || '#home';
    for (const r of routes){
      const match = hash.match(r.pattern);
      if (match){ r.render(match); return; }
    }
  }

  function updateCartCount(){ const c = API.getCart(); $('#navCartCount').textContent = c.reduce((n,i)=>n+(i.qty||1),0); }
  function filteredEvents(){
    const EVENTS = API.getEvents();
    return EVENTS.filter(e=> (CURRENT_CAT==='Todos' || e.cat===CURRENT_CAT) && (CURRENT_QUERY==='' || `${e.title} ${e.place}`.toLowerCase().includes(CURRENT_QUERY)) );
  }
  function requireSession(){ const me = API.me(); if(!me){ window.location.hash = '#login'; showToast('info','Inicia sesión','Necesitas iniciar sesión para continuar.'); return null; } return me; }
  function formatP(v){ return PEN.format(v||0); }

  function renderCart(){
    const cart = API.getCart();
    const lines = $('#cartLines');
    if(!cart.length){
      lines.innerHTML = `<div class="text-center text-body-secondary py-5"><i class="bi bi-bag-dash fs-1 d-block mb-3"></i>Tu carrito está vacío</div>`;
    } else {
      lines.innerHTML = cart.map(it => `
        <div class="d-flex gap-2 align-items-center border rounded p-2 mb-2">
          <img src="${it.img}" alt="${it.title}" class="rounded" style="width:64px;height:64px;object-fit:cover;">
          <div class="flex-fill">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <strong>${it.title}</strong>
                <div class="small text-body-secondary"><i class="bi bi-geo-alt me-1"></i>${it.place} · <i class="bi bi-calendar-event ms-2 me-1"></i>${it.when}</div>
              </div>
              <button class="btn btn-sm btn-outline-danger btn-rem" data-id="${it.id}" aria-label="Quitar"><i class="bi bi-x"></i></button>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-2">
              <div class="qty" role="group" aria-label="Cantidad">
                <button class="btn-minus" data-id="${it.id}" aria-label="Disminuir"><i class="bi bi-dash"></i></button>
                <input class="qty-input" data-id="${it.id}" type="number" min="1" value="${it.qty}">
                <button class="btn-plus" data-id="${it.id}" aria-label="Aumentar"><i class="bi bi-plus"></i></button>
              </div>
              <strong>${it.price>0?formatP(it.price):'<span class="text-success">GRATIS</span>'}</strong>
            </div>
          </div>
        </div>`).join('');

      lines.querySelectorAll('.btn-rem').forEach(b=> b.addEventListener('click', ()=> { API.removeCartItem(b.dataset.id); renderCart(); updateCartCount(); calcCartTotals(); }));
      lines.querySelectorAll('.btn-minus').forEach(b=> b.addEventListener('click', ()=> { const it = API.getCart().find(x=>x.id===b.dataset.id); API.updateCartItem(b.dataset.id, (it.qty||1)-1); renderCart(); updateCartCount(); calcCartTotals(); }));
      lines.querySelectorAll('.btn-plus').forEach(b=> b.addEventListener('click',  ()=> { const it = API.getCart().find(x=>x.id===b.dataset.id); API.updateCartItem(b.dataset.id, (it.qty||1)+1); renderCart(); updateCartCount(); calcCartTotals(); }));
      lines.querySelectorAll('.qty-input').forEach(inp=> inp.addEventListener('change', ()=> { API.updateCartItem(inp.dataset.id, inp.value); renderCart(); updateCartCount(); calcCartTotals(); }));
    }
  }
  function calcCartTotals(){
    const cart = API.getCart();
    const totals = API.calcTotals(cart, APPLIED_COUPON);
    $('#cartSubtotal').textContent = formatP(totals.subtotal);
    $('#cartQtyDiscount').textContent = `- ${formatP(totals.qtyDiscount)}`;
    $('#cartCoupon').textContent = `- ${formatP(totals.couponDiscount)}`;
    $('#cartTotal').textContent = formatP(totals.total);
  }

  function renderHome(){
    setActiveView('view-home');
    const el = $('#view-home');
    el.innerHTML = `
      <section class="py-4 py-md-5">
        <div class="p-4 p-md-5 rounded-4 glass mb-4">
          <div class="row g-4 align-items-center">
            <div class="col-lg-6">
              <h1 id="home-title" class="display-6 fw-bold mb-2" style="color:var(--color-primary)">Encuentra tu próximo evento</h1>
              <p class="mb-0 text-body-secondary">Busca por título o lugar. Filtra por categoría.</p>
            </div>
            <div class="col-lg-6">
              <label class="form-label visually-hidden" for="q">Buscar</label>
              <div class="input-group input-group-lg">
                <span class="input-group-text bg-white"><i class="bi bi-search" aria-hidden="true"></i></span>
                <input id="q" type="text" class="form-control" placeholder="Ej. Lima, Jazz, Summit..." aria-label="Buscar por título o lugar">
              </div>
              <div class="mt-3 d-flex flex-wrap gap-2" role="tablist" aria-label="Categorías">
                ${['Todos','Música','Cultural','Deporte','Tech'].map(c => `
                  <button type="button" class="btn btn-sm ${c==='Todos'?'btn-dark-subtle':'btn-outline-secondary'} cat-btn" data-cat="${c}" aria-pressed="${c==='Todos'?'true':'false'}">
                    <i class="bi bi-tag me-1"></i>${c}
                  </button>`).join('')}
              </div>
            </div>
          </div>
        </div>
        <div id="grid" class="row g-4" aria-live="polite"></div>
      </section>`;

    const q = $('#q'); q.value = CURRENT_QUERY; q.addEventListener('input', () => { CURRENT_QUERY = q.value.trim().toLowerCase(); renderEventCards(); });
    el.querySelectorAll('.cat-btn').forEach(btn=>{
      btn.addEventListener('click', () => {
        CURRENT_CAT = btn.dataset.cat;
        el.querySelectorAll('.cat-btn').forEach(b=>{ b.classList.remove('btn-dark-subtle'); b.setAttribute('aria-pressed','false'); b.classList.add('btn-outline-secondary');});
        btn.classList.remove('btn-outline-secondary'); btn.classList.add('btn-dark-subtle'); btn.setAttribute('aria-pressed','true');
        renderEventCards();
      });
    });

    renderEventCards(); updateCartCount();
  }
  function renderEventCards(){
    const grid = $('#grid');
    const list = filteredEvents();
    if(!list.length){ grid.innerHTML = `<div class=\"text-center text-body-secondary py-5\"><i class=\"bi bi-search-heart fs-1 d-block mb-3\"></i>No hay resultados</div>`; return; }
    grid.innerHTML = list.map(ev => `
      <div class="col-12 col-sm-6 col-lg-4">
        <article class="card h-100 card-event" aria-label="${ev.title}">
          <img src="${ev.img}" class="card-img-top" alt="Imagen de ${ev.title}">
          <div class="card-body d-flex flex-column">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <span class="badge badge-outline"><i class="bi bi-bookmark me-1"></i>${ev.cat}</span>
              <span class="text-warning" aria-label="Calificación ${ev.rating}"><i class="bi bi-star-fill me-1"></i>${ev.rating.toFixed(1)}</span>
            </div>
            <h3 class="h6 fw-bold mb-1">${ev.title}</h3>
            <p class="small text-body-secondary mb-2"><i class="bi bi-geo-alt me-1"></i>${ev.place} · <i class="bi bi-calendar-event ms-2 me-1"></i>${ev.when}</p>
            <div class="mt-auto d-flex justify-content-between align-items-center">
              <strong class="fs-5">${ev.price>0?formatP(ev.price):'<span class="text-success">GRATIS</span>'}</strong>
              <div class="btn-group" role="group" aria-label="Acciones">
                <a class="btn btn-sm btn-outline-secondary" href="#event/${ev.id}"><i class="bi bi-eye me-1"></i>Ver</a>
                <button class="btn btn-sm btn-primary btn-add" data-id="${ev.id}"><i class="bi bi-bag-plus me-1"></i>${ev.price>0?'Añadir':'Obtener'}</button>
              </div>
            </div>
          </div>
        </article>
      </div>`).join('');

    grid.querySelectorAll('.btn-add').forEach(b=> b.addEventListener('click', ()=> { API.addCartItem(b.dataset.id, 1); updateCartCount(); renderCart(); calcCartTotals(); showToast('success','Añadido', 'Se agregó al carrito'); }));
  }

  function renderEvent(id){
    setActiveView('view-event');
    const ev = API.getEvent(id);
    if(!ev){ $('#view-event').innerHTML = '<div class="py-5">Evento no encontrado</div>'; return; }
    const me = API.me();
    $('#view-event').innerHTML = `
      <section class="py-5">
        <div class="p-4 p-md-5 rounded-4 mb-4" style="background:linear-gradient(135deg, rgba(108,92,231,.15), rgba(0,210,211,.15));">
          <h2 id="event-title" class="fw-bold mb-1">${ev.title}</h2>
          <div class="d-flex flex-wrap gap-3 text-body-secondary">
            <span class="badge badge-outline"><i class="bi bi-bookmark me-1"></i>${ev.cat}</span>
            <span><i class="bi bi-geo-alt me-1"></i>${ev.place}</span>
            <span><i class="bi bi-calendar-event me-1"></i>${ev.when}</span>
            <span class="text-warning"><i class="bi bi-star-fill me-1"></i>${ev.rating.toFixed(1)}</span>
          </div>
        </div>
        <div class="row g-4">
          <div class="col-lg-7">
            <img src="${ev.img}" class="rounded w-100" alt="${ev.title}" style="object-fit:cover; max-height:420px;">
            <p class="mt-3">${ev.desc||''}</p>
          </div>
          <div class="col-lg-5">
            <div class="border rounded p-3">
              <div class="d-flex justify-content-between align-items-center">
                <strong class="fs-3">${ev.price>0?formatP(ev.price):'<span class="text-success">GRATIS</span>'}</strong>
              </div>
              <div class="mt-3 d-flex align-items-center gap-2">
                <div class="qty" role="group" aria-label="Cantidad">
                  <button id="evMinus"><i class="bi bi-dash"></i></button>
                  <input id="evQty" type="number" min="1" value="1">
                  <button id="evPlus"><i class="bi bi-plus"></i></button>
                </div>
                <button id="evAdd" class="btn btn-primary flex-fill"><i class="bi bi-bag-plus me-1"></i>${ev.price>0?'Añadir al carrito':'Obtener'}</button>
              </div>
            </div>

            <div class="mt-4">
              <h5 class="mb-2">Reseñas</h5>
              <div id="reviewsWrap"></div>
              <div id="reviewFormWrap" class="mt-3"></div>
            </div>
          </div>
        </div>
      </section>`;

    const qI = $('#evQty'); $('#evMinus').addEventListener('click', ()=> qI.value = Math.max(1, (+qI.value||1)-1)); $('#evPlus').addEventListener('click', ()=> qI.value = (+qI.value||1)+1);
    $('#evAdd').addEventListener('click', ()=>{ API.addCartItem(ev.id, +qI.value||1); updateCartCount(); renderCart(); calcCartTotals(); showToast('success','Añadido', 'Se agregó al carrito'); });

    renderReviews(ev.id);
    renderReviewForm(ev.id);
  }
  function renderReviews(eventId){
    const wrap = $('#reviewsWrap'); const list = API.getReviews(eventId);
    if(!list.length){ wrap.innerHTML = '<div class="text-body-secondary">Aún no hay reseñas.</div>'; return; }
    const avg = (list.reduce((s,r)=>s+r.rating,0) / list.length).toFixed(1);
    wrap.innerHTML = `
      <div class="mb-2"><strong class="me-2" style="color:var(--color-primary)">Promedio ${avg}</strong>${'★'.repeat(Math.round(avg))}</div>
      <div class="vstack gap-2">
        ${list.map(r => `
          <div class="border rounded p-2 ticket-badge">
            <div class="small">${'★'.repeat(r.rating)}<span class="ms-2 text-body-secondary">${new Date(r.createdAt).toLocaleString()}</span></div>
            <div>${r.comment? r.comment : '<span class=\"text-body-secondary\">(Sin comentario)</span>'}</div>
          </div>`).join('')}
      </div>`;
  }
  function canUserReview(eventId){
    const me = API.me(); if(!me) return false;
    const orders = API.getOrders(me.id);
    const hasTicket = orders.some(o => o.tickets?.some(t => t.eventId === Number(eventId)));
    const hasReview = API.getReviews(eventId).some(r => r.userId === me.id);
    return hasTicket && !hasReview;
  }
  function renderReviewForm(eventId){
    const wrap = $('#reviewFormWrap');
    if(!canUserReview(eventId)) { wrap.innerHTML = '<div class="small text-body-secondary">(Puedes reseñar cuando hayas asistido: tickets usados en esta demo se asumen al pagar.)</div>'; return; }
    wrap.innerHTML = `
      <div class="border rounded p-3">
        <h6 class="mb-2">Escribe tu reseña</h6>
        <div class="mb-2">
          <label class="form-label" for="revRating">Calificación</label>
          <select id="revRating" class="form-select" aria-label="Calificación">
            <option value="5">★★★★★ (5)</option>
            <option value="4">★★★★☆ (4)</option>
            <option value="3">★★★☆☆ (3)</option>
            <option value="2">★★☆☆☆ (2)</option>
            <option value="1">★☆☆☆☆ (1)</option>
          </select>
        </div>
        <div class="mb-2">
          <label class="form-label" for="revComment">Comentario (opcional)</label>
          <textarea id="revComment" class="form-control" rows="3" placeholder="¿Qué te pareció?"></textarea>
        </div>
        <button id="revSubmit" class="btn btn-primary"><i class="bi bi-send me-1"></i>Enviar</button>
      </div>`;
    $('#revSubmit').addEventListener('click', ()=>{
      const me = API.me(); if(!me){ window.location.hash = '#login'; return; }
      const rating = Number($('#revRating').value); const comment = $('#revComment').value;
      const res = API.createReview({eventId, userId: me.id, rating, comment});
      if(res.ok){ showToast('success','¡Gracias!','Reseña enviada.'); renderReviews(eventId); renderReviewForm(eventId); }
      else showToast('error','Ups', res.msg||'No se pudo enviar.');
    });
  }

  function renderCheckout(){
    setActiveView('view-checkout');
    const el = $('#view-checkout');
    const cart = API.getCart();
    if(!cart.length){ el.innerHTML = '<div class="py-5"><h2 id="checkout-title" class="fw-bold mb-3">Checkout</h2><div class="alert alert-info">No hay items en el carrito.</div></div>'; return; }

    el.innerHTML = `
      <div class="py-4">
        <div class="d-flex align-items-center justify-content-between mb-3">
          <h2 id="checkout-title" class="fw-bold mb-0">Checkout</h2>
          <div class="stepper">
            <div class="step active">1</div><div class="line"></div><div class="step">2</div><div class="line"></div><div class="step">3</div>
          </div>
        </div>
        <div class="row g-4">
          <div class="col-lg-8">
            <div id="coStep" class="vstack gap-3"></div>
          </div>
          <div class="col-lg-4">
            <div class="border rounded p-3">
              <div class="input-group input-group-sm mb-2">
                <span class="input-group-text"><i class="bi bi-ticket-perforated"></i></span>
                <input id="coCoupon" type="text" class="form-control" placeholder="Cupón (FIESTA20)" value="${APPLIED_COUPON||''}">
                <button id="coApply" class="btn btn-outline-secondary">Aplicar</button>
              </div>
              <div class="d-flex justify-content-between"><span>Subtotal</span><strong id="coSubtotal">S/ 0.00</strong></div>
              <div class="d-flex justify-content-between small text-body-secondary"><span>Desc. por cantidad</span><span id="coQtyDiscount">S/ 0.00</span></div>
              <div class="d-flex justify-content-between small text-body-secondary"><span>Cupones</span><span id="coCouponAmt">S/ 0.00</span></div>
              <div class="d-flex justify-content-between mt-2 fs-5"><span>Total</span><strong id="coTotal">S/ 0.00</strong></div>
              <div class="d-grid gap-2 mt-3">
                <a id="coBack" class="btn btn-outline-secondary" href="#home"><i class="bi bi-arrow-left me-1"></i>Seguir comprando</a>
                <button id="coNext" class="btn btn-primary"><i class="bi bi-arrow-right me-1"></i>Continuar</button>
              </div>
            </div>
          </div>
        </div>
      </div>`;

    function renderStep1(){
      const cart = API.getCart();
      $('#coStep').innerHTML = cart.map(it => `
        <div class="d-flex gap-2 align-items-center border rounded p-2">
          <img src="${it.img}" alt="${it.title}" class="rounded" style="width:72px;height:72px;object-fit:cover;">
          <div class="flex-fill">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <strong>${it.title}</strong>
                <div class="small text-body-secondary"><i class="bi bi-geo-alt me-1"></i>${it.place} · <i class="bi bi-calendar-event ms-2 me-1"></i>${it.when}</div>
              </div>
              <button class="btn btn-sm btn-outline-danger btn-rem" data-id="${it.id}" aria-label="Quitar"><i class="bi bi-x"></i></button>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-2">
              <div class="qty" role="group" aria-label="Cantidad">
                <button class="btn-minus" data-id="${it.id}"><i class="bi bi-dash"></i></button>
                <input class="qty-input" data-id="${it.id}" type="number" min="1" value="${it.qty}">
                <button class="btn-plus" data-id="${it.id}"><i class="bi bi-plus"></i></button>
              </div>
              <strong>${it.price>0?formatP(it.price):'<span class="text-success">GRATIS</span>'}</strong>
            </div>
          </div>
        </div>`).join('');
      $('#coStep').querySelectorAll('.btn-rem').forEach(b=> b.addEventListener('click', ()=> { API.removeCartItem(b.dataset.id); renderStep1(); updateCartCount(); calcCO(); }));
      $('#coStep').querySelectorAll('.btn-minus').forEach(b=> b.addEventListener('click', ()=> { const it = API.getCart().find(x=>x.id===b.dataset.id); API.updateCartItem(b.dataset.id, (it.qty||1)-1); renderStep1(); updateCartCount(); calcCO(); }));
      $('#coStep').querySelectorAll('.btn-plus').forEach(b=> b.addEventListener('click',  ()=> { const it = API.getCart().find(x=>x.id===b.dataset.id); API.updateCartItem(b.dataset.id, (it.qty||1)+1); renderStep1(); updateCartCount(); calcCO(); }));
      $('#coStep').querySelectorAll('.qty-input').forEach(inp=> inp.addEventListener('change', ()=> { API.updateCartItem(inp.dataset.id, inp.value); renderStep1(); updateCartCount(); calcCO(); }));
    }

    function renderStep2(){
      const me = requireSession(); if(!me) return;
      $('#coStep').innerHTML = `
        <div class="alert alert-primary d-flex align-items-center" role="status"><i class="bi bi-info-circle me-2"></i>
          <div>Demo: los métodos de pago son simulados.</div>
        </div>
        <div class="vstack gap-3">
          <div class="form-check">
            <input class="form-check-input" type="radio" name="payM" id="payCard" value="TARJETA" checked>
            <label class="form-check-label" for="payCard"><i class="bi bi-credit-card me-1"></i>Tarjeta (morado)</label>
          </div>
          <div class="form-check">
            <input class="form-check-input" type="radio" name="payM" id="payYape" value="YAPE">
            <label class="form-check-label" for="payYape"><i class="bi bi-phone me-1"></i>Yape (turquesa)</label>
          </div>
          <div class="form-check">
            <input class="form-check-input" type="radio" name="payM" id="payWallet" value="BILLETERA">
            <label class="form-check-label" for="payWallet"><i class="bi bi-wallet2 me-1"></i>Billetera — saldo: <strong>${formatP(me.wallet||0)}</strong></label>
            <div id="walletWarn" class="form-text text-danger d-none">Saldo insuficiente</div>
          </div>
          <div class="d-flex gap-2">
            <button id="coPrev2" class="btn btn-outline-secondary"><i class="bi bi-arrow-left me-1"></i>Atrás</button>
            <button id="coPay" class="btn btn-primary"><i class="bi bi-check2-circle me-1"></i>Pagar ahora</button>
          </div>
        </div>`;

      const totals = API.calcTotals(API.getCart(), APPLIED_COUPON);
      const walletRadio = $('#payWallet');
      function checkWallet(){ const warn = $('#walletWarn'); if(walletRadio.checked && (me.wallet||0) < totals.total){ warn.classList.remove('d-none'); $('#coPay').disabled = true; } else { warn.classList.add('d-none'); $('#coPay').disabled = false; } }
      $$('input[name="payM"]').forEach(r=> r.addEventListener('change', checkWallet)); checkWallet();

      $('#coPrev2').addEventListener('click', ()=> { step=1; setStepUI(); renderStep1(); });
      $('#coPay').addEventListener('click', ()=> {
        const me = requireSession(); if(!me) return;
        const method = ($$('input[name="payM"]').find(r=>r.checked)||{}).value || 'TARJETA';
        const order = API.createOrder({ userId: me.id, items: API.getCart(), couponCode: APPLIED_COUPON, payMethod: method });
        if(method==='BILLETERA'){
          API.addWallet(-order.total);
        }
        API.clearCart(); updateCartCount();
        showToast('success','Pago exitoso',`Pedido ${order.id}`);
        window.location.hash = `#confirm/${order.id}`;
      });
    }

    function renderStep3(){}

    function calcCO(){
      const t = API.calcTotals(API.getCart(), APPLIED_COUPON);
      $('#coSubtotal').textContent = formatP(t.subtotal);
      $('#coQtyDiscount').textContent = `- ${formatP(t.qtyDiscount)}`;
      $('#coCouponAmt').textContent = `- ${formatP(t.couponDiscount)}`;
      $('#coTotal').textContent = formatP(t.total);
    }

    let step = 1; renderStep1(); calcCO();

    $('#coApply').addEventListener('click', ()=>{ const code = $('#coCoupon').value.trim(); const res = API.applyCoupon(code, API.calcTotals(API.getCart()).total); if(res.valid){ APPLIED_COUPON = res.code; showToast('success','Cupón aplicado',`- ${formatP(res.amount)}`); } else { APPLIED_COUPON = null; showToast('error','Cupón inválido',''); } calcCO(); });
    $('#coNext').addEventListener('click', ()=>{ if(step===1){ step=2; setStepUI(); renderStep2(); } });

    function setStepUI(){
      const steps = $$('.stepper .step'); const lines = $$('.stepper .line');
      steps.forEach(s=>{ s.classList.remove('active','done'); }); lines.forEach(l=> l.classList.remove('done'));
      if(step===1){ steps[0].classList.add('active'); }
      if(step===2){ steps[0].classList.add('done'); lines[0].classList.add('done'); steps[1].classList.add('active'); }
      if(step===3){ steps[0].classList.add('done'); lines[0].classList.add('done'); steps[1].classList.add('done'); lines[1].classList.add('done'); steps[2].classList.add('active'); }
    }
  }

  function renderConfirm(orderId){
    setActiveView('view-confirm');
    const me = requireSession(); if(!me) return;
    const order = API.getOrders(me.id).find(o=> o.id===orderId);
    if(!order){ $('#view-confirm').innerHTML = '<div class="py-5">Orden no encontrada.</div>'; return; }
    $('#view-confirm').innerHTML = `
      <div class="py-4">
        <div class="d-flex align-items-center justify-content-between mb-3">
          <h2 id="confirm-title" class="fw-bold mb-0">Confirmación</h2>
          <div class="stepper"><div class="step done">1</div><div class="line done"></div><div class="step done">2</div><div class="line done"></div><div class="step active">3</div></div>
        </div>
        <div class="border rounded p-3 mb-3">
          <div class="row g-3">
            <div class="col-md-6"><strong>Pedido:</strong> ${order.id}</div>
            <div class="col-md-6"><strong>Fecha:</strong> ${new Date(order.createdAt).toLocaleString()}</div>
            <div class="col-md-6"><strong>Método:</strong> ${order.payMethod}</div>
            <div class="col-md-6"><strong>Total:</strong> ${formatP(order.total)}</div>
          </div>
        </div>
        <div class="vstack gap-2 mb-3">
          ${order.items.map(it => `
            <div class="border rounded p-2">
              <div class="d-flex justify-content-between"><strong>${it.title}</strong><span>${formatP(it.price)} × ${it.qty}</span></div>
              <div class="small text-body-secondary"><i class="bi bi-geo-alt me-1"></i>${it.place} · <i class="bi bi-calendar-event ms-2 me-1"></i>${it.when}</div>
            </div>`).join('')}
        </div>
        <div class="border rounded p-3 mb-3">
          <h5 class="mb-2">Tickets</h5>
          <div class="row g-2">
            ${order.tickets.map(t => `
              <div class="col-12 col-sm-6 col-lg-4">
                <div class="border rounded p-2 d-flex align-items-center gap-2 ticket-badge">
                  <i class="bi bi-qr-code fs-3"></i>
                  <div>
                    <div class="small text-body-secondary">${API.getEvent(t.eventId)?.title||''}</div>
                    <strong>${t.code}</strong>
                  </div>
                </div>
              </div>`).join('')}
          </div>
        </div>
        <div class="d-flex gap-2">
          <a class="btn btn-outline-secondary" href="#account"><i class="bi bi-ticket-perforated me-1"></i>Ver mis entradas</a>
          <a class="btn btn-primary" href="#home"><i class="bi bi-house me-1"></i>Ir al inicio</a>
        </div>
      </div>`;
  }

  function renderLogin(){
    setActiveView('view-login');
    $('#view-login').innerHTML = `
      <div class="py-4">
        <h2 id="login-title" class="fw-bold mb-3">Ingresar</h2>
        <div class="row g-4">
          <div class="col-md-6">
            <div class="border rounded p-3">
              <h5>Login</h5>
              <div class="mb-2">
                <label class="form-label" for="lemail">Email</label>
                <input id="lemail" type="email" class="form-control" required>
              </div>
              <div class="mb-2">
                <label class="form-label" for="lpass">Contraseña</label>
                <input id="lpass" type="password" class="form-control" required>
              </div>
              <button id="btnLogin" class="btn btn-primary w-100">Ingresar</button>
              <div class="form-text mt-1"><a href="#">¿Olvidaste tu contraseña?</a></div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="border rounded p-3">
              <h5>Registro</h5>
              <div class="mb-2">
                <label class="form-label" for="rname">Nombre</label>
                <input id="rname" type="text" class="form-control" required>
              </div>
              <div class="mb-2">
                <label class="form-label" for="remail">Email</label>
                <input id="remail" type="email" class="form-control" required>
              </div>
              <div class="mb-2">
                <label class="form-label" for="rpass">Contraseña</label>
                <input id="rpass" type="password" class="form-control" required>
              </div>
              <div class="mb-3">
                <label class="form-label" for="rpass2">Repetir contraseña</label>
                <input id="rpass2" type="password" class="form-control" required>
              </div>
              <button id="btnRegister" class="btn btn-success w-100">Crear cuenta</button>
            </div>
          </div>
        </div>
      </div>`;

    $('#btnLogin').addEventListener('click', ()=>{
      const email = $('#lemail').value.trim(); const pass = $('#lpass').value;
      const res = API.login(email, pass);
      if(res.ok){ showToast('success','Bienvenido', res.user.name||res.user.email); window.location.hash = '#home'; updateCartCount(); }
      else showToast('error','Error', res.msg||'No se pudo iniciar sesión');
    });
    $('#btnRegister').addEventListener('click', ()=>{
      const name = $('#rname').value.trim(); const email = $('#remail').value.trim(); const pass = $('#rpass').value; const pass2 = $('#rpass2').value;
      if(pass!==pass2){ showToast('error','Las contraseñas no coinciden',''); return; }
      const res = API.register({name, email, pass});
      if(res.ok){ showToast('success','Cuenta creada', 'Se inició sesión.'); window.location.hash = '#home'; updateCartCount(); }
      else showToast('error','Error', res.msg||'No se pudo registrar');
    });
  }

  function renderAccount(){
    const me = requireSession(); if(!me) return;
    setActiveView('view-account');
    const orders = API.getOrders(me.id);
    $('#view-account').innerHTML = `
      <div class="py-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h2 id="account-title" class="fw-bold mb-0">Mi cuenta</h2>
          <button id="btnLogout" class="btn btn-outline-danger"><i class="bi bi-box-arrow-right me-1"></i>Salir</button>
        </div>
        <div class="row g-4">
          <div class="col-lg-4">
            <div class="border rounded p-3 h-100">
              <h5>Perfil</h5>
              <div><strong>${me.name||'(sin nombre)'}</strong></div>
              <div class="text-body-secondary">${me.email}</div>
              <hr>
              <h6>Billetera</h6>
              <div class="mb-2">Saldo: <strong>${formatP(me.wallet||0)}</strong></div>
              <div class="input-group input-group-sm">
                <span class="input-group-text">S/</span>
                <input id="topupAmt" type="number" class="form-control" min="1" value="20">
                <button id="btnTopup" class="btn btn-outline-secondary">Recargar</button>
              </div>
            </div>
          </div>
          <div class="col-lg-8">
            <div class="border rounded p-3 h-100">
              <h5>Mis órdenes y tickets</h5>
              ${orders.length? '' : '<div class="text-body-secondary">Aún no tienes órdenes.</div>'}
              <div class="vstack gap-3 mt-2">
                ${orders.map(o => `
                  <div class="border rounded p-2">
                    <div class="d-flex justify-content-between align-items-center">
                      <div>
                        <div class="small text-body-secondary">${new Date(o.createdAt).toLocaleString()}</div>
                        <strong>Pedido ${o.id}</strong> — ${o.payMethod}
                      </div>
                      <div><strong>${formatP(o.total)}</strong></div>
                    </div>
                    <div class="mt-2 small">Tickets:</div>
                    <div class="row g-2">
                      ${o.tickets.map(t => `
                        <div class="col-12 col-md-6">
                          <div class="border rounded p-2 d-flex align-items-center gap-2 ticket-badge">
                            <i class="bi bi-qr-code fs-4"></i>
                            <div>
                              <div class="small text-body-secondary">${API.getEvent(t.eventId)?.title||''}</div>
                              <strong>${t.code}</strong>
                            </div>
                          </div>
                        </div>`).join('')}
                    </div>
                  </div>`).join('')}
              </div>
            </div>
          </div>
        </div>
      </div>`;

    $('#btnTopup').addEventListener('click', ()=>{ const n = Number($('#topupAmt').value||0); if(n<=0) return; const newBal = API.addWallet(n); showToast('success','Recarga exitosa', formatP(n)); renderAccount(); });
    $('#btnLogout').addEventListener('click', ()=>{ API.logout(); showToast('info','Sesión cerrada',''); window.location.hash = '#home'; });
  }

  document.addEventListener('click', (e)=>{
    const t = e.target;
    if (t.matches('#btnGoCheckout')){ window.location.hash = '#checkout'; }
    if (t.matches('#applyCouponBtn')){
      const code = $('#couponInput').value.trim(); const res = API.applyCoupon(code, API.calcTotals(API.getCart()).total);
      if(res.valid){ APPLIED_COUPON = res.code; showToast('success','Cupón aplicado', `- ${formatP(res.amount)}`); } else { APPLIED_COUPON = null; showToast('error','Cupón inválido',''); }
      calcCartTotals();
    }
  });

  window.addEventListener('hashchange', router);
  window.addEventListener('DOMContentLoaded', () => {
    router(); onScroll(); updateCartCount(); renderCart(); calcCartTotals();
    showToast('info','Bienvenida','SPA cargada.');
  });
})();
