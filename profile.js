(function(){
  console.log("Iniciando profile.js");

  // Función simple para mostrar un toast
  function showToast(type = 'info', title = 'Info', message = ''){
    const toastContainer = document.getElementById('toasts');
    if (!toastContainer) return;
    
    const el = document.createElement('div');
    el.className = `toast align-items-center text-white bg-${type === 'error' ? 'danger' : type === 'success' ? 'success' : 'primary'}`;
    el.setAttribute('role','status');
    el.setAttribute('aria-live','polite');
    el.innerHTML = `
      <div class="toast-header">
        <i class="bi bi-${type === 'error' ? 'exclamation-triangle' : type === 'success' ? 'check-circle' : 'info-circle'} me-2"></i>
        <strong class="me-auto">${title}</strong>
        <small class="text-white">ahora</small>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Cerrar"></button>
      </div>
      <div class="toast-body">${message}</div>`;
    toastContainer.appendChild(el);
    const toast = new bootstrap.Toast(el, { delay: 3500 });
    toast.show();
  }

  // Formatear moneda
  function formatP(v) {
    return new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN', minimumFractionDigits: 2 }).format(v || 0);
  }

  // Obtener órdenes del usuario
  function getUserOrders(userId) {
    try {
      const ordersData = localStorage.getItem('orders');
      if (!ordersData) return [];
      
      const orders = JSON.parse(ordersData);
      return orders.filter(order => order.userId === userId) || [];
    } catch (error) {
      console.error("Error al obtener órdenes:", error);
      return [];
    }
  }

  // Función para obtener movimientos de la billetera
  function getWalletMovements(userId) {
    try {
      const movementsData = localStorage.getItem(`walletMovements_${userId}`);
      if (!movementsData) return [];
      
      const movements = JSON.parse(movementsData);
      return movements || [];
    } catch (error) {
      console.error("Error al obtener movimientos de billetera:", error);
      return [];
    }
  }

  // Función para agregar un movimiento a la billetera
  function addWalletMovement(userId, amount, description) {
    try {
      const movements = getWalletMovements(userId);
      movements.push({
        id: Date.now(),
        amount: amount,
        description: description,
        date: new Date().toISOString()
      });
      
      localStorage.setItem(`walletMovements_${userId}`, JSON.stringify(movements));
      return true;
    } catch (error) {
      console.error("Error al agregar movimiento de billetera:", error);
      return false;
    }
  }

  // Función para obtener todos los movimientos (incluyendo compras)
  function getAllMovements(userId) {
    try {
      // Obtener movimientos de billetera
      const walletMovements = getWalletMovements(userId);
      
      // Obtener órdenes del usuario
      const orders = getUserOrders(userId);
      
      // Convertir órdenes a movimientos
      const purchaseMovements = orders.map(order => ({
        id: `order_${order.id}`,
        amount: -order.total, // Negativo porque es un gasto
        description: `Compra: ${order.items.map(item => item.title).join(', ')}`,
        date: order.createdAt,
        type: 'purchase'
      }));
      
      // Combinar y ordenar por fecha (más recientes primero)
      const allMovements = [...walletMovements, ...purchaseMovements];
      allMovements.sort((a, b) => new Date(b.date) - new Date(a.date));
      
      return allMovements;
    } catch (error) {
      console.error("Error al obtener todos los movimientos:", error);
      return [];
    }
  }

  // Función para obtener información de un evento
  function getEvent(eventId) {
    try {
      const eventsData = localStorage.getItem('events');
      if (!eventsData) return null;
      
      const events = JSON.parse(eventsData);
      return events.find(event => event.id === Number(eventId)) || null;
    } catch (error) {
      console.error("Error al obtener evento:", error);
      return null;
    }
  }

  // Función para cargar datos del perfil
  function loadProfileData() {
    console.log("Cargando datos del perfil");

    // Verificar si el usuario ha iniciado sesión
    const user = API.me();

    if (!user) {
      // Mostrar formulario de inicio de sesión
      const viewProfile = document.getElementById('view-profile');
      if (viewProfile) {
        viewProfile.innerHTML = `
          <div class="container py-5 text-center">
            <div class="card shadow-sm mx-auto" style="max-width: 500px;">
              <div class="card-body p-5">
                <i class="bi bi-person-circle fs-1 text-primary mb-3"></i>
                <h2 class="mb-3">Inicia sesión</h2>
                <p class="text-muted mb-4">Para ver tu perfil, necesitas iniciar sesión primero.</p>
                <a href="index.html#login" class="btn btn-primary">Iniciar Sesión</a>
              </div>
            </div>
          </div>
        `;
      }
      return;
    }

    // Actualizar elementos del perfil con datos del usuario
    const profilePhoto = document.getElementById('profilePhoto');
    if (profilePhoto) {
      profilePhoto.src = `https://picsum.photos/seed/user${user.id}/150/150.jpg`;
    }
    
    const profileNameDisplay = document.getElementById('profileNameDisplay');
    if (profileNameDisplay) {
      profileNameDisplay.textContent = user.name || 'Usuario';
    }
    
    const profileEmailDisplay = document.getElementById('profileEmailDisplay');
    if (profileEmailDisplay) {
      profileEmailDisplay.textContent = user.email;
    }
    
    const personalName = document.getElementById('personalName');
    if (personalName) {
      personalName.textContent = user.name || 'No especificado';
    }
    
    const personalEmail = document.getElementById('personalEmail');
    if (personalEmail) {
      personalEmail.textContent = user.email;
    }

    const personalPhone = document.getElementById('personalPhone');
    if (personalPhone) {
      personalPhone.textContent = user.phone || 'No especificado';
    }

    const personalLocation = document.getElementById('personalLocation');
    if (personalLocation) {
      personalLocation.textContent = user.location || 'No especificado';
    }

    const personalBio = document.getElementById('personalBio');
    if (personalBio) {
      personalBio.textContent = user.bio || 'Amante de los eventos y experiencias culturales. Siempre buscando nuevas actividades para disfrutar y compartir.';
    }

    const walletBalance = document.getElementById('walletBalance');
    if (walletBalance) {
      walletBalance.textContent = formatP(user.wallet || 0);
    }
    
    const currentBalance = document.getElementById('currentBalance');
    if (currentBalance) {
      currentBalance.textContent = formatP(user.wallet || 0);
    }

    // Obtener las órdenes del usuario
    const orders = getUserOrders(user.id);
    
    // Calcular puntos y nivel (cada 1 sol = 0.5 puntos)
    let totalSpent = 0;
    orders.forEach(order => {
      totalSpent += order.items.reduce((sum, item) => sum + (item.price || 0) * (item.qty || 1), 0);
    });
    
    // Calcular puntos (cada 1 sol = 0.5 puntos)
    const totalPoints = Math.floor(totalSpent * 0.5);
    
    // Calcular nivel (cada 100 puntos = 1 nivel)
    const level = Math.floor(totalPoints / 100) + 1;
    const nextLevelPoints = level * 100;
    const progressPercentage = ((totalPoints % 100) / 100) * 100;
    
    // Actualizar puntos y nivel
    const totalPointsEl = document.getElementById('totalPoints');
    if (totalPointsEl) {
      totalPointsEl.textContent = `${totalPoints} pts`;
    }
    
    const levelDisplay = document.getElementById('levelDisplay');
    if (levelDisplay) {
      levelDisplay.textContent = `Nivel ${level}`;
    }
    
    const pointsToNextLevel = document.getElementById('pointsToNextLevel');
    if (pointsToNextLevel) {
      pointsToNextLevel.textContent = `${nextLevelPoints - totalPoints} pts para siguiente nivel`;
    }
    
    const levelProgressBar = document.getElementById('levelProgressBar');
    if (levelProgressBar) {
      levelProgressBar.style.width = `${progressPercentage}%`;
      levelProgressBar.setAttribute('aria-valuenow', progressPercentage);
    }

    // Obtener todos los movimientos (billetera + compras)
    const allMovements = getAllMovements(user.id);
    
    // Actualizar movimientos recientes
    const recentMovements = document.getElementById('recentMovements');
    if (recentMovements) {
      if (allMovements.length > 0) {
        recentMovements.innerHTML = allMovements.slice(0, 3).map(movement => `
          <div class="d-flex justify-content-between mb-2">
            <div>
              <div>${movement.description}</div>
              <small class="text-muted">${new Date(movement.date).toLocaleDateString()}</small>
            </div>
            <div class="${movement.amount > 0 ? 'text-success' : 'text-danger'}">
              ${movement.amount > 0 ? '+' : ''}${formatP(movement.amount)}
            </div>
          </div>
        `).join('');
      } else {
        recentMovements.innerHTML = '<p class="text-muted small">No hay movimientos recientes</p>';
      }
    }

    // Calcular próximos eventos
    const upcomingEvents = [];
    orders.forEach(order => {
      if (order.items && Array.isArray(order.items)) {
        order.items.forEach(item => {
          try {
            const eventDate = new Date(item.when);
            if (eventDate > new Date()) {
              upcomingEvents.push({
                ...item,
                orderId: order.id,
                orderDate: order.createdAt,
                orderTotal: order.total,
                orderPayMethod: order.payMethod,
                orderTickets: order.tickets
              });
            }
          } catch (e) {
            console.error("Error al procesar fecha del evento:", e);
          }
        });
      }
    });

    // Actualizar próximos eventos
    const upcomingEventsContainer = document.getElementById('upcomingEventsContainer');
    if (upcomingEventsContainer) {
      if (upcomingEvents.length > 0) {
        upcomingEventsContainer.innerHTML = `
          <div class="swiper-container eventsSwiper">
            <div class="swiper-wrapper">
              ${upcomingEvents.map(event => `
                <div class="swiper-slide">
                  <div class="event-card h-100" data-event-id="${event.eventId}" data-order-id="${event.orderId}">
                    <div class="position-relative">
                      <img src="${event.img || 'https://picsum.photos/seed/event/600/300.jpg'}" class="card-img-top" alt="${event.title}">
                      <div class="position-absolute top-0 end-0 m-2">
                        <span class="badge bg-success">Confirmado</span>
                      </div>
                    </div>
                    <div class="card-body">
                      <h5 class="card-title">${event.title}</h5>
                      <p class="card-text">
                        <i class="bi bi-calendar3 me-1"></i> ${event.when}
                      </p>
                      <p class="card-text">
                        <i class="bi bi-geo-alt me-1"></i> ${event.place}
                      </p>
                      <div class="d-grid">
                        <button class="btn btn-sm btn-outline-primary view-event-details">
                          Ver detalles
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              `).join('')}
            </div>
            <div class="swiper-pagination"></div>
            <div class="swiper-button-next"></div>
            <div class="swiper-button-prev"></div>
          </div>
        `;

        // Inicializar Swiper si hay eventos próximos
        setTimeout(() => {
          const swiper = new Swiper('.eventsSwiper', {
            slidesPerView: 1,
            spaceBetween: 30,
            loop: true,
            pagination: {
              el: '.swiper-pagination',
              clickable: true,
            },
            navigation: {
              nextEl: '.swiper-button-next',
              prevEl: '.swiper-button-prev',
            },
            autoplay: {
              delay: 5000,
              disableOnInteraction: false,
            },
          });
        }, 100);
      } else {
        upcomingEventsContainer.innerHTML = '<p class="text-muted">No tienes próximos eventos.</p>';
      }
    }

    // Agregar event listeners para los botones de ver detalles de evento
    document.querySelectorAll('.view-event-details').forEach(button => {
      button.addEventListener('click', function() {
        const eventCard = this.closest('.event-card');
        const eventId = eventCard.dataset.eventId;
        const orderId = eventCard.dataset.orderId;
        showEventDetailsModal(eventId, orderId);
      });
    });
  }

  // Función para mostrar modal con detalles del evento
  function showEventDetailsModal(eventId, orderId) {
    // Obtener información del evento
    const event = getEvent(eventId);
    
    // Obtener información de la orden
    const user = API.me();
    if (!user) return;
    
    const orders = getUserOrders(user.id);
    const order = orders.find(o => o.id === orderId);
    
    if (!event || !order) {
      showToast('error', 'Error', 'No se pudo encontrar la información del evento');
      return;
    }
    
    // Encontrar el item específico del evento en la orden
    const orderItem = order.items.find(item => item.eventId === Number(eventId));
    
    // Crear modal dinámicamente
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.id = 'eventDetailsModal';
    modal.setAttribute('tabindex', '-1');
    modal.setAttribute('aria-labelledby', 'eventDetailsModalLabel');
    modal.setAttribute('aria-hidden', 'true');
    
    modal.innerHTML = `
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="eventDetailsModalLabel">Detalles del Evento</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="row">
              <div class="col-md-6">
                <img src="${event.img || 'https://picsum.photos/seed/event/600/400.jpg'}" class="img-fluid rounded mb-3" alt="${event.title}">
                <div class="d-flex justify-content-between mb-2">
                  <span class="badge bg-primary"><i class="bi bi-bookmark me-1"></i>${event.cat}</span>
                  <span class="text-warning"><i class="bi bi-star-fill me-1"></i>${event.rating.toFixed(1)}</span>
                </div>
                <h4>${event.title}</h4>
                <p class="text-muted">${event.desc || ''}</p>
              </div>
              <div class="col-md-6">
                <div class="card mb-3">
                  <div class="card-header bg-light">
                    <h6 class="mb-0">Información del Evento</h6>
                  </div>
                  <div class="card-body">
                    <div class="mb-2">
                      <i class="bi bi-calendar3 me-2"></i>
                      <strong>Fecha y Hora:</strong> ${event.when}
                    </div>
                    <div class="mb-2">
                      <i class="bi bi-geo-alt me-2"></i>
                      <strong>Lugar:</strong> ${event.place}
                    </div>
                    <div>
                      <i class="bi bi-tag me-2"></i>
                      <strong>Precio:</strong> ${event.price > 0 ? formatP(event.price) : '<span class="text-success">GRATIS</span>'}
                    </div>
                  </div>
                </div>
                
                <div class="card mb-3">
                  <div class="card-header bg-light">
                    <h6 class="mb-0">Información de Compra</h6>
                  </div>
                  <div class="card-body">
                    <div class="mb-2">
                      <i class="bi bi-receipt me-2"></i>
                      <strong>Orden:</strong> #${order.id}
                    </div>
                    <div class="mb-2">
                      <i class="bi bi-calendar-date me-2"></i>
                      <strong>Fecha de Compra:</strong> ${new Date(order.createdAt).toLocaleString()}
                    </div>
                    <div class="mb-2">
                      <i class="bi bi-credit-card me-2"></i>
                      <strong>Método de Pago:</strong> ${order.payMethod}
                    </div>
                    <div>
                      <i class="bi bi-cash-stack me-2"></i>
                      <strong>Total Pagado:</strong> ${formatP(order.total)}
                    </div>
                  </div>
                </div>
                
                <div class="card">
                  <div class="card-header bg-light">
                    <h6 class="mb-0">Entradas</h6>
                  </div>
                  <div class="card-body">
                    ${order.tickets && order.tickets.length > 0 ? `
                      <div class="table-responsive">
                        <table class="table table-sm">
                          <thead>
                            <tr>
                              <th>Código</th>
                              <th>Estado</th>
                            </tr>
                          </thead>
                          <tbody>
                            ${order.tickets.filter(ticket => ticket.eventId === Number(eventId)).map(ticket => `
                              <tr>
                                <td><code>${ticket.code}</code></td>
                                <td><span class="badge bg-success">Válida</span></td>
                              </tr>
                            `).join('')}
                          </tbody>
                        </table>
                      </div>
                    ` : '<p class="text-muted">No se encontraron entradas para este evento</p>'}
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
            <button type="button" class="btn btn-primary" id="downloadTicketsBtn">
              <i class="bi bi-download me-1"></i>Descargar Entradas
            </button>
          </div>
        </div>
      </div>
    `;
    
    document.body.appendChild(modal);
    const modalInstance = new bootstrap.Modal(modal);
    modalInstance.show();
    
    // Event listener para descargar entradas
    const downloadTicketsBtn = document.getElementById('downloadTicketsBtn');
    if (downloadTicketsBtn) {
      downloadTicketsBtn.addEventListener('click', () => {
        showToast('info', 'Descargar Entradas', 'Función no implementada en esta demo');
      });
    }
    
    // Eliminar el modal del DOM cuando se cierre
    modal.addEventListener('hidden.bs.modal', function () {
      document.body.removeChild(modal);
    });
  }

  // Mostrar modal para editar perfil
  function showEditProfileModal(user) {
    // Rellenar el formulario con los datos actuales
    const profileName = document.getElementById('profileName');
    const profileEmail = document.getElementById('profileEmail');
    const profilePhone = document.getElementById('profilePhone');
    const profileLocation = document.getElementById('profileLocation');
    const profileBio = document.getElementById('profileBio');
    
    if (profileName) profileName.value = user.name || '';
    if (profileEmail) profileEmail.value = user.email || '';
    if (profilePhone) profilePhone.value = user.phone || '';
    if (profileLocation) profileLocation.value = user.location || '';
    if (profileBio) profileBio.value = user.bio || '';
    
    const modal = new bootstrap.Modal(document.getElementById('editProfileModal'));
    modal.show();
    
    // Event listener para guardar cambios
    const saveProfileBtn = document.getElementById('saveProfileBtn');
    if (saveProfileBtn) {
      saveProfileBtn.onclick = function() {
        const name = document.getElementById('profileName').value.trim();
        const email = document.getElementById('profileEmail').value.trim();
        const phone = document.getElementById('profilePhone').value.trim();
        const location = document.getElementById('profileLocation').value.trim();
        const bio = document.getElementById('profileBio').value.trim();
        
        if (!name || !email) {
          showToast('error', 'Error', 'Por favor completa todos los campos requeridos');
          return;
        }
        
        try {
          // Actualizar el usuario
          const usersData = localStorage.getItem('users');
          if (!usersData) return;
          
          const users = JSON.parse(usersData);
          const userIndex = users.findIndex(u => u.id === user.id);
          if (userIndex !== -1) {
            users[userIndex].name = name;
            users[userIndex].email = email;
            users[userIndex].phone = phone;
            users[userIndex].location = location;
            users[userIndex].bio = bio;
            localStorage.setItem('users', JSON.stringify(users));
            
            showToast('success', 'Perfil Actualizado', 'Tu perfil ha sido actualizado correctamente');
            modal.hide();
            loadProfileData(); // Recargar los datos del perfil
          }
        } catch (error) {
          console.error("Error al guardar perfil:", error);
          showToast('error', 'Error', 'No se pudo guardar el perfil');
        }
      };
    }
  }

  // Mostrar modal de configuración
  function showSettingsModal() {
    const modal = new bootstrap.Modal(document.getElementById('settingsModal'));
    modal.show();
    
    // Event listener para guardar cambios
    const saveSettingsBtn = document.getElementById('saveSettingsBtn');
    if (saveSettingsBtn) {
      saveSettingsBtn.onclick = function() {
        // Aquí guardaríamos las configuraciones del usuario
        const notifEvents = document.getElementById('notifEvents').checked;
        const notifOffers = document.getElementById('notifOffers').checked;
        const privProfile = document.getElementById('privProfile').checked;
        
        // Guardar en localStorage
        const settings = {
          notifEvents,
          notifOffers,
          privProfile
        };
        
        const user = API.me();
        if (user) {
          localStorage.setItem(`settings_${user.id}`, JSON.stringify(settings));
        }
        
        showToast('success', 'Configuración Guardada', 'Tus preferencias han sido guardadas');
        modal.hide();
      };
    }
  }

  // Mostrar modal de saldo
  function showBalanceModal(user) {
    // Mostrar saldo actual
    const currentBalance = document.getElementById('currentBalance');
    if (currentBalance) {
      currentBalance.textContent = formatP(user.wallet || 0);
    }
    
    const modal = new bootstrap.Modal(document.getElementById('balanceModal'));
    modal.show();
    
    // Event listener para agregar saldo
    const addBalanceBtn = document.getElementById('addBalanceBtn');
    if (addBalanceBtn) {
      addBalanceBtn.onclick = function() {
        const amount = Number(document.getElementById('addBalance').value);
        if (amount <= 0) {
          showToast('error', 'Error', 'Por favor ingresa un monto válido');
          return;
        }
        
        try {
          // Actualizar el saldo del usuario
          const usersData = localStorage.getItem('users');
          if (!usersData) return;
          
          const users = JSON.parse(usersData);
          const userIndex = users.findIndex(u => u.id === user.id);
          if (userIndex !== -1) {
            users[userIndex].wallet = (users[userIndex].wallet || 0) + amount;
            localStorage.setItem('users', JSON.stringify(users));
            
            // Registrar movimiento
            addWalletMovement(user.id, amount, 'Recarga de saldo');
            
            showToast('success', 'Saldo Agregado', `Se agregó ${formatP(amount)} a tu billetera`);
            
            // Actualizar el saldo en el modal
            if (currentBalance) {
              currentBalance.textContent = formatP(users[userIndex].wallet);
            }
            
            // Actualizar el perfil
            loadProfileData();
          }
        } catch (error) {
          console.error("Error al agregar saldo:", error);
          showToast('error', 'Error', 'No se pudo agregar el saldo');
        }
      };
    }
  }

  // Función para mostrar modal de movimientos
  function showMovementsModal(userId) {
    const allMovements = getAllMovements(userId);
    
    // Crear modal dinámicamente
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.id = 'movementsModal';
    modal.setAttribute('tabindex', '-1');
    modal.setAttribute('aria-labelledby', 'movementsModalLabel');
    modal.setAttribute('aria-hidden', 'true');
    
    modal.innerHTML = `
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="movementsModalLabel">Todos los Movimientos</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            ${allMovements.length > 0 ? `
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead>
                    <tr>
                      <th>Fecha</th>
                      <th>Descripción</th>
                      <th class="text-end">Monto</th>
                    </tr>
                  </thead>
                  <tbody>
                    ${allMovements.map(movement => `
                      <tr>
                        <td>${new Date(movement.date).toLocaleDateString()}</td>
                        <td>${movement.description}</td>
                        <td class="text-end ${movement.amount > 0 ? 'text-success' : 'text-danger'}">
                          ${movement.amount > 0 ? '+' : ''}${formatP(movement.amount)}
                        </td>
                      </tr>
                    `).join('')}
                  </tbody>
                </table>
              </div>
            ` : '<p class="text-center text-muted">No hay movimientos registrados</p>'}
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
          </div>
        </div>
      </div>
    `;
    
    document.body.appendChild(modal);
    const modalInstance = new bootstrap.Modal(modal);
    modalInstance.show();
    
    // Eliminar el modal del DOM cuando se cierre
    modal.addEventListener('hidden.bs.modal', function () {
      document.body.removeChild(modal);
    });
  }

  // Función para mostrar modal de editar tarjeta
  function showEditCardModal() {
    // Crear modal dinámicamente
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.id = 'editCardModal';
    modal.setAttribute('tabindex', '-1');
    modal.setAttribute('aria-labelledby', 'editCardModalLabel');
    modal.setAttribute('aria-hidden', 'true');
    
    modal.innerHTML = `
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="editCardModalLabel">Editar Tarjeta</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form id="editCardForm">
              <div class="mb-3">
                <label for="cardNumber" class="form-label">Número de Tarjeta</label>
                <input type="text" class="form-control" id="cardNumber" placeholder="1234 5678 9012 3456" value="1234 5678 9012 3456" maxlength="19">
              </div>
              <div class="row mb-3">
                <div class="col-md-6">
                  <label for="cardExpiry" class="form-label">Fecha de Vencimiento</label>
                  <input type="text" class="form-control" id="cardExpiry" placeholder="MM/AA" value="12/25">
                </div>
                <div class="col-md-6">
                  <label for="cardCVC" class="form-label">CVC</label>
                  <input type="text" class="form-control" id="cardCVC" placeholder="123" value="123" maxlength="4">
                </div>
              </div>
              <div class="mb-3">
                <label for="cardName" class="form-label">Nombre en la Tarjeta</label>
                <input type="text" class="form-control" id="cardName" placeholder="Nombre completo" value="Juan Pérez">
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
            <button type="button" class="btn btn-danger" id="removeCardBtn">Eliminar Tarjeta</button>
            <button type="button" class="btn btn-primary" id="saveCardBtn">Guardar Cambios</button>
          </div>
        </div>
      </div>
    `;
    
    document.body.appendChild(modal);
    const modalInstance = new bootstrap.Modal(modal);
    modalInstance.show();
    
    // Event listeners
    const saveCardBtn = document.getElementById('saveCardBtn');
    if (saveCardBtn) {
      saveCardBtn.addEventListener('click', () => {
        showToast('success', 'Tarjeta Actualizada', 'Los datos de tu tarjeta han sido actualizados correctamente');
        modalInstance.hide();
      });
    }
    
    const removeCardBtn = document.getElementById('removeCardBtn');
    if (removeCardBtn) {
      removeCardBtn.addEventListener('click', () => {
        if (confirm('¿Estás seguro de que deseas eliminar esta tarjeta?')) {
          showToast('info', 'Tarjeta Eliminada', 'La tarjeta ha sido eliminada de tu cuenta');
          modalInstance.hide();
        }
      });
    }
    
    // Eliminar el modal del DOM cuando se cierre
    modal.addEventListener('hidden.bs.modal', function () {
      document.body.removeChild(modal);
    });
  }

  // Inicialización
  document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM cargado");
    loadProfileData();
    
    // Agregar event listeners para los botones
    const changePhotoBtn = document.getElementById('changePhotoBtn');
    if (changePhotoBtn) {
      changePhotoBtn.addEventListener('click', () => {
        showToast('info', 'Cambiar Foto', 'Función no implementada en esta demo');
      });
    }
    
    const editProfileBtn = document.getElementById('editProfileBtn');
    if (editProfileBtn) {
      editProfileBtn.addEventListener('click', () => {
        const user = API.me();
        if (user) showEditProfileModal(user);
      });
    }
    
    const settingsBtn = document.getElementById('settingsBtn');
    if (settingsBtn) {
      settingsBtn.addEventListener('click', () => {
        showSettingsModal();
      });
    }
    
    const manageBalanceBtn = document.getElementById('manageBalanceBtn');
    if (manageBalanceBtn) {
      manageBalanceBtn.addEventListener('click', () => {
        const user = API.me();
        if (user) showBalanceModal(user);
      });
    }
    
    const editInfoBtn = document.getElementById('editInfoBtn');
    if (editInfoBtn) {
      editInfoBtn.addEventListener('click', () => {
        const user = API.me();
        if (user) showEditProfileModal(user);
      });
    }
    
    const viewAllMovementsBtn = document.getElementById('viewAllMovementsBtn');
    if (viewAllMovementsBtn) {
      viewAllMovementsBtn.addEventListener('click', () => {
        const user = API.me();
        if (user) showMovementsModal(user.id);
      });
    }
    
    const editCardBtn = document.getElementById('editCardBtn');
    if (editCardBtn) {
      editCardBtn.addEventListener('click', () => {
        showEditCardModal();
      });
    }
    
    // Actualizar contador del carrito
    try {
      const cartData = localStorage.getItem('cart');
      const cart = cartData ? JSON.parse(cartData) : [];
      const cartCount = document.getElementById('navCartCount');
      if (cartCount) {
        cartCount.textContent = cart.reduce((n, item) => n + (item.qty || 1), 0);
      }
    } catch (error) {
      console.error("Error al actualizar contador del carrito:", error);
    }
  });
})();
