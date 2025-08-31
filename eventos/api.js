(function(){
  const PEN = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN', minimumFractionDigits: 2 });
  const LS = {
    get(key, def){ try{ return JSON.parse(localStorage.getItem(key)) ?? def; }catch{ return def; } },
    set(key, val){ localStorage.setItem(key, JSON.stringify(val)); }
  };

  if(!LS.get('events')){
    LS.set('events', [
      {id:1, cat:'Música',   title:'Festival Andino 2025', desc:'Bandas indie y folk.', place:'Cusco',   when:'2025-09-20 18:00', price:120, img:'https://images.unsplash.com/photo-1511379938547-c1f69419868d?q=80&w=1400&auto=format&fit=crop', rating:4.6},
      {id:2, cat:'Tech',     title:'JS Summit Lima',       desc:'Charlas y workshops JS.', place:'Lima',    when:'2025-10-05 09:00', price:0,   img:'https://images.unsplash.com/photo-1555255707-c07966088b7b?q=80&w=1400&auto=format&fit=crop', rating:4.8},
      {id:3, cat:'Cultural', title:'Noche de Museos',      desc:'Museos abiertos y guiados.', place:'Arequipa', when:'2025-09-28 19:00', price:35,  img:'https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=1400&auto=format&fit=crop', rating:4.4},
      {id:4, cat:'Deporte',  title:'Maratón del Pacífico', desc:'42k, 21k y 10k.', place:'Trujillo', when:'2025-11-10 06:00', price:90,  img:'https://images.unsplash.com/photo-1546483875-ad9014c88eba?q=80&w=1400&auto=format&fit=crop', rating:4.7},
      {id:5, cat:'Música',   title:'Jazz en la Plaza',     desc:'Jam session al aire libre.', place:'Lima',    when:'2025-09-12 20:00', price:0,   img:'https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?q=80&w=1400&auto=format&fit=crop', rating:4.2},
      {id:6, cat:'Tech',     title:'IA para Todos',        desc:'Intro práctica a IA.', place:'Virtual', when:'2025-09-22 18:30', price:50,  img:'https://images.unsplash.com/photo-1556157382-97eda2d62296?q=80&w=1400&auto=format&fit=crop', rating:4.5},
      {id:7, cat:'Cultural', title:'Feria del Libro',      desc:'Autores y firmas.', place:'Lima', when:'2025-10-15 10:00', price:10,  img:'https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=1400&auto=format&fit=crop', rating:4.1},
      {id:8, cat:'Deporte',  title:'Copa Ciudad',          desc:'Torneo amistoso.', place:'Piura', when:'2025-09-30 15:00', price:25,  img:'https://images.unsplash.com/photo-1502877338535-766e1452684a?q=80&w=1400&auto=format&fit=crop', rating:4.0}
    ]);
  }
  if(!LS.get('users')) LS.set('users', []);
  if(!LS.get('cart')) LS.set('cart', []);
  if(!LS.get('orders')) LS.set('orders', []);
  if(!LS.get('reviews')) LS.set('reviews', []);

  const API = {
    getEvents(){ return LS.get('events', []); },
    getEvent(id){ return this.getEvents().find(e=> e.id === Number(id)); },

    getCart(){ return LS.get('cart', []); },
    addCartItem(eventId, qty=1){
      const ev = this.getEvent(eventId); if(!ev) return this.getCart();
      const cart = this.getCart();
      const existing = cart.find(i=>i.eventId===ev.id);
      if(existing) existing.qty += qty; else cart.push({ id: crypto.randomUUID(), eventId: ev.id, title: ev.title, price: ev.price, qty, img: ev.img, place: ev.place, when: ev.when });
      LS.set('cart', cart); return cart;
    },
    updateCartItem(id, qty){
      const cart = this.getCart();
      const it = cart.find(i=> i.id===id);
      if(!it) return cart;
      it.qty = Math.max(1, Number(qty)||1);
      LS.set('cart', cart); return cart;
    },
    removeCartItem(id){
      const cart = this.getCart().filter(i=> i.id!==id);
      LS.set('cart', cart); return cart;
    },
    clearCart(){ LS.set('cart', []); },
    calcTotals(cart = this.getCart(), couponCode = null){
      let subtotal = 0, qtyDiscount = 0, couponDiscount = 0;
      for(const it of cart){
        const line = (it.price||0) * (it.qty||1);
        subtotal += line;
        if(it.price>0 && it.qty>=3){ qtyDiscount += line * 0.10; }
      }
      if(couponCode){
        const res = this.applyCoupon(couponCode, subtotal - qtyDiscount);
        if(res.valid) couponDiscount = res.amount;
      }
      let total = Math.max(0, subtotal - qtyDiscount - couponDiscount);
      return { subtotal, qtyDiscount, couponDiscount, total };
    },

    applyCoupon(code, base=0){
      if(!code) return { valid:false, code:null, amount:0 };
      code = String(code).trim().toUpperCase();
      if(code==='FIESTA20'){
        return { valid:true, code, amount: Math.min(20, base) };
      }
      return { valid:false, code, amount:0 };
    },

    register({name, email, pass}){
      const users = LS.get('users', []);
      const exists = users.some(u=> u.email.toLowerCase()===String(email).toLowerCase());
      if(exists) return { ok:false, msg:'El email ya está registrado.' };
      const user = { id: crypto.randomUUID(), name, email, passHash: btoa(pass), wallet: 100.00 };
      users.push(user); LS.set('users', users);
      LS.set('session', { userId: user.id });
      return { ok:true, user };
    },
    login(email, pass){
      const users = LS.get('users', []);
      const user = users.find(u=> u.email.toLowerCase()===String(email).toLowerCase() && u.passHash===btoa(pass));
      if(!user) return { ok:false, msg:'Credenciales inválidas' };
      LS.set('session', { userId: user.id });
      return { ok:true, user };
    },
    logout(){ localStorage.removeItem('session'); },
    me(){ const s = LS.get('session'); if(!s) return null; return LS.get('users', []).find(u=> u.id===s.userId) || null; },
    addWallet(amount){ const me = this.me(); if(!me) return null; const users = LS.get('users', []); const u = users.find(x=>x.id===me.id); u.wallet = (u.wallet||0) + Number(amount||0); LS.set('users', users); return u.wallet; },

    // ===== Órdenes =====
    getOrders(userId){ return LS.get('orders', []).filter(o=> o.userId===userId); },
    createOrder({ userId, items, couponCode, payMethod }){
      const cart = items.map(it=> ({...it}));
      const totals = this.calcTotals(cart, couponCode);
      const orderId = Date.now().toString(36);
      const tickets = [];
      cart.forEach(it=>{
        for(let n=1;n<=it.qty;n++){
          tickets.push({ code:`TCK-${orderId}-${it.eventId}-${n}`, eventId: it.eventId, title: it.title });
        }
      });
      const order = { id: orderId, userId, items: cart, subtotal: +totals.subtotal.toFixed(2), qtyDiscount:+totals.qtyDiscount.toFixed(2), coupon: couponCode||null, total:+totals.total.toFixed(2), payMethod, createdAt: new Date().toISOString(), tickets };
      const orders = LS.get('orders', []); orders.unshift(order); LS.set('orders', orders);
      return order;
    },

    getReviews(eventId){ return LS.get('reviews', []).filter(r=> r.eventId===Number(eventId)); },
    createReview({eventId, userId, rating, comment}){
      const reviews = LS.get('reviews', []);
      const exists = reviews.some(r=> r.eventId===Number(eventId) && r.userId===userId);
      if(exists) return { ok:false, msg:'Ya enviaste una reseña para este evento.' };
      const rev = { id: crypto.randomUUID(), eventId:Number(eventId), userId, rating:Number(rating), comment:String(comment||'').trim(), createdAt:new Date().toISOString() };
      reviews.unshift(rev); LS.set('reviews', reviews);
      return { ok:true, review: rev };
    },

    _setCart(cart){ LS.set('cart', cart); },
    _PEN(v){ return PEN.format(v||0); }
  };

  window.API = API;
})();
