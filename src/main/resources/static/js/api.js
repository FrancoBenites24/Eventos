(function(){
  const PEN = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN', minimumFractionDigits: 2 });
  const LS = {
    get(key, def){ try{ return JSON.parse(localStorage.getItem(key)) ?? def; }catch{ return def; } },
    set(key, val){ localStorage.setItem(key, JSON.stringify(val)); }
  };

  LS.set('events', [
    {id:1, title:'Tech Summit 2025', date:'2025-09-20', location:'lima', price:120, category:'tech', tags:['cloud', 'ai', 'devops'], brief:'Conferencia sobre IA aplicada y arquitecturas cloud nativas.', image:'https://www.politico.com/dims4/default/resize/1040/quality/90/?url=https%3A%2F%2Fstatic.politico.com%2F02%2F02%2Fe305d48b4877ade360e1c694268a%2F20240917-tech-cms-shared-graphic.png', url:'evento.html?id=e1'},
    {id:2, title:'Festival de Música Andina', date:'2025-10-05', location:'cusco', price:60, category:'music', tags:['folk', 'tradicional'], brief:'Artistas locales e internacionales celebran la música de los Andes.', image:'https://images.unsplash.com/photo-1511379938547-c1f69419868d?q=80&w=1600&auto=format&fit=crop', url:'evento.html?id=e2'},
    {id:3, title:'Expo Negocios & Startups', date:'2025-09-28', location:'lima', price:0, category:'business', tags:['networking', 'pitch'], brief:'Feria de innovación, stands y ruedas de negocio para startups.', image:'https://images.unsplash.com/photo-1551836022-d5d88e9218df?q=80&w=1600&auto=format&fit=crop', url:'evento.html?id=e3'},
    {id:4, title:'Maratón Costa Verde', date:'2025-11-12', location:'lima', price:40, category:'sports', tags:['running', 'salud'], brief:'Recorrido de 21K junto al mar. Cupos limitados.', image:'', url:'evento.html?id=e4'},
    {id:5, title:'Bienal de Arte Contemporáneo', date:'2025-12-03', location:'arequipa', price:25, category:'art', tags:['exposicion', 'galeria'], brief:'Obras de artistas emergentes y consagrados en espacios históricos.', image:'https://pixnio.com/free-images/2022/04/19/2022-04-19-09-14-10-1350x971.jpg', url:'evento.html?id=e5'},
    {id:6, title:'Taller de Data Science', date:'2025-09-10', location:'virtual', price:15, category:'education', tags:['python', 'ml'], brief:'Introducción práctica a modelos supervisados con Python.', image:'https://images.unsplash.com/photo-1555066931-4365d14bab8c?q=80&w=1600&auto=format&fit=crop', url:'evento.html?id=e6'},
    {id:7, title:'Noche de Jazz en el Parque', date:'2025-09-18', location:'lima', price:30, category:'music', tags:['jazz', 'live'], brief:'Concierto al aire libre con solistas invitados.', image:'https://images.unsplash.com/photo-1483412033650-1015ddeb83d1?q=80&w=1600&auto=format&fit=crop', url:'evento.html?id=e7'},
    {id:8, title:'Hackathon Universitaria', date:'2025-09-25', location:'lima', price:0, category:'tech', tags:['estudiantes', 'hackathon'], brief:'48 horas para crear soluciones a retos reales.', image:'https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=1600&auto=format&fit=crop', url:'evento.html?id=e8'},
    {id:9, title:'Cumbre de Emprendimiento Social', date:'2025-10-22', location:'cusco', price:45, category:'business', tags:['impacto', 'ong'], brief:'Casos de éxito y workshops de innovación social.', image:'https://images.unsplash.com/photo-1492724441997-5dc865305da7?q=80&w=1600&auto=format&fit=crop', url:'evento.html?id=e9'},
    {id:10, title:'Festival de Cómic y Manga', date:'2025-10-15', location:'lima', price:20, category:'art', tags:['ilustracion', 'cultura'], brief:'Exposiciones, talleres y meet & greets con artistas.', image:'https://festivalcomicmanga.com/wp-content/uploads/2024/07/logo2.png', url:'evento.html?id=e10'},
    {id:11, title:'Taller de Fotografía Urbana', date:'2025-11-01', location:'arequipa', price:55, category:'education', tags:['fotografia', 'workshop'], brief:'Aprende a capturar la esencia de la ciudad.', image:'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTZWFlFba-W2H2KVQO2rw-rVsbESdoTwlXKvA&s', url:'evento.html?id=e11'},
    {id:12, title:'Cena Solidaria', date:'2025-12-10', location:'lima', price:150, category:'business', tags:['gala', 'solidario'], brief:'Evento benéfico para recaudar fondos para una causa social.', image:'https://caretas.pe/wp-content/uploads/2024/12/banco-2-1153x768.jpg', url:'evento.html?id=e12'},
    {id:13, title:'Campeonato de Ajedrez Nacional', date:'2025-10-29', location:'lima', price:0, category:'sports', tags:['ajedrez', 'competencia'], brief:'Torneo abierto para jugadores de todas las edades.', image:'https://federacionperuanadeajedrez.org/wp-content/uploads/2024/05/Imagen-de-WhatsApp-2024-05-20-a-las-13.23.01_e052eefa.jpg', url:'evento.html?id=e13'},
    {id:14, title:'Clínica de Béisbol para Niños', date:'2025-11-20', location:'lima', price:20, category:'sports', tags:['niños', 'deporte'], brief:'Taller gratuito para iniciarse en este deporte.', image:'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQgnX24RqkV0I0iOxKa7VHN2MRhLjLJ8Ml1fw&s', url:'evento.html?id=e14'},
    {id:15, title:'WebDev Conference', date:'2025-11-25', location:'virtual', price:75, category:'tech', tags:['frontend', 'backend', 'ux'], brief:'Charla de expertos en desarrollo web full-stack.', image:'https://ct-webdev.com/wp-content/uploads/2025/04/MG_0080_heinz_1_1.jpg', url:'evento.html?id=e15'},
    {id:16, title:'Taller de Robótica para Niños', date:'2025-11-15', location:'piura', price:50, category:'education', tags:['robotica', 'steam'], brief:'Aprende a construir y programar tu propio robot desde cero.', image:'https://portal.andina.pe/EDPfotografia3/Thumbnail/2022/12/21/000920659W.jpg', url:'evento.html?id=e16'},
    {id:17, title:'Festival de la Cumbia Piurana', date:'2025-12-08', location:'piura', price:20, category:'music', tags:['cumbia', 'tropical'], brief:'Una noche para bailar y disfrutar de los mejores exponentes de la cumbia local.', image:'https://i.scdn.co/image/ab67616d0000b2736697c5cde5b6a5cc9582c588', url:'evento.html?id=e17'},
    {id:18, title:'Seminario de Marketing Digital', date:'2025-10-01', location:'virtual', price:150, category:'business', tags:['marketing', 'seo', 'rrss'], brief:'Descubre las últimas tendencias y estrategias para crecer en línea.', image:'https://www.feedough.com/wp-content/uploads/2020/08/what-is-marketing.png', url:'evento.html?id=e18'},
    {id:19, title:'Exposición de Arte Moche', date:'2025-11-05', location:'lima', price:0, category:'art', tags:['cultura', 'historia'], brief:'Un recorrido por la iconografía y el legado de la cultura Moche.', image:'https://detrujillo.com/wp-content/uploads/2018/03/unt-realiza-exhibicion-de-textiles-post-mochicas-en-museo-huachas-de-moche.jpg', url:'evento.html?id=e19'},
    {id:20, title:'Campeonato de Surf Mancora', date:'2025-11-28', location:'piura', price:80, category:'sports', tags:['surf', 'mar'], brief:'Competencia de surf en la reconocida playa del norte del Perú.', image:'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ9H-z7hfm67jukdxLEOcEqRukrDl5e3t1Pvg&s', url:'evento.html?id=e20'}
  ]);
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
      if(existing) existing.qty += qty; else cart.push({ id: crypto.randomUUID(), eventId: ev.id, title: ev.title, price: ev.price, qty, img: ev.image, place: ev.location, when: ev.date });
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
      const user = { id: crypto.randomUUID(), name, email, passHash: btoa(pass), wallet: 100.00, phone: '', location: '', bio: '' };
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
