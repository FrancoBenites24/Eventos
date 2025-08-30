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

  // Verificar si el usuario ha iniciado sesión
  function checkSession() {
    try {
      // Intentar obtener la sesión del localStorage
      const sessionData = localStorage.getItem('session');
      if (!sessionData) {
        return null;
      }
      
      const session = JSON.parse(sessionData);
      if (!session || !session.userId) {
        return null;
      }
      
      // Obtener los usuarios
      const usersData = localStorage.getItem('users');
      if (!usersData) {
        return null;
      }
      
      const users = JSON.parse(usersData);
      const user = users.find(u => u.id === session.userId);
      
      return user || null;
    } catch (error) {
      console.error("Error al verificar sesión:", error);
      return null;
    }
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

  // Renderizar el perfil
  function renderProfile() {
    console.log("Renderizando perfil");
    
    const profileContainer = document.getElementById('view-profile');
    if (!profileContainer) {
      console.error("No se encontró el contenedor del perfil");
      return;
    }

    // Verificar si el usuario ha iniciado sesión
    const user = checkSession();
    
    if (!user) {
      // Mostrar formulario de inicio de sesión
      profileContainer.innerHTML = `
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
      return;
    }

    // Obtener las órdenes del usuario
    const orders = getUserOrders(user.id);
    
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
                orderDate: order.createdAt
              });
            }
          } catch (e) {
            console.error("Error al procesar fecha del evento:", e);
          }
        });
      }
    });
    
    // Limitar a 3 eventos próximos para mostrar
    const limitedEvents = upcomingEvents.slice(0, 3);

    // Renderizar el perfil
    profileContainer.innerHTML = `
      <div class="py-4">
        <h2 id="profile-title" class="fw-bold mb-4">Mi Perfil</h2>
        <div class="row">
          <div class="col-lg-4 mb-4">
            <div class="card shadow-sm">
              <div class="card-body text-center p-4">
                <div class="position-relative d-inline-block mb-3">
                  <img src="https://picsum.photos/seed/user${user.id}/150/150.jpg" class="rounded-circle border border-4 border-white shadow-sm" alt="Foto de perfil">
                  <button class="btn btn-sm btn-light position-absolute bottom-0 end-0 rounded-circle p-2 shadow-sm" id="changePhotoBtn">
                    <i class="bi bi-camera"></i>
                  </button>
                </div>
                <h4 class="mb-1">${user.name || 'Usuario'}</h4>
                <p class="text-muted mb-3">${user.email}</p>
                <div class="d-flex justify-content-center mb-3">
                  <span class="badge bg-primary me-2">Miembro</span>
                  <span class="badge bg-success">Verificado</span>
                </div>
                <button class="btn btn-outline-primary w-100 mb-2" id="editProfileBtn">
                  <i class="bi bi-pencil-square me-2"></i>Editar Perfil
                </button>
                <button class="btn btn-sm btn-link text-muted w-100" id="settingsBtn">
                  <i class="bi bi-gear me-2"></i>Configuración
                </button>
              </div>
            </div>
            
            <div class="card shadow-sm mt-4">
              <div class="card-body">
                <h5 class="card-title mb-3">Estadísticas</h5>
                <div class="d-flex justify-content-between mb-2">
                  <span>Eventos Asistidos</span>
                  <span class="fw-bold">${orders.length}</span>
                </div>
                <div class="d-flex justify-content-between mb-2">
                  <span>Próximos Eventos</span>
                  <span class="fw-bold">${upcomingEvents.length}</span>
                </div>
                <div class="d-flex justify-content-between">
                  <span>Saldo</span>
                  <span class="fw-bold">${formatP(user.wallet || 0)}</span>
                </div>
                <button class="btn btn-sm btn-outline-secondary w-100 mt-3" id="manageBalanceBtn">
                  <i class="bi bi-wallet2 me-1"></i>Gestionar Saldo
                </button>
              </div>
            </div>
          </div>
          
          <div class="col-lg-8">
            <div class="card shadow-sm mb-4">
              <div class="card-body p-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                  <h4 class="mb-0">Información Personal</h4>
                  <button class="btn btn-sm btn-outline-secondary" id="editInfoBtn">
                    <i class="bi bi-pencil me-1"></i>Editar
                  </button>
                </div>
                
                <div class="row mb-3">
                  <div class="col-md-6 mb-3">
                    <label class="form-label small text-muted">Nombre Completo</label>
                    <p class="fw-bold">${user.name || 'No especificado'}</p>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label class="form-label small text-muted">Email</label>
                    <p class="fw-bold">${user.email}</p>
                  </div>
                </div>
                
                <div class="mb-3">
                  <label class="form-label small text-muted">Biografía</label>
                  <p>Amante de los eventos y experiencias culturales. Siempre buscando nuevas actividades para disfrutar y compartir.</p>
                </div>
                
                <div class="mb-3">
                  <label class="form-label small text-muted">Intereses</label>
                  <div class="d-flex flex-wrap gap-2">
                    <span class="badge bg-light text-dark">Música</span>
                    <span class="badge bg-light text-dark">Arte</span>
                    <span class="badge bg-light text-dark">Tecnología</span>
                    <span class="badge bg-light text-dark">Deportes</span>
                    <span class="badge bg-light text-dark">Gastronomía</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="card shadow-sm mb-4">
              <div class="card-body p-4">
                <h4 class="mb-4">Próximos Eventos</h4>
                ${limitedEvents.length > 0 ? `
                  <div class="row g-3">
                    ${limitedEvents.map(event => `
                      <div class="col-md-6">
                        <div class="card border-0 shadow-sm h-100">
                          <div class="row g-0">
                            <div class="col-4">
                              <img src="${event.img || 'https://picsum.photos/seed/event/100/100.jpg'}" class="img-fluid rounded-start h-100 object-fit-cover" alt="${event.title}">
                            </div>
                            <div class="col-8">
                              <div class="card-body">
                                <h6 class="card-title">${event.title}</h6>
                                <p class="card-text small text-muted">
                                  <i class="bi bi-calendar3 me-1"></i> ${event.when}
                                </p>
                                <p class="card-text small text-muted">
                                  <i class="bi bi-geo-alt me-1"></i> ${event.place}
                                </p>
                                <span class="badge bg-success">Confirmado</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    `).join('')}
                  </div>
                ` : '<p class="text-muted">No tienes próximos eventos.</p>'}
                <div class="text-center mt-3">
                  <a href="index.html#account" class="btn btn-sm btn-link">Ver todos mis eventos</a>
                </div>
              </div>
            </div>
            
            <div class="card shadow-sm">
              <div class="card-body p-4">
                <h4 class="mb-4">Actividad Reciente</h4>
                <div class="timeline">
                  ${orders.length > 0 ? orders.slice(0, 3).map(order => `
                    <div class="timeline-item mb-3 pb-3 border-bottom">
                      <div class="d-flex">
                        <div class="me-3">
                          <div class="bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;">
                            <i class="bi bi-calendar-check"></i>
                          </div>
                        </div>
                        <div>
                          <p class="mb-1">Registraste <strong>${order.items ? order.items.length : 0} evento${order.items && order.items.length > 1 ? 's' : ''}</strong></p>
                          <small class="text-muted">Hace ${Math.floor((new Date() - new Date(order.createdAt)) / (1000 * 60 * 60 * 24))} días</small>
                        </div>
                      </div>
                    </div>
                  `).join('') : '<p class="text-muted">No hay actividad reciente.</p>'}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;

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
        showEditProfileModal(user);
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
        showBalanceModal(user);
      });
    }
    
    const editInfoBtn = document.getElementById('editInfoBtn');
    if (editInfoBtn) {
      editInfoBtn.addEventListener('click', () => {
        showEditProfileModal(user);
      });
    }
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
    if (profilePhone) profilePhone.value = '+51 987 654 321';
    if (profileLocation) profileLocation.value = 'Lima, Perú';
    if (profileBio) profileBio.value = 'Amante de los eventos y experiencias culturales. Siempre buscando nuevas actividades para disfrutar y compartir.';
    
    const modal = new bootstrap.Modal(document.getElementById('editProfileModal'));
    modal.show();
    
    // Event listener para guardar cambios
    const saveProfileBtn = document.getElementById('saveProfileBtn');
    if (saveProfileBtn) {
      saveProfileBtn.onclick = function() {
        const name = document.getElementById('profileName').value.trim();
        const email = document.getElementById('profileEmail').value.trim();
        
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
            localStorage.setItem('users', JSON.stringify(users));
            
            showToast('success', 'Perfil Actualizado', 'Tu perfil ha sido actualizado correctamente');
            modal.hide();
            renderProfile(); // Recargar el perfil
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
        
        const user = checkSession();
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
            
            showToast('success', 'Saldo Agregado', `Se agregó ${formatP(amount)} a tu billetera`);
            
            // Actualizar el saldo en el modal
            if (currentBalance) {
              currentBalance.textContent = formatP(users[userIndex].wallet);
            }
            
            // Actualizar el perfil
            renderProfile();
          }
        } catch (error) {
          console.error("Error al agregar saldo:", error);
          showToast('error', 'Error', 'No se pudo agregar el saldo');
        }
      };
    }
  }

  // Inicialización
  document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM cargado");
    renderProfile();
    
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